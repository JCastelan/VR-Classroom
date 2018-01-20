package com.jhacks.vrclassroom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class StudSession extends AppCompatActivity {

    String sesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stud_session);

        // Get session ID from other activity
        Bundle extras = getIntent().getExtras();
        sesId = extras.getString("sesId");

    }
}
