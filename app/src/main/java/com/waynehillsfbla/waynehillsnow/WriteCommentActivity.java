package com.waynehillsfbla.waynehillsnow;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.RequestParams;

//TODO add formatting functionality
public class WriteCommentActivity extends ActionBarActivity {

    String userId;
    String eventId;
    String commentBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_comment);
        eventId = getIntent().getStringExtra("eventId");
        userId = getIntent().getStringExtra("userId");


        final EditText comment = (EditText) findViewById(R.id.commentText);

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentBody = comment.getText().toString();

                publishComment();

                finish();
            }
        });
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

    private void publishComment() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", eventId);
        requestParams.put("userId", userId);
        requestParams.put("commentBody", commentBody);
        ClientServerInterface.post("publish_comment.php", requestParams, null);
    }
}
