package com.MMGS.radiogram.else_activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.MMGS.radiogram.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class BrowserActivity extends AppCompatActivity {

    private com.github.barteksc.pdfviewer.PDFView bLayout;
    private String bURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        bLayout = findViewById(R.id.pdfView);
        bURI = getIntent().getStringExtra("uri");
        Toolbar toolbar = findViewById(R.id.toolbar_table);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        new DownloadFile().execute(bURI);

    }

    @SuppressLint("StaticFieldLeak")
    public class DownloadFile extends AsyncTask<String, Void, InputStream> {
        @Override
        protected void onPostExecute(InputStream inputStream) {
            super.onPostExecute(inputStream);
            bLayout.fromStream(inputStream).load();
        }

        @Override
        protected InputStream doInBackground(String... strings) {
            String fileUrl = strings[0];
            InputStream inputStream = null;
            try {
                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputStream;
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }
}


