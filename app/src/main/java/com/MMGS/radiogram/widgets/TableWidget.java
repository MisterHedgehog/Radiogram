package com.MMGS.radiogram.widgets;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.MMGS.radiogram.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TableWidget extends AppWidgetProvider {

    final static int EXTRA_IMAGE = 1;
    final static int EXTRA_BUTTON = 2;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        try {
            Intent intent = new Intent(context, TableWidget.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            int page = getPageNumber(context, appWidgetId);
            if (page >= 0) {
                views.setTextViewText(R.id.count, String.valueOf(page + 1));
                FileInputStream stream = context.openFileInput("table_bitmap_"
                        + String.valueOf(page)+".png");
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                views.setImageViewBitmap(R.id.image, bitmap);
            }
            intent.setAction("lol");
            intent.putExtra("mode", EXTRA_BUTTON);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
            views.setOnClickPendingIntent(R.id.button_update, pendingIntent);

            intent = new Intent(context, TableWidget.class);
            intent.setAction("kek");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra("mode", EXTRA_IMAGE);
            pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
            views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e){
            Toast.makeText(context, "Error: Ошибка при обновлении виджета\n" +
                    " Java: TableWidget, updateAppWidget\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        switch (intent.getIntExtra("mode", 0)){
            case EXTRA_BUTTON:
                Toast.makeText(context, "Ожидайте, загрузка...", Toast.LENGTH_LONG).show();
                new TableBitmapDownloader(context, appWidgetManager, appWidgetId).execute();
                return;
            case EXTRA_IMAGE:
                //Toast.makeText(context, "image", Toast.LENGTH_SHORT).show();
                incPageNumber(context,appWidgetId);
                break;
        }
        if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)){
            setPageNumber(context, appWidgetId, -1);
        }
        updateAppWidget(context,appWidgetManager, appWidgetId);
    }
    static void setPageNumber(Context context, int id, int number){
        context.getSharedPreferences("widget_manager_" + String.valueOf(id), Context.MODE_PRIVATE)
                .edit()
                .putInt("page", number)
                .apply();
    }
    void incPageNumber(Context context, int id){
        SharedPreferences memory = context.getSharedPreferences("widget_manager_" + String.valueOf(id), Context.MODE_PRIVATE);
        int page = memory.getInt("page", -1);
        if(page >= (getPageCount(context, id) - 1)){
           page = -1;
        }
        memory.edit().putInt("page", page + 1).apply();
    }
    static int getPageNumber(Context context, int id){
        return  context.getSharedPreferences("widget_manager_" + String.valueOf(id), Context.MODE_PRIVATE)
                .getInt("page", -1);
    }
    static int getPageCount(Context context, int id){
        return  context.getSharedPreferences("widget_manager_" + String.valueOf(id), Context.MODE_PRIVATE)
                .getInt("page_max", -1);
    }
    static void setPageCount(Context context, int id, int count){
        context.getSharedPreferences("widget_manager_" + String.valueOf(id), Context.MODE_PRIVATE)
                .edit()
                .putInt("page_max", count)
                .apply();
    }
    @SuppressLint("StaticFieldLeak")
    public static class TableBitmapDownloader extends AsyncTask<Void, Void, ArrayList<Bitmap>> {
        Context context;
        AppWidgetManager appWidgetManager;
        int appWidgetId;

        public TableBitmapDownloader(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
            this.context = context;
            this.appWidgetManager = appWidgetManager;
            this.appWidgetId = appWidgetId;
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... voids) {
            String uri = " ";
            try {
                Document document = Jsoup.connect("http://mrk-bsuir.by/").get();
                Element rasp = document.getElementsByAttributeValue("id", "blockSidebar").first().selectFirst("div.block-content p");
                uri = rasp.select("a[href]").first().attr("abs:href");
            } catch (IOException e) { }
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            InputStream inputStream;
            try {
                URL fileUrl = new URL(uri);
                HttpURLConnection urlConnection = (HttpURLConnection) fileUrl.openConnection();
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();

                File folder = context.getDir("pdf_files", Context.MODE_PRIVATE);
                folder.mkdir();
                File pdfFile = new File(folder, "table.pdf");

                FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
                byte[] buffer = new byte[1024 * 1024];
                int bufferLength;
                while((bufferLength = inputStream.read(buffer)) > 0 ){
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
                fileOutputStream.close();

                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
                Bitmap bitmap;
                final int pageCount = renderer.getPageCount();
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    int width = page.getWidth();
                    int height = page.getHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    bitmaps.add(bitmap);
                    // close the page
                    page.close();
                }
                // close the renderer
                renderer.close();
            } catch (Exception e){ }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            try {
                super.onPostExecute(bitmaps);
                //Toast.makeText(context, "страниц " + String.valueOf(bitmaps.size()), Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Расписание загружено!", Toast.LENGTH_LONG).show();
                for(int i = 0; i < bitmaps.size();i++){
                    Bitmap bitmap = bitmaps.get(i);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data = baos.toByteArray();
                    FileOutputStream stream = context.openFileOutput("table_bitmap_"
                            + String.valueOf(i)+".png", Context.MODE_PRIVATE);
                    stream.write(data);
                    stream.close();
                }
                setPageCount(context, appWidgetId, bitmaps.size());
                setPageNumber(context, appWidgetId, 0);
                updateAppWidget(context,appWidgetManager, appWidgetId);
            }catch (Exception e){
            }
        }
    }
}


