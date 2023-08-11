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

public class NurseRegisterPageActivity extends AppCompatActivity {


    EditText nurse_name, nurse_licence_number, nurse_phonenumber, nurse_email, nurse_password, nurse_confirm_password;
    TextView loginRedirectText;
    Button nurse_register_button;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://findnurseproject-default-rtdb.firebaseio.com/");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_register_page);

        nurse_name = findViewById(R.id.nurse_name);
        nurse_licence_number = findViewById(R.id.nurse_license_number);
        nurse_phonenumber = findViewById(R.id.nurse_phonenumber);
        nurse_email = findViewById(R.id.nurse_email);
        nurse_password = findViewById(R.id.nurse_password);
        nurse_confirm_password = findViewById(R.id.nurse_confirm_password);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        nurse_register_button = findViewById(R.id.nurse_register_button);

        nurse_register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nurse_name.getText().toString();
                String licenceNumber = nurse_licence_number.getText().toString();
                String phoneNumber = nurse_phonenumber.getText().toString();
                String email = nurse_email.getText().toString();
                String password = nurse_password.getText().toString();
                String confirmPassword = nurse_confirm_password.getText().toString();

                RegisterNurse(name, licenceNumber,phoneNumber, email, password, confirmPassword);
            }
        });



    }

    private void RegisterNurse(String name, String licenceNumber, String phoneNumber,String email, String password, String confirmPassword) {

        if(name.equals("") || licenceNumber.equals("") || phoneNumber.equals("") || email.equals("") ||password.equals("")||confirmPassword.equals("")) {
            Toast.makeText(NurseRegisterPageActivity.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
        } else if (!isValidEmail(email)){
            Toast.makeText(NurseRegisterPageActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
        } else if (phoneNumber.length() != 10){
            Toast.makeText(NurseRegisterPageActivity.this, "Phone Number should be 10 digits", Toast.LENGTH_SHORT).show();
        }else if (!isValidPassword(password)) {
            // Password validation failed
            if (password.length() < 8) {
                Toast.makeText(NurseRegisterPageActivity.this, "Password should be at least 8 characters long", Toast.LENGTH_SHORT).show();
            } else if (!containsUppercase(password)) {
                Toast.makeText(NurseRegisterPageActivity.this, "Password should contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            } else if (!containsLowercase(password)) {
                Toast.makeText(NurseRegisterPageActivity.this, "Password should contain at least one lowercase letter", Toast.LENGTH_SHORT).show();
            } else if (!containsDigit(password)) {
                Toast.makeText(NurseRegisterPageActivity.this, "Password should contain at least one digit", Toast.LENGTH_SHORT).show();
            } else if (!containsSpecialChar(password)) {
                Toast.makeText(NurseRegisterPageActivity.this, "Password should contain at least one special character", Toast.LENGTH_SHORT).show();
            } else if (containsWhitespace(password)) {
                Toast.makeText(NurseRegisterPageActivity.this, "Password should not contain whitespace", Toast.LENGTH_SHORT).show();
            }
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(NurseRegisterPageActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        }else{

            String encodedEmail = email.replace(".",",");

            databaseReference.child("nurses").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.hasChild(phoneNumber)){
                        Toast.makeText(NurseRegisterPageActivity.this, "Phone Number is already registered", Toast.LENGTH_SHORT).show();
                    } else {

                        databaseReference.child("nurses").child(phoneNumber).child("nurse_name").setValue(name);
                        databaseReference.child("nurses").child(phoneNumber).child("nurse_licence_number").setValue(licenceNumber);
                        databaseReference.child("nurses").child(phoneNumber).child("nurse_email").setValue(encodedEmail);
                        databaseReference.child("nurses").child(phoneNumber).child("nurse_password").setValue(password);

                        Toast.makeText(NurseRegisterPageActivity.this, "Nurse Registered Sucessfully", Toast.LENGTH_SHORT).show();
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