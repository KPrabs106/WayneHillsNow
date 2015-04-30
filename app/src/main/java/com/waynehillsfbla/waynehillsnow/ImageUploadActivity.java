package com.waynehillsfbla.waynehillsnow;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ImageUploadActivity extends AppCompatActivity {

    private final int SELECT_PHOTO = 1;
    private final String IMGUR_CLIENT_ID = "1f141501d65d7d7";
    private ImageView imagePreview;
    private Button uploadButton;
    private InputStream imageStream;
    private String[] eventNames;
    private HashMap events;
    private String imageURL;

    private Uri uploadFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getPastEvents();

        imagePreview = (ImageView) findViewById(R.id.imagePreview);
        Button chooseButton = (Button) findViewById(R.id.chooseButton);
        uploadButton = (Button) findViewById(R.id.uploadButton);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    openImageIntent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /*
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                */
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void openImageIntent() throws IOException {
        final File root = new File(Environment.getExternalStorageDirectory() +
                File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fileName = File.createTempFile("tmp", ".txt").getPath();
        final File sdImageMainDirectory = new File(root, fileName);

        uploadFileUri = Uri.fromFile(sdImageMainDirectory);

        //Camera
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : resolveInfoList) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uploadFileUri);
            cameraIntents.add(intent);
        }

        //Filesystem
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, SELECT_PHOTO);
    }

    private void uploadImage() {
        Bitmap bitmap = ((BitmapDrawable) imagePreview.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        String base64Image = Base64.encodeToString(b, Base64.DEFAULT);
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID);

        RequestParams requestParams = new RequestParams();
        requestParams.add("image", base64Image);

        client.post("https://api.imgur.com/3/image", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    imageURL = response.getJSONObject("data").getString("link");
                    showDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void logImage(String eventTitle) {
        RequestParams requestParams = new RequestParams();
        requestParams.add("imageURL", imageURL);
        requestParams.add("eventId", String.valueOf(events.get(eventTitle)));
        requestParams.add("googleId", getSharedPreferences("userDetails", MODE_PRIVATE).getString("googleId", null));
        ClientServerInterface.post("log_image.php", requestParams, new JsonHttpResponseHandler());
        Toast.makeText(getApplicationContext(), "Image Uploaded.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Select Event");
        b.setItems(eventNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                logImage(eventNames[which]);
            }
        });
        b.show();
    }

    private void getPastEvents() {
        ClientServerInterface.get("get_past_events.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                events = new HashMap<String, Integer>();
                eventNames = new String[response.length()];
                for (int i = 0; i < response.length(); i++) {
                    try {
                        events.put(response.getJSONObject(i).getString("title"), response.getJSONObject(i).getInt("id"));
                        eventNames[i] = response.getJSONObject(i).getString("title");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageIntent) {
        super.onActivityResult(requestCode, resultCode, imageIntent);

                if (resultCode == RESULT_OK) {
                    if(requestCode == SELECT_PHOTO){
                    final boolean isCamera;
                    if (imageIntent == null) {
                        isCamera = true;
                    } else {
                        final String action = imageIntent.getAction();
                        if (action == null) {
                            isCamera = false;
                        } else {
                            isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                        }
                    }

                    Uri selectedImageUri;
                    if (isCamera) {
                        selectedImageUri = uploadFileUri;
                    } else {
                        selectedImageUri = imageIntent == null ? null : imageIntent.getData();
                    }
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imagePreview.setImageBitmap(selectedImage);
                        uploadButton.setEnabled(true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_upload, menu);
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
}
