package com.example.findnurseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstPageActivity extends AppCompatActivity {

    Button nurse_btn, patient_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);

        nurse_btn = findViewById(R.id.nurse_btn);
        patient_btn = findViewById(R.id.patient_btn);

        nurse_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstPageActivity.this, NurseLoginActivity.class);
                startActivity(intent);
            }
        });

        patient_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstPageActivity.this, PatientLoginActivity.class);
                startActivity(intent);
            }
        });

    }
}