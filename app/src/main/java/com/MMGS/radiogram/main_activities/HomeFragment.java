package com.MMGS.radiogram.main_activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.News;
import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends ListFragment
{
    private Activity activity;
    private ProgressDialog mProgressDialog;

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        SetViews();
        ParseTask mSetNews = new ParseTask();
        mSetNews.execute();
        return view;
    }
    private void SetViews() {

        activity = getActivity();
        mProgressDialog = new ProgressDialog(activity);
    }
    private void CreateDialog(ArrayList<String> texts,String title, String uri){
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setPositiveButton("ЯСНЕНЬКО",null);
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams")
        View view = layoutInflater.inflate(R.layout.dialog_news, null);
        TextView textView = view.findViewById(R.id.dlg_news_text);
        ImageView srcView = view.findViewById(R.id.dlg_news_src);
        StringBuilder text = new StringBuilder();
        for(String t : texts)
            text.append(t).append("\n");
        if(text.toString().equals(""))
            text = new StringBuilder(title);
        textView.setText(text.toString());
        Glide.with(activity)
                .load(uri)
                .into(srcView);
        dialog.setView(view);
        dialog.create().show();
    }
    @SuppressLint("StaticFieldLeak")
    private class ParseTextTask extends AsyncTask<News, Void, ArrayList<String>>{

        private String uriOfSrc = "";
        private String title = "";

        @Override
        protected ArrayList<String> doInBackground(News... news) {

            ArrayList<String> texts = new ArrayList<>();
            uriOfSrc = news[0].getSrcUri();
            title = news[0].getTitle();
            try {
                Document document = Jsoup.connect(news[0].getUri()).get();
                Elements elements = document.select("div.field-item");
                elements = elements.first().select("p");
                if(elements.size()==0){
                   Element element = document.select("div.breadcrumb a").last();
                   texts.add(element.text());
                }else
                for (Element element : elements)
                    texts.add(element.text());
            } catch (IOException ignored) {
            }
            return texts;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Загрузка...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            CreateDialog(strings,title, uriOfSrc);
            mProgressDialog.dismiss();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class ParseTask extends AsyncTask<Void, Void, ArrayList<News>> {

        @Override
        protected ArrayList<News> doInBackground(Void... path) {
            ArrayList<News> news = new ArrayList<>();
            try {
                Document document = Jsoup.connect("http://mrk-bsuir.by/").get();
                Elements imgs = document.select("div.field-content img");
                Elements texts = document.select("ul.tab2 li.info div.news span.title");
                for (int i = 0; i<imgs.size();i++){
                    String uri = texts.get(i).select("a[href]").first().attr("abs:href");
                    String title = texts.get(i).select("a").first().text();
                    String imgUri = imgs.get(i).attr("src");
                    news.add(new News(uri,title,imgUri));
                }
            } catch (IOException ignored) {
            }

            return news;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<News> news) {
            super.onPostExecute(news);
            NewsAdapter mAdapter = new NewsAdapter(activity, news);
            setListAdapter(mAdapter);

        }
    }
    public class NewsAdapter extends BaseAdapter {

        Context c;
        ArrayList<News> listOfNews;
        LayoutInflater inflater;

        NewsAdapter(Context c, ArrayList<News> news) {
            this.c = c;
            this.listOfNews = news;
            this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return listOfNews.size();
        }
        @Override
        public News getItem(int position) {
            return listOfNews.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.news, parent, false);
            }

            ImageView image = view.findViewById(R.id.news_image);
            TextView title = view.findViewById(R.id.news_title);
            CardView card = view.findViewById(R.id.news_card);

            title.setText(getItem(position).getTitle());
            card.setOnClickListener(v -> {
                ParseTextTask textTask = new ParseTextTask();
                textTask.execute(getItem(position));
            });
            Glide.with(activity)
                    .load(getItem(position).getSrcUri())
                    .into(image);
            return view;
        }
    }
}
