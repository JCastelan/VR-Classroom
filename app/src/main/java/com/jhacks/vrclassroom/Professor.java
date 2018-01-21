package com.jhacks.vrclassroom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Professor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor);

        Button startSes = findViewById(R.id.startSes);
        startSes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startSession = new Intent(Professor.this, ProfSession.class);
                startActivity(startSession);
            }
        });
    }
}
