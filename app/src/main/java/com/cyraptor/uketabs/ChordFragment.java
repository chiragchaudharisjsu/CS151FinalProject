package com.cyraptor.uketabs;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ChordFragment extends Fragment {

    int position;
    private ViewPager viewPager;
    private ImageView chord1, chord2, chord3, chord4, chord5, chord6, chord7, chord8, chord9, chord10, chord11, chord12, chord13, chord14, chord15;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        ChordFragment chordFragment = new ChordFragment();
        chordFragment.setArguments(bundle);
        return chordFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chord, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
        setImages(view);
    }

    private Bitmap getBitmapFromAssets(String fileName){
        AssetManager assetManager = getContext().getAssets();
        InputStream inputStream = null;

        try {
            inputStream = assetManager.open("chords/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    private void setImages(View view) {
        chord1 = (ImageView) view.findViewById(R.id.chord_1);
        chord1.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + ".png"));
        chord1.getLayoutParams().height = 500;
        chord1.getLayoutParams().width = 320;

        chord2 = (ImageView) view.findViewById(R.id.chord_2);
        chord2.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "m.png"));
        chord2.getLayoutParams().height = 500;
        chord2.getLayoutParams().width = 320;

        chord3 = (ImageView) view.findViewById(R.id.chord_3);
        chord3.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "aug.png"));
        chord3.getLayoutParams().height = 500;
        chord3.getLayoutParams().width = 320;

        chord4 = (ImageView) view.findViewById(R.id.chord_4);
        chord4.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "dim.png"));
        chord4.getLayoutParams().height = 500;
        chord4.getLayoutParams().width = 320;

        chord5 = (ImageView) view.findViewById(R.id.chord_5);
        chord5.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "7.png"));
        chord5.getLayoutParams().height = 500;
        chord5.getLayoutParams().width = 320;

        chord6 = (ImageView) view.findViewById(R.id.chord_6);
        chord6.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "m7.png"));
        chord6.getLayoutParams().height = 500;
        chord6.getLayoutParams().width = 320;

        chord7 = (ImageView) view.findViewById(R.id.chord_7);
        chord7.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "maj7.png"));
        chord7.getLayoutParams().height = 500;
        chord7.getLayoutParams().width = 320;

        chord8 = (ImageView) view.findViewById(R.id.chord_8);
        chord8.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "6.png"));
        chord8.getLayoutParams().height = 500;
        chord8.getLayoutParams().width = 320;

        chord9 = (ImageView) view.findViewById(R.id.chord_9);
        chord9.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "m6.png"));
        chord9.getLayoutParams().height = 500;
        chord9.getLayoutParams().width = 320;

        chord10 = (ImageView) view.findViewById(R.id.chord_10);
        chord10.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "add9.png"));
        chord10.getLayoutParams().height = 500;
        chord10.getLayoutParams().width = 320;

        chord11 = (ImageView) view.findViewById(R.id.chord_11);
        chord11.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "m9.png"));
        chord11.getLayoutParams().height = 500;
        chord11.getLayoutParams().width = 320;

        chord12 = (ImageView) view.findViewById(R.id.chord_12);
        chord12.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "9.png"));
        chord12.getLayoutParams().height = 500;
        chord12.getLayoutParams().width = 320;

        chord13 = (ImageView) view.findViewById(R.id.chord_13);
        chord13.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "sus2.png"));
        chord13.getLayoutParams().height = 500;
        chord13.getLayoutParams().width = 320;

        chord14 = (ImageView) view.findViewById(R.id.chord_14);
        chord14.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "sus4.png"));
        chord14.getLayoutParams().height = 500;
        chord14.getLayoutParams().width = 320;

        chord15 = (ImageView) view.findViewById(R.id.chord_15);
        chord15.setImageBitmap(getBitmapFromAssets("chord_" + viewPager.getAdapter().getPageTitle(position).toString().toLowerCase() + "7sus4.png"));
        chord15.getLayoutParams().height = 500;
        chord15.getLayoutParams().width = 320;
    }
}