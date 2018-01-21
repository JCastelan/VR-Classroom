package com.jhacks.vrclassroom;


import android.Manifest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.io.IOException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


/*

Thanks to
https://code.tutsplus.com/tutorials
for tutorial code for VR
 */

public class StudSession extends AppCompatActivity implements  Session.SessionListener/*, PublisherKit.PublisherListener*/  {

    private static String API_KEY, SESSION_ID, TOKEN, NAME; // = "46043342";
    //private static String SESSION_ID = "2_MX40NjA0MzM0Mn5-MTUxNjQ3NzQxODk3Mn5TZnV5MS9kMm9OcUdJQVpCVG9UVmFYR25-fg";
    //private static String TOKEN = "T1==cGFydG5lcl9pZD00NjA0MzM0MiZzaWc9YTM5Zjk1YWM5MjAyZDllN2Q1ZWMwYzIyOGMwMGE3YmVmZWRmMDYzZTpzZXNzaW9uX2lkPTJfTVg0ME5qQTBNek0wTW41LU1UVXhOalEzTnpReE9EazNNbjVUWm5WNU1TOWtNbTlPY1VkSlFWcENWRzlVVm1GWVIyNS1mZyZjcmVhdGVfdGltZT0xNTE2NDc3NTU1Jm5vbmNlPTAuNTIxMjg4MzcyNDM4NTM5MyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTE5MDY5NTU0JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
   // private FrameLayout mSubscriberViewContainer;
    private Subscriber mSubscriber;
    private VrVideoView mVrVideoView;
    private boolean mIsPaused;
    private Uri mUri;
    private Uri.Builder mUriBuilder;


    public void fetchSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://vr-classroom.herokuapp.com" + "/room/" + NAME,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {

                    API_KEY = response.getString("apiKey");
                    SESSION_ID = response.getString("sessionId");
                    TOKEN = response.getString("token");

                    Log.i(LOG_TAG, "API_KEY: " + API_KEY);
                    Log.i(LOG_TAG, "SESSION_ID: " + SESSION_ID);
                    Log.i(LOG_TAG, "TOKEN: " + TOKEN);
                    Log.i(LOG_TAG, "NAME: " + NAME);

                   // mUriBuilder = new Uri.Builder();
                    VideoLoaderTask mBackgroundVideoLoaderTask = new VideoLoaderTask();
                    mBackgroundVideoLoaderTask.execute();
                   // mSession = new Session.Builder(StudSession.this, API_KEY, SESSION_ID).build();
                    //mSession.setSessionListener(StudSession.this);
                  //  mSession.connect(TOKEN);

                } catch (JSONException error) {
                    Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
            }
        }));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stud_session);

        Bundle extras = getIntent().getExtras();
        NAME = extras.getString("studSesId");
        mUri.parse(NAME);
        requestPermissions();


    }

    private static final String STATE_PROGRESS = "state_progress";
    private static final String STATE_DURATION = "state_duration";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(STATE_PROGRESS, mVrVideoView.getCurrentPosition());
        outState.putLong(STATE_DURATION, mVrVideoView.getDuration());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progress = savedInstanceState.getLong(STATE_PROGRESS);

        mVrVideoView.seekTo(progress);
    }

    private class ActivityEventListener extends VrVideoEventListener {
        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();
            mIsPaused = false;
        }

        @Override
        public void onLoadError(String errorMessage) {
            super.onLoadError(errorMessage);
        }

        @Override
        public void onClick() {
            super.onClick();
        }

        @Override
        public void onNewFrame() {
            super.onNewFrame();
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            //Restart the video, allowing it to loop
            mVrVideoView.seekTo(0);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        mVrVideoView.pauseRendering();
        mIsPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVrVideoView.resumeRendering();
        mIsPaused = false;
    }

    @Override
    protected void onDestroy() {
        mVrVideoView.shutdown();
        super.onDestroy();
    }




    class VideoLoaderTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                VrVideoView.Options options = new VrVideoView.Options();
                options.inputType = VrVideoView.Options.TYPE_MONO;
                mVrVideoView.loadVideo(mUri, options);
            } catch( IOException e ) {
                //Handle exception
            }

            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initialize view objects from your layout
    //        mSubscriberViewContainer = findViewById(R.id.subscriber_container);
            mVrVideoView = (VrVideoView) findViewById(R.id.video_view);
            mVrVideoView.setEventListener(new ActivityEventListener());

            // initialize and connect to the session
            fetchSessionConnectionData();

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    /*@AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            mSubscriberViewContainer = findViewById(R.id.subscriber_container);

            // initialize and connect to the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }*/

    // SessionListener methods

    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }






    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            Toast.makeText(this, "You are now connected", Toast.LENGTH_SHORT).show();
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
           // mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }



    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
          //  mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }

    // Back disconnects session
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Disconnected from Session", Toast.LENGTH_SHORT).show();
        mSession.disconnect();
        finish();
    }
}

