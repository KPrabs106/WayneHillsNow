package com.waynehillsfbla.waynehillsnow;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class ViewImageActivity extends AppCompatActivity {

    String pictureURL;
    TouchImageView eventPic;
    Toolbar toolbar;
    String eventName;;

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        eventPic = (TouchImageView) findViewById(R.id.eventImage);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        final Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        pictureURL = extras.getString("pictureURL");
        eventName = extras.getString("eventName");
        Picasso.with(getApplicationContext()).load(pictureURL).into(eventPic);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_event_image, menu);

        /*MenuItem item = menu.findItem(R.id.menu_item_share);
        MenuItemCompat.getActionProvider(item);
        mShareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(item, mShareActionProvider);*/
        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_download) {
            downloadFromUrl(pictureURL, eventName);
            return true;
        }
        else if(id == R.id.menu_item_share) {
            shareImage(pictureURL);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareImage(String imageURL) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Bitmap loadedImage = getBitmapFromURL(imageURL);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), loadedImage, "", null);
        Uri screenshotUri = Uri.parse(path);

        intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share image via..."));

    }

    private void downloadFromUrl(String imageURL, String fileName)  {  //this is the downloader method
        File direct = new File(Environment.getExternalStorageDirectory() + "/WayneHillsNow");

        if(!direct.exists())
            direct.mkdirs();

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(imageURL);

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                |   DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(eventName)
                .setDescription("Cover image for " + eventName)
                .setDestinationInExternalPublicDir("/WayneHillsNow", fileName + ".jpg");

        downloadManager.enqueue(request);
        Toast.makeText(getApplicationContext(), "Image Downloaded", Toast.LENGTH_SHORT).show();

    }

}
