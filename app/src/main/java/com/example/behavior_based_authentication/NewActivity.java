package com.example.behavior_based_authentication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NewActivity extends TouchActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Button backBtn = (Button) findViewById(R.id.BackButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}