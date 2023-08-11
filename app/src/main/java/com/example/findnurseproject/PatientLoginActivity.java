package com.example.findnurseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class PatientLoginActivity extends AppCompatActivity {

    EditText patient_login_phonenumber, patient_login_password;
    Button patient_login_button;
    TextView patient_registerRedirectText;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findnurseproject-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        patient_login_phonenumber = findViewById(R.id.patient_login_phonenumber);
        patient_login_password = findViewById(R.id.patient_login_password);
        patient_registerRedirectText = findViewById(R.id.patient_registerRedirectText);
        patient_login_button = findViewById(R.id.patient_login_button);

        patient_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phonenumber =  patient_login_phonenumber.getText().toString();
                String password =  patient_login_password.getText().toString();

                if (phonenumber.equals("") || password.equals("")){
                    Toast.makeText( PatientLoginActivity.this, "Please enter phonenumber or password", Toast.LENGTH_SHORT).show();
                } else {


                    databaseReference.child("nurses").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild(phonenumber)){
                                String getPassword = snapshot.child(phonenumber).child("nurse_password").getValue(String.class);

                                if (getPassword.equals(password)){
                                    Toast.makeText(PatientLoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(PatientLoginActivity.this,PatientGoogleMap.class));
                                    finish();
                                } else {
                                    Toast.makeText(PatientLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(PatientLoginActivity.this, "Invalid phonenumber ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(PatientLoginActivity.this, "Error accessing database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        patient_registerRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientLoginActivity.this, PatientRegisterPageActivity.class);
                startActivity(intent);
            }
        });

    }
}