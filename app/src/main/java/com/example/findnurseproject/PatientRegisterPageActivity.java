package com.example.findnurseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
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

public class PatientRegisterPageActivity extends AppCompatActivity {

    EditText patient_email, patient_phonenumber, patient_password, patient_confirm_password;
    Button patient_register_button;
    TextView loginRedirectText;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findnurseproject-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_register_page);

        patient_email = findViewById(R.id.patient_email);
        patient_phonenumber = findViewById(R.id.patient_phonenumber);
        patient_password = findViewById(R.id.patient_password);
        patient_confirm_password = findViewById(R.id.patient_confirm_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        patient_register_button = findViewById(R.id.patient_register_button);

        patient_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = patient_phonenumber.getText().toString();
                String email = patient_email.getText().toString();
                String password = patient_password.getText().toString();
                String confirmPassword = patient_confirm_password.getText().toString();

                RegisterPatient(phoneNumber, email, password, confirmPassword);
            }
        });


    }

    private void RegisterPatient(String phoneNumber, String email, String password, String confirmPassword) {
        if (phoneNumber.equals("") || email.equals("") || password.equals("") || confirmPassword.equals("")) {
            Toast.makeText(PatientRegisterPageActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
        } else if (!isValidEmail(email)) {
            Toast.makeText(PatientRegisterPageActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
        } else if (phoneNumber.length() != 10) {
            Toast.makeText(PatientRegisterPageActivity.this, "Phone Number should be 10 digits", Toast.LENGTH_SHORT).show();
        } else if (!isValidPassword(password)) {
            // Password validation failed
            if (password.length() < 8) {
                Toast.makeText(PatientRegisterPageActivity.this, "Password should be at least 8 characters long", Toast.LENGTH_SHORT).show();
            } else if (!containsUppercase(password)) {
                Toast.makeText(PatientRegisterPageActivity.this, "Password should contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            } else if (!containsLowercase(password)) {
                Toast.makeText(PatientRegisterPageActivity.this, "Password should contain at least one lowercase letter", Toast.LENGTH_SHORT).show();
            } else if (!containsDigit(password)) {
                Toast.makeText(PatientRegisterPageActivity.this, "Password should contain at least one digit", Toast.LENGTH_SHORT).show();
            } else if (!containsSpecialChar(password)) {
                Toast.makeText(PatientRegisterPageActivity.this, "Password should contain at least one special character", Toast.LENGTH_SHORT).show();
            } else if (containsWhitespace(password)) {
                Toast.makeText(PatientRegisterPageActivity.this, "Password should not contain whitespace", Toast.LENGTH_SHORT).show();
            }
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(PatientRegisterPageActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {

            databaseReference.child("patients").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.hasChild(phoneNumber)) {
                        Toast.makeText(PatientRegisterPageActivity.this, "Phone Number is already registered", Toast.LENGTH_SHORT).show();
                    } else {

                        databaseReference.child("nurses").child(phoneNumber).child("nurse_email").setValue(email);
                        databaseReference.child("nurses").child(phoneNumber).child("nurse_password").setValue(password);

                        Toast.makeText(PatientRegisterPageActivity.this, "Nurse Registered Sucessfully", Toast.LENGTH_SHORT).show();
                        finish();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && containsUppercase(password) && containsLowercase(password) && containsDigit(password) && containsSpecialChar(password) && !containsWhitespace(password);
    }


    private boolean containsUppercase(String password) {
        return !password.equals(password.toLowerCase());
    }

    private boolean containsLowercase(String password) {
        return !password.equals(password.toUpperCase());
    }

    private boolean containsDigit(String password) {
        return password.matches(".*\\d.*");
    }

    private boolean containsSpecialChar(String password) {
        return !password.matches("[a-zA-Z0-9 ]*");
    }

    private boolean containsWhitespace(String password) {
        return password.contains(" ");
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}