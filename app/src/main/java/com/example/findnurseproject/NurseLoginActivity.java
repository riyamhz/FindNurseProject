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

public class NurseLoginActivity extends AppCompatActivity {

    EditText nurse_login_phonenumber, nurse_login_password;
    Button nurse_login_button;
    TextView nurse_registerRedirectText;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findnurseproject-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_login);

        nurse_login_phonenumber = findViewById(R.id.nurse_login_phonenumber);
        nurse_login_password = findViewById(R.id.nurse_login_password);
        nurse_registerRedirectText = findViewById(R.id.nurse_registerRedirectText);
        nurse_login_button = findViewById(R.id.nurse_login_button);

        nurse_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phonenumber = nurse_login_phonenumber.getText().toString();
                String password = nurse_login_password.getText().toString();

                if (phonenumber.equals("") || password.equals("")){
                    Toast.makeText(NurseLoginActivity.this, "Please enter phonenumber or password", Toast.LENGTH_SHORT).show();
                } else {


                    databaseReference.child("nurses").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild(phonenumber)){
                                String getPassword = snapshot.child(phonenumber).child("nurse_password").getValue(String.class);
                                
                                if (getPassword.equals(password)){
                                    Toast.makeText(NurseLoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                                    String nursePhoneNumber = nurse_login_phonenumber.getText().toString();
                                    Intent intent = new Intent(NurseLoginActivity.this,GoogleMap.class);
                                    intent.putExtra("nursePhoneNumber", nursePhoneNumber);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(NurseLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(NurseLoginActivity.this, "Invalid phonenumber ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(NurseLoginActivity.this, "Error accessing database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        nurse_registerRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NurseLoginActivity.this, NurseRegisterPageActivity.class);
                startActivity(intent);
            }
        });

    }


}