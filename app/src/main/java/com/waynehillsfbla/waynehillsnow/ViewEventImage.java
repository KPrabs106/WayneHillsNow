package com.waynehillsfbla.waynehillsnow;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Path;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class ViewEventImage extends ActionBarActivity {

    String pictureURL;
    ImageView eventPic;
    Toolbar toolbar;
    String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image);

        eventPic = (ImageView) findViewById(R.id.eventImage);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_download) {
            DownloadFromUrl(pictureURL, eventName);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void DownloadFromUrl(String imageURL, String fileName)  {  //this is the downloader method
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
        try{
            wait(999);
        }
        catch (InterruptedException i){
            Log.getStackTraceString(i);
        }
        Toast.makeText(getApplicationContext(), "Image Downloaded", Toast.LENGTH_SHORT).show();

    }
}
