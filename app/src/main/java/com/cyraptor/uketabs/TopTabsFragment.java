package com.cyraptor.uketabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class TopTabsFragment extends Fragment {

    int position;
    private ViewPager viewPager;
    private TopTabsAdapter topTabsAdapter;
    private ArrayList<Tab> topTabsList = new ArrayList<Tab>();

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        TopTabsFragment topTabsFragment = new TopTabsFragment();
        topTabsFragment.setArguments(bundle);
        return topTabsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_tabs, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
        setTab(view);
    }

    private void setTab(View view) {
        if (viewPager.getAdapter().getPageTitle(position).equals("Ukutabs")) {
            try {
                Document document = Jsoup.connect("https://ukutabs.com/top-tabs/99-most-popular-ukulele-songs/all-time/").get();
                Elements posts = document.getElementsByClass("tptn_posts").select("ol").select("li");

                for (Element post : posts) {
                    String songImage = post.select("a").get(1).select("img").attr("src")
                            .replace("-50x50", "")
                            .replace("-54x54", "")
                            .replace("-144x144", "")
                            .replace("-150x150", "");
                    String songName = post.select("a").get(1).select("img").attr("title");
                    int rightArrowIndex = post.select("a").get(0).select("span").text().indexOf("»");
                    String songArtist = post.select("a").get(0).select("span").text().substring(0, rightArrowIndex - 1);
                    String songLink = post.select("a").get(0).attr("href");

                    topTabsList.add(new Tab(songImage, songName, songArtist, songLink));
                }
            } catch (IOException e) {}
        } else {
            try {
                Document document = Jsoup.connect("https://www.ukulele-tabs.com/famous-ukulele-songs.html").get();
                Elements posts = document.getElementsByTag("tbody").select("tr");

                String songImage = "";
                for (Element post : posts) {
                    String songName = post.select("td").get(1).text();
                    String songArtist = post.select("td").get(2).text();
                    String songLink = post.select("td").get(1).select("a").attr("abs:href");

                    topTabsList.add(new Tab(songImage, songName, songArtist, songLink));
                }
            } catch (IOException e) {}
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.top_tabs_rv);
        rv.setHasFixedSize(true);

        TopTabsAdapter adapter = new TopTabsAdapter(topTabsList);
        rv.setAdapter(adapter);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);
    }

    public class TopTabsAdapter extends RecyclerView.Adapter<TopTabsAdapter.MyViewHolder> {
        private ArrayList<Tab> topTabsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageView tabImage;
            public TextView tabName;
            public TextView tabArtist;

            public MyViewHolder(final View view) {
                super(view);
                tabImage = (ImageView) view.findViewById(R.id.tab_image);
                tabName = (TextView) view.findViewById(R.id.tab_name);
                tabArtist = (TextView) view.findViewById(R.id.tab_artist);
            }
        }

        public TopTabsAdapter(ArrayList<Tab> topTabsList) {
            this.topTabsList = topTabsList;
        }

        @Override
        public TopTabsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_layout, parent, false);
            return new TopTabsAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            if (!topTabsList.get(position).getTabImage().isEmpty()) {
                Glide.with(TopTabsFragment.this)
                        .load(topTabsList.get(position).getTabImage())
                        .into(holder.tabImage);
            }

            holder.tabName.setText(topTabsList.get(position).getTabName());
            holder.tabArtist.setText(topTabsList.get(position).getTabArtist());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), TabActivity.class);
                    i.putExtra("songImage", topTabsList.get(position).getTabImage());
                    i.putExtra("songName", topTabsList.get(position).getTabName());
                    i.putExtra("songArtist", topTabsList.get(position).getTabArtist());
                    i.putExtra("songFileName", topTabsList.get(position).getTabFile());
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return topTabsList.size();
        }
    }
}
