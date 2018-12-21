package com.cyraptor.uketabs;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.io.IOException;

public class StringsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    public ImageButton playG, playC, playE, playA;
    private MediaPlayer mediaPlayer;
    boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Strings");

        playG = (ImageButton) findViewById(R.id.play_g);
        playC = (ImageButton) findViewById(R.id.play_c);
        playE = (ImageButton) findViewById(R.id.play_e);
        playA = (ImageButton) findViewById(R.id.play_a);

        playG.setSoundEffectsEnabled(false);
        playC.setSoundEffectsEnabled(false);
        playE.setSoundEffectsEnabled(false);
        playA.setSoundEffectsEnabled(false);
    }

    public void playG(View view) {
        if (isPlaying) {
            if (playG.getTag().equals("playingG")) {
                stopSound(playG, playC, playE, playA);
            } else {
                playSound(view, playG, "string_G.mp3");
            }
            playG.setTag("stoppedG");
        } else {
            playSound(view, playG, "string_G.mp3");
            playG.setTag("playingG");
        }
        isPlaying = !isPlaying;
    }

    public void playC(View view) {
        if (isPlaying) {
            if (playC.getTag().equals("playingC")) {
                stopSound(playC, playE, playA, playG);
            } else {
                playSound(view, playC, "string_C.mp3");
            }
            playC.setTag("stoppedC");
        } else {
            playSound(view, playC, "string_C.mp3");
            playC.setTag("playingC");
        }
        isPlaying = !isPlaying;
    }

    public void playE(View view) {
        if (isPlaying) {
            if (playE.getTag().equals("playingE")) {
                stopSound(playE, playG, playC, playA);
            } else {
                playSound(view, playE, "string_E.mp3");
            }
            playE.setTag("stoppedE");
        } else {
            playSound(view, playE, "string_E.mp3");
            playE.setTag("playingE");
        }
        isPlaying = !isPlaying;
    }

    public void playA(View view) {
        if (isPlaying) {
            if (playA.getTag().equals("playingA")) {
                stopSound(playA, playG, playC, playE);
            } else {
                playSound(view, playA, "string_A.mp3");
            }
            playA.setTag("stoppedA");
        } else {
            playSound(view, playA, "string_A.mp3");
            playA.setTag("playingA");
        }
        isPlaying = !isPlaying;
    }

    private void playSound(View view, final ImageButton playButton, String stringName) {
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = view.getContext().getAssets().openFd("strings/" + stringName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
            playButton.setImageResource(R.drawable.pause_circle);
        }
        catch (IOException e) {}
    }

    private void stopSound(ImageButton playButtonMain, ImageButton playButton2, ImageButton playButton3, ImageButton playButton4) {
        mediaPlayer.stop();
        playButtonMain.setImageResource(R.drawable.play_circle);
        playButton2.setImageResource(R.drawable.play_circle);
        playButton3.setImageResource(R.drawable.play_circle);
        playButton4.setImageResource(R.drawable.play_circle);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (isPlaying) {
            mediaPlayer.release();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        if(isPlaying) {
            mediaPlayer.release();
        }
    }
}