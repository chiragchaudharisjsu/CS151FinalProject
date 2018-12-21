package com.cyraptor.uketabs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    SwipeRefreshLayout swipeLayout;
    private ArrayList<Tab> tabList = new ArrayList<Tab>();
    private RecyclerView recyclerView;
    private TabAdapter tAdapter;
    Handler handler;
    FloatingActionMenu fab;
    FloatingActionButton menuWeb, menuStorage;
    final private String externalDirectory = Environment.getExternalStorageDirectory() + File.separator + "UkeTabs";

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTabs();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.tabs_rv);
        handler = new Handler(Looper.getMainLooper());

        setFabs();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    File directory = new File(externalDirectory);
                    directory.mkdirs();
                    setTabsList();
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to access your External storage. Importing tabs will be disabled.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * This is method which checks if the user has granted the optional permissions to the app
     * @return Boolean.
     */
    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @SuppressLint("RestrictedApi")
    private void setFabs() {
        fab = (FloatingActionMenu) findViewById(R.id.fab);
        fab.setClosedOnTouchOutside(true);
        menuWeb = (FloatingActionButton) findViewById(R.id.menu_web);
        menuStorage = (FloatingActionButton) findViewById(R.id.menu_storage);

        menuWeb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fab.close(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Input tab URL");

                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText url = new EditText(MainActivity.this);
                url.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
                url.setHint("URL");
                //url.setText("https://ukutabs.com/t/twenty-one-pilots/cant-help-falling-in-love/");
                layout.addView(url);

                final EditText strum = new EditText(MainActivity.this);
                strum.setInputType(InputType.TYPE_CLASS_TEXT);
                strum.setHint("Strumming pattern");
                //strum.setText("dudu");
                layout.addView(strum);

                builder.setView(layout, 75,25, 75, 0);

                builder.setPositiveButton("Import", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkWriteExternalPermission()) {
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        if (url.getText().toString().contains("ukutabs.com")) {
                                            scrapeText(url.getText().toString(), strum.getText().toString(), "stckchrd");
                                        } else if (url.getText().toString().contains("ukulele-tabs.com")) {
                                            scrapeText(url.getText().toString(), strum.getText().toString(), "crdinfos");
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(MainActivity.this, "Only tab urls from ukutabs.com or ukulele-tabs.com are valid", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    } catch (Exception e) {}
                                }
                            }).start();
                        } else {
                            Toast.makeText(MainActivity.this, "Permission is denied to access your External storage", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        menuStorage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fab.close(true);

                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(1)
                        //.withPath("/sdcard")
                        .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
                        .withTitle("Select tab text file")
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            File src = new File(filePath);
            File dst = new File(externalDirectory);

            try {
                copyFile(src, dst);
            } catch (Exception e) {}
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            System.out.println("source: " + source);
            destination = new FileOutputStream(destFile).getChannel();
            System.out.println("destination: " + destination);
            destination.transferFrom(source, 0, source.size());
            System.out.println("transfer from");
        }
        catch (IOException e) {
            System.out.println("Exception");
        }
        finally {
            if (source != null) {
                source.close();
                System.out.println("src close");
            }
            if (destination != null) {
                destination.close();
                System.out.println("dst close");
            }
        }
    }

    public void refreshTabs() {
        tabList.removeAll(tabList);
        setTabsList();
        tAdapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
    }

    /**
     * This is method which sets the recyclerview content
     * @return Nothing.
     */

    private void setTabsList() {
        String songName;
        String songArtist;
        String songImage;

        File tabsDirectory = new File(externalDirectory);
        File[] files = tabsDirectory.listFiles();

        Arrays.sort(files, new Comparator() {
            public int compare(Object o1, Object o2) {
                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
                    return 1;
                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        if (files != null) {
            for (File file : files) {
                if (file.getName().contains(".txt")) {
                    try {
                        FileInputStream inputStream = new FileInputStream(new File(externalDirectory + "/" + file.getName()));
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                        songName = br.readLine();
                        songArtist = br.readLine();
                        songImage = br.readLine();
                        tabList.add(new Tab(songImage, songName, songArtist, file.getName()));

                        inputStream.close();
                    } catch (IOException e) {}
                }
            }

            recyclerView = (RecyclerView) findViewById(R.id.tabs_rv);

            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

            tAdapter = new TabAdapter(tabList);
            tAdapter.setHasStableIds(true);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(tAdapter);
        }
    }

    /**
     * This will use jsoup to scrape the text from one of the two sites: ukutabs.com/ukuleletabs.com
     *
     * @author Chirag Chaudhari
     *
     */
    private void scrapeText(String url, String strum, String chordsClass) {
        try {
            Document document = Jsoup.connect(url).get();

            final String songTitle;
            final String songArtist;
            if (url.contains("ukutabs")) {
                songTitle = document.select("div.inner-wrap > h3").text() + "\n";
                songArtist = document.select("span.entry-category > a").text() + "\n";
            } else {
                songTitle = document.getElementsByClass("uppcase").get(0).text() + "\n";
                songArtist = document.getElementsByClass("uppcase").get(1).text() + "\n";
            }

            final String songImage = document.getElementsByClass("sidebar_thumb").select("img").attr("src") + "\n";
            String songPattern = strum + "\n";

            String songChords = "";
            Elements chords = document.getElementsByClass(chordsClass);
            for (Element chord : chords) {
                if (url.contains("ukutabs")) {
                    songChords += chord.attr("name") + " ";
                } else {
                    songChords += (chord.attr("title").replace(" Ukulele chord", "")) + " ";
                }
            }
            songChords += "\n";

            String songTab = "";
            if (url.contains("ukutabs")) {
                Elements lines = document.getElementsByClass("qoate-code");
                for (Element tabLine : lines) {
                    songTab += tabLine.text() + "\n";
                }
            } else {
                Elements lines = document.getElementsByTag("pre");
                String linesReplaced = Jsoup.parse(lines.outerHtml().replaceAll("(?i)<br[^>]*>", "br2n")).text();
                songTab = linesReplaced.replaceAll("br2n", "\n");
            }

            final String fileName = songTitle.replace(" ", "_").replace("\n", "").replaceAll("(\\W|^_)*", "").toLowerCase() + ".txt";

            File tabsDirectory = new File(externalDirectory);
            String[] filesInDirectory = tabsDirectory.list();
            List<String> filesInDirectorList = Arrays.asList(filesInDirectory);

            if (!filesInDirectorList.contains(fileName)) {
                File file = new File (externalDirectory, fileName);
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(songTitle.getBytes());
                    outputStream.write(songArtist.getBytes());
                    outputStream.write(songImage.getBytes());
                    outputStream.write(songPattern.getBytes());
                    outputStream.write(songChords.getBytes());
                    outputStream.write(songTab.getBytes());
                    outputStream.close();
                } catch (IOException e) {}

                runOnUiThread(new Runnable() {
                    public void run() {
                        tAdapter.insertTab(new Tab(songImage, songTitle.replace("\n", ""), songArtist.replace("\n", ""), fileName), tabList.size());
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Tab has already been added", Toast.LENGTH_LONG).show();
                    }
                });
            }

        } catch (IOException e) {}
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_strings) {
            Intent i = new Intent(getBaseContext(), StringsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_chords) {
            Intent i = new Intent(getBaseContext(), ChordsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_strumming_patterns) {
            Intent i = new Intent(getBaseContext(), PatternsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_top_tabs) {
            Intent i = new Intent(getBaseContext(), TopTabsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_favorite_tabs) {
            Intent i = new Intent(getBaseContext(), FavoritesActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(getBaseContext(), SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_help_and_feedback) {
            Intent i = new Intent(getBaseContext(), HelpAndFeedbackActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    public class TabAdapter extends RecyclerView.Adapter<TabAdapter.MyViewHolder> {
        private ArrayList<Tab> tabList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private ImageView tabImage;
            private TextView tabName;
            private TextView tabArtist;

            public MyViewHolder(final View view) {
                super(view);
                tabImage = (ImageView) view.findViewById(R.id.tab_image);
                tabName = (TextView) view.findViewById(R.id.tab_name);
                tabArtist = (TextView) view.findViewById(R.id.tab_artist);
            }
        }

        public TabAdapter(ArrayList<Tab> tabList) {
            this.tabList = tabList;
        }

        @Override
        public TabAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_layout, parent, false);
            return new TabAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final TabAdapter.MyViewHolder holder, final int position) {
            if (!tabList.get(position).getTabImage().isEmpty()) {
                Glide.with(MainActivity.this)
                        .load(tabList.get(position).getTabImage())
                        .into(holder.tabImage);
            }

            holder.tabName.setText(tabList.get(position).getTabName());
            holder.tabArtist.setText(tabList.get(position).getTabArtist());


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File tabsDirectory = new File(externalDirectory);
                    List tabFiles = Arrays.asList(tabsDirectory.list());

                    if (tabFiles.contains(tabList.get(position).getTabFile())) {
                        Intent i = new Intent(getBaseContext(), TabActivity.class);
                        i.putExtra("songFileName", tabList.get(position).getTabFile());
                        i.putExtra("songImage", tabList.get(position).getTabImage());
                        startActivity(i);
                    } else {
                        Toast.makeText(MainActivity.this, "Tab no longer exists, please refresh", Toast.LENGTH_LONG).show();
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @SuppressLint("RestrictedApi")
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Are you sure you want to delete this tab?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File file = new File(externalDirectory + "/" + tabList.get(position).tabFile);
                            file.delete();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    removeAt(position);
                                }
                            });
                            Toast.makeText(MainActivity.this, "Tab has been deleted", Toast.LENGTH_LONG).show();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            if (tabList == null) {
                return 0;
            } else {
                return tabList.size();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public long getItemId(int position){
            Tab currentTab = tabList.get(position);
            return (currentTab.getTabName() + currentTab.getTabArtist()).hashCode();
        }

        private void insertTab(Tab newTab, int position) {
            tabList.add(newTab);
            notifyItemInserted(position);
            recyclerView.getRecycledViewPool().clear();
            recyclerView.smoothScrollToPosition(position);
        }

        private void removeAt(int position) {
            if (position == -1) {
                return;
            }
            tabList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, tabList.size());
            recyclerView.getRecycledViewPool().clear();
        }
    }


    private ArrayList<Integer> list;

    public void produce() throws InterruptedException {
        int value = 0;
        while (true) {
            synchronized (this) {
                while (list.size() == 1) {
                    wait();
                }

                System.out.println("Tab produced - " + value);

                list.add(value++);
                notify();
                Thread.sleep(1000);
            }
        }
    }
}