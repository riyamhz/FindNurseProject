package com.example.findnurseproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

public class GoogleMap extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;

    private DatabaseReference AssignedPatientRef, AssignedPatientSearchRef;
    private String nursePhoneNumber, patientPhoneNumber;

com.google.android.gms.maps.GoogleMap mMap;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);

        fusedLocationProviderClient = (FusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);

        Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                getCurrentLocation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

        mapFragment.getMapAsync(this);

        GetAssignedPatientRequest();

    }

    private void GetAssignedPatientRequest() {
        AssignedPatientRef = FirebaseDatabase.getInstance().getReference().child("nurses").child(nursePhoneNumber).child("PatientSearchID");

        AssignedPatientRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.exists()) {
                    patientPhoneNumber = datasnapshot.getValue().toString();

                    GetAssignedPatientSearchLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetAssignedPatientSearchLocation() {
        AssignedPatientSearchRef = FirebaseDatabase.getInstance().getReference().child("Patient Requests").child(patientPhoneNumber).child("l");

        AssignedPatientSearchRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.exists()){
                    List<Object> patientLocationMap = (List<Object>) datasnapshot.getValue();

                    double Locationlat = 0;
                    double Locationlng = 0;

                    if (patientLocationMap.get(0) != null){
                        Locationlat = Double.parseDouble(patientLocationMap.get(0).toString());
                    }

                    if (patientLocationMap.get(1) != null){
                        Locationlng = Double.parseDouble(patientLocationMap.get(1).toString());
                    }

                    LatLng NurseLatLng = new LatLng(Locationlat, Locationlng);
                    mMap.addMarker(new MarkerOptions().position(NurseLatLng).title("Search Location"));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull com.google.android.gms.maps.GoogleMap googleMap) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location !");
                            googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                            String nursePhoneNumber = getIntent().getStringExtra("nursePhoneNumber");
                            DatabaseReference NurseAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Nurse Available");

                            GeoFire geoFireAvailability = new GeoFire(NurseAvailibilityRef);

                            DatabaseReference NurseWorkingRef = FirebaseDatabase.getInstance().getReference().child("Nurses Working");
                            GeoFire geoFireWorking = new GeoFire(NurseWorkingRef);

                            switch (patientPhoneNumber){
                                case "":
                                    geoFireWorking.removeLocation(nursePhoneNumber);
                                    geoFireAvailability.setLocation(nursePhoneNumber, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                break;

                                default:
                                    geoFireAvailability.removeLocation(nursePhoneNumber);
                                    geoFireWorking.setLocation(nursePhoneNumber, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                break;
                            }
                        } else {
                            Toast.makeText(GoogleMap.this, "Please on your Location App permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(@NonNull com.google.android.gms.maps.GoogleMap googleMap) {

    }

    @Override
    protected void onStop() {
        super.onStop();


        String nursePhoneNumber = getIntent().getStringExtra("nursePhoneNumber");
        DatabaseReference NurseAvailibilityRef = FirebaseDatabase.getInstance().getReference().child("Nurse Available");

        GeoFire geoFire = new GeoFire(NurseAvailibilityRef);
        geoFire.removeLocation(nursePhoneNumber);
    }
}

