package com.jhacks.vrclassroom;


import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class StudSession extends AppCompatActivity implements  Session.SessionListener/*, PublisherKit.PublisherListener*/  {

    private static String API_KEY = "46043342";
    private static String SESSION_ID = "2_MX40NjA0MzM0Mn5-MTUxNjQ3NzQxODk3Mn5TZnV5MS9kMm9OcUdJQVpCVG9UVmFYR25-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjA0MzM0MiZzaWc9YTM5Zjk1YWM5MjAyZDllN2Q1ZWMwYzIyOGMwMGE3YmVmZWRmMDYzZTpzZXNzaW9uX2lkPTJfTVg0ME5qQTBNek0wTW41LU1UVXhOalEzTnpReE9EazNNbjVUWm5WNU1TOWtNbTlPY1VkSlFWcENWRzlVVm1GWVIyNS1mZyZjcmVhdGVfdGltZT0xNTE2NDc3NTU1Jm5vbmNlPTAuNTIxMjg4MzcyNDM4NTM5MyZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTE5MDY5NTU0JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private FrameLayout mSubscriberViewContainer;
    private Subscriber mSubscriber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stud_session);

        requestPermissions();
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
            mSubscriberViewContainer = findViewById(R.id.subscriber_container);

            // initialize and connect to the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

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
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
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