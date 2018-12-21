package com.cyraptor.uketabs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TabActivity extends AppCompatActivity implements OnItemSelectedListener {
    private Toolbar toolbar;
    private Spinner speedsSpinner;
    private ImageButton editTab;
    SwipeRefreshLayout swipeLayout;
    private static final String[] speeds = {"AutoScroll", "Speed 1", "Speed 2", "Speed 3", "Speed 4", "Speed 5"};
    final private String externalDirectory = Environment.getExternalStorageDirectory() + File.separator + "UkeTabs";

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("UkeTabs");

        speedsSpinner = (Spinner) findViewById(R.id.spinner_speeds);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(TabActivity.this, R.layout.spinner_item, speeds);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        speedsSpinner.setAdapter(adapter);
        speedsSpinner.setOnItemSelectedListener(this);

        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTabs();
            }
        });

        Intent i = getIntent();
        String songIdentifier = i.getStringExtra("songFileName");

        editTab = (ImageButton) findViewById(R.id.edit_tab);

        if (songIdentifier.contains(".txt")) {
            setFileTab();
        } else {
            setLinkTab();
            editTab.setVisibility(View.GONE);
        }
    }

    private void setLinkTab() {
        TextView songArtist = findViewById(R.id.song_artist);
        TextView songName = findViewById(R.id.song_name);
        ImageView songImage = findViewById(R.id.song_image);
        TextView songTab = findViewById(R.id.song_tab);

        Intent i = getIntent();
        String songImageURL = i.getStringExtra("songImage");
        String songLink = i.getStringExtra("songFileName");

        Glide.with(TabActivity.this)
                .load(songImageURL)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_cover))
                .into(songImage);

        songName.setText(i.getStringExtra("songName"));
        songArtist.setText(i.getStringExtra("songArtist"));

        try {
            Document document = Jsoup.connect(i.getStringExtra("songFileName")).get();

            String songChords = "";
            if (songLink.contains("ukutabs")) {
                Elements chords = document.getElementsByClass("stckchrd");
                for (Element chord : chords) {
                    songChords += chord.attr("name") + " ";
                }
            } else {
                Elements chords = document.getElementsByClass("crdinfos");
                for (Element chord : chords) {
                    songChords += (chord.attr("title").replace(" Ukulele chord", "")) + " ";
                }
            }

            String chordIdentifier;
            LinearLayout layout = (LinearLayout) findViewById(R.id.chords_layout);
            List<String> chordsList = Arrays.asList(songChords.split("\\s* \\s*"));

            for (String chord : chordsList) {
                chordIdentifier = "chord_" + chord.toLowerCase() + ".png";
                ImageView chordImage = new ImageView(this);
                chordImage.setImageBitmap(getBitmapFromAssets(chordIdentifier));
                layout.addView(chordImage);
                chordImage.getLayoutParams().height = 500;
                chordImage.getLayoutParams().width = 320;
            }

            String songTabText = "";
            if (songLink.contains("ukutabs")) {
                Elements lines = document.getElementsByClass("qoate-code");
                for (Element tabLine : lines) {
                    songTabText += tabLine.text() + "\n";
                }
            } else {
                Elements lines = document.getElementsByTag("pre");
                String linesReplaced = Jsoup.parse(lines.outerHtml().replaceAll("(?i)<br[^>]*>", "br2n")).text();
                songTabText = linesReplaced.replaceAll("br2n", "\n");
            }

            Scanner scanner = new Scanner(songTabText);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> wordsList = new LinkedList<String>(Arrays.asList(line.split("\\s* \\s*")));
                wordsList.removeAll(Collections.singleton(""));
                wordsList.retainAll(chordsList);

                if (!Collections.disjoint(chordsList, wordsList)) {
                    SpannableString coloredLine = new SpannableString(line);
                    coloredLine.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), 0);
                    Map<String, Integer> indexOfLastChord = new HashMap<>();

                    for (final String chord : wordsList) {
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                Dialog builder = new Dialog(TabActivity.this);
                                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {}
                                });

                                ImageView imageView = new ImageView(TabActivity.this);
                                imageView.setImageBitmap(getBitmapFromAssets("chord_" + chord.toLowerCase() + ".png"));
                                imageView.setBackgroundResource(R.drawable.popup_rounded_corners);

                                GradientDrawable drawable = (GradientDrawable) imageView.getBackground();
                                drawable.setColor(Color.WHITE);
                                imageView.setPadding(0, 25, 85, 75);

                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(800,1200);
                                builder.addContentView(imageView, params);
                                builder.show();
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setColor(ContextCompat.getColor(TabActivity.this, R.color.chordColor));
                                ds.setUnderlineText(false);
                            }
                        };

                        int indexStart;
                        int indexEnd;
                        int lineStart2;

                        if (indexOfLastChord.get(chord) == null) {
                            indexStart = line.indexOf(chord);
                            indexEnd = indexStart + chord.length();
                            indexOfLastChord.put(chord, indexEnd);
                        } else {
                            lineStart2 = indexOfLastChord.get(chord);
                            indexStart = line.substring(lineStart2).indexOf(chord);
                            indexEnd = indexStart + chord.length() + lineStart2;
                            indexOfLastChord.put(chord, indexEnd);
                        }

                        coloredLine.setSpan(clickableSpan, indexStart, indexEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        songTab.setHighlightColor(Color.TRANSPARENT);
                        songTab.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    songTab.append(coloredLine);
                    songTab.append("\n");
                } else {
                    songTab.append(line + "\n");
                }
            }

            scanner.close();
        } catch (IOException e) {}
    }

    private void setFileTab() {
        TextView songArtist = findViewById(R.id.song_artist);
        TextView songName = findViewById(R.id.song_name);
        ImageView songImage = findViewById(R.id.song_image);
        TextView strumPattern = findViewById(R.id.strum_pattern);
        TextView songTab = findViewById(R.id.song_tab);

        Intent i = getIntent();
        String songIdentifier = i.getStringExtra("songFileName");
        String songImageURL = i.getStringExtra("songImage");

        Glide.with(TabActivity.this)
                .load(songImageURL)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.default_cover))
                .into(songImage);

        try {
            FileInputStream inputStream = new FileInputStream (new File(Environment.getExternalStorageDirectory() + "/Uketabs/" + songIdentifier));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            songArtist.setText(br.readLine());
            songName.setText(br.readLine());
            br.readLine();
            strumPattern.setText(br.readLine());
            String line = br.readLine();
            String chordIdentifier;
            LinearLayout layout = (LinearLayout) findViewById(R.id.chords_layout);
            List<String> chordsList = Arrays.asList(line.split("\\s* \\s*"));

            for (String chord : chordsList) {
                chordIdentifier = "chord_" + chord.toLowerCase() + ".png";
                ImageView chordImage = new ImageView(this);
                chordImage.setImageBitmap(getBitmapFromAssets(chordIdentifier));
                layout.addView(chordImage);
                chordImage.getLayoutParams().height = 500;
                chordImage.getLayoutParams().width = 320;
            }

            while ((line = br.readLine()) != null) {
                List<String> wordsList = new LinkedList<String>(Arrays.asList(line.split("\\s* \\s*")));
                wordsList.removeAll(Collections.singleton(""));
                wordsList.retainAll(chordsList);

                if (!Collections.disjoint(chordsList, wordsList)) {
                    SpannableString coloredLine = new SpannableString(line);
                    coloredLine.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, line.length(), 0);
                    Map<String, Integer> indexOfLastChord = new HashMap<>();

                    for (final String chord : wordsList) {
                        ClickableSpan clickableSpan = new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                Dialog builder = new Dialog(TabActivity.this);
                                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                builder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {}
                                });

                                ImageView imageView = new ImageView(TabActivity.this);
                                imageView.setImageBitmap(getBitmapFromAssets("chord_" + chord.toLowerCase() + ".png"));
                                imageView.setBackgroundResource(R.drawable.popup_rounded_corners);

                                GradientDrawable drawable = (GradientDrawable) imageView.getBackground();
                                drawable.setColor(Color.WHITE);
                                imageView.setPadding(0, 25, 85, 75);

                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(800,1200);
                                builder.addContentView(imageView, params);
                                builder.show();
                            }

                            @Override
                            public void updateDrawState(TextPaint ds) {
                                ds.setColor(ContextCompat.getColor(TabActivity.this, R.color.chordColor));
                                ds.setUnderlineText(false);
                            }
                        };

                        int indexStart;
                        int indexEnd;
                        int lineStart2;

                        if (indexOfLastChord.get(chord) == null) {
                            indexStart = line.indexOf(chord);
                            indexEnd = indexStart + chord.length();
                            indexOfLastChord.put(chord, indexEnd);
                        } else {
                            lineStart2 = indexOfLastChord.get(chord);
                            indexStart = line.substring(lineStart2).indexOf(chord);
                            indexEnd = indexStart + chord.length() + lineStart2;
                            indexOfLastChord.put(chord, indexEnd);
                        }

                        coloredLine.setSpan(clickableSpan, indexStart, indexEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        songTab.setHighlightColor(Color.TRANSPARENT);
                        songTab.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    songTab.append(coloredLine);
                    songTab.append("\n");
                } else {
                    songTab.append(line + "\n");
                }
            }

            inputStream.close();
        } catch (IOException e) {}
    }

    public void editTab(View view) {
        Intent i = getIntent();

        File file = new File (externalDirectory, i.getStringExtra("songFileName"));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(TabActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
        intent.setDataAndType(uri, "text/plain");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Open with"));
        }
    }

    private void refreshTabs() {
        finish();
        startActivity(getIntent());
        swipeLayout.setRefreshing(false);
        Toast.makeText(TabActivity.this, "Tab has been updated", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            case 0:
                break;
            case 1:
                scrollViewWithSpeed(50, 1);
                break;
            case 2:
                scrollViewWithSpeed(50, 2);
                break;
            case 3:
                scrollViewWithSpeed(50, 3);
                break;
            case 4:
                scrollViewWithSpeed(50, 4);
                break;
            case 5:
                scrollViewWithSpeed(50, 5);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void scrollViewWithSpeed(final int scrollPeriod, final int heightToScroll) {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final UpdatedSpinner spinner = (UpdatedSpinner) findViewById(R.id.spinner_speeds);
        final long totalScrollTime = Long.MAX_VALUE;

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                final CountDownTimer countDownTimer = new CountDownTimer(totalScrollTime, scrollPeriod) {
                    public void onTick(long millisUntilFinished) {
                        scrollView.scrollBy(0, heightToScroll);

                        if (!scrollView.canScrollVertically(1)) {
                            cancel();
                        }
                    }

                    public void onFinish() {}
                }.start();

                toolbar.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        countDownTimer.cancel();
                        return false;
                    }
                });

                spinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        countDownTimer.cancel();
                        return false;
                    }
                });
            }
        });
    }

    private Bitmap getBitmapFromAssets(String fileName){
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open("chords/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(inputStream);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {}
}