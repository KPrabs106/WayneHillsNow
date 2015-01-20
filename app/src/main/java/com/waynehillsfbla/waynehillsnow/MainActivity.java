package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends ActionBarActivity {

    ImageView scroller;
    AnimationDrawable anim;
    JSONArray jarr;
    ClientServerInterface clientServerInterface = new ClientServerInterface();
    Bitmap[] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton listButton = (ImageButton) findViewById(R.id.buttonList);
        ImageButton calendarButton = (ImageButton) findViewById(R.id.buttonCalendar);

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CardListActivity.class);
                startActivity(intent);
            }
        });

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CalendarActivity.class);
                startActivity(intent);
            }
        });

        RetrievePictures rp = new RetrievePictures();
        rp.execute();
        try {
            rp.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        scroller = (ImageView) findViewById(R.id.imageScroller);
        //ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        anim = new AnimationDrawable();

        for(int i = 0; i < images.length; i++) {
            anim.addFrame((new BitmapDrawable(getResources(), images[i])), 2000);
        }

        scroller.setBackgroundDrawable(anim);
        anim.setOneShot(false);
        scroller.setVisibility(View.VISIBLE);
        //progressBar.setVisibility(View.INVISIBLE);
        anim.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class RetrievePictures extends AsyncTask<String, String, Void> {
        protected Void doInBackground(String... arg0) {

            jarr = clientServerInterface.makeHttpRequest("http://54.164.136.46/get_pictures.php");

            images = new Bitmap[jarr.length()];
            String url = "";

            for (int i = 0; i < jarr.length(); i++) {
                try {
                    url = jarr.getJSONObject(i).getString("pictureURL");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    images[i] = BitmapFactory.decodeStream(new URL(url).openStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


}
