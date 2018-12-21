package com.cyraptor.uketabs;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class PatternsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ArrayList<String> patternList = new ArrayList<String>();
    private RecyclerView recyclerView;
    private PatternAdapter pAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patterns);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Strumming patterns");

        recyclerView = (RecyclerView) findViewById(R.id.strumming_patterns_rv);
        recyclerView.setHasFixedSize(true);

        pAdapter = new PatternAdapter(patternList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(pAdapter);

        addSongsToList();
    }

    private void addSongsToList() {
        try {
            String[] patternArray = getAssets().list("patterns");
            for (int i = 0; i <= patternArray.length - 1; i++) {
                patternList.add(patternArray[i]);
            }
        }
        catch (IOException e) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public class PatternAdapter extends RecyclerView.Adapter<PatternAdapter.MyViewHolder> {
        private ArrayList<String> patternList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView patternName;
            public ImageButton playPauseButton;
            private MediaPlayer mediaPlayer = new MediaPlayer();

            public MyViewHolder(final View view) {
                super(view);
                patternName = (TextView) view.findViewById(R.id.pattern_name);
                playPauseButton = (ImageButton) view.findViewById(R.id.play_pause_button);

                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            if (mediaPlayer != null){
                                stopSound();
                            }
                        } else {
                            if (mediaPlayer != null) {
                                playSound(view);
                            }
                        }
                    }
                });
            }

            private void playSound(View view) {
                mediaPlayer = new MediaPlayer();
                try {
                    AssetFileDescriptor afd = view.getContext().getAssets().openFd("patterns/" + patternList.get(getAdapterPosition()));
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playPauseButton.setImageResource(R.drawable.pause);
                }
                catch (IOException e) {}

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        playPauseButton.setImageResource(R.drawable.play);
                    }
                });
            }

            private void stopSound() {
                mediaPlayer.stop();
                playPauseButton.setImageResource(R.drawable.play);
            }
        }

        public PatternAdapter(ArrayList<String> patternList) {
            this.patternList = patternList;
            setHasStableIds(true);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pattern_layout, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.patternName.setText(patternList.get(position).replace(".ogg", ""));
        }

        @Override
        public int getItemCount() {
            return patternList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }
}