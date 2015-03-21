package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;


public class WriteCommentActivity extends ActionBarActivity {

    JSONObject commentDetails = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_comment);
        try {
            final JSONObject userEventData = new JSONObject(getIntent().getStringExtra("userEventDataJSON"));
            commentDetails.put("googleId", userEventData.getString("googleId"));
            commentDetails.put("eventId", userEventData.getString("eventId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final EditText comment = (EditText) findViewById(R.id.commentText);

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentBody = comment.getText().toString();

                try {
                    commentDetails.put("commentBody", commentBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PublishComment publishComment = new PublishComment();
                publishComment.execute(commentDetails);

                //goBack();


            }
        });


    }

    private void goBack() {
        Intent intent = new Intent(this, DetailedEventActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write_comment, menu);
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

    class PublishComment extends AsyncTask<JSONObject, Void, Void> {
        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[0];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            clientServerInterface.postData("http://54.164.136.46/publish_comment.php", jsonObject);
            Log.e("Sent to publish comment", jsonObject.toString());
            return null;
        }
    }
}
