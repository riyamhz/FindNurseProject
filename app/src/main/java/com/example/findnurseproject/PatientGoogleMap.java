package com.example.findnurseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

import java.util.HashMap;
import java.util.List;

public class PatientGoogleMap extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;

    Button findNurseBtn;
    Location Patientlocation;


    int radius = 1;
     private Boolean nurseFound = false;
     private  String nurseFoundPhoneNumber ;
     String patientPhoneNumber;

     DatabaseReference NurseLocationRef;

     Marker NurseMarker;
     private  DatabaseReference NurseRef;



    GoogleMap mMap;
    DatabaseReference NurseAvailableRef;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_google_map);

        NurseLocationRef = FirebaseDatabase.getInstance().getReference().child("Nurses Working");
        NurseAvailableRef = FirebaseDatabase.getInstance().getReference().child("Nurse Available");

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

        findNurseBtn=findViewById(R.id.findNurseBtn);



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

                            findNurseBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

//                                    String PatientPhoneNumber = getIntent().getStringExtra("PatientPhoneNumber");
//                                    DatabaseReference PatientDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Patient Request");
//
//                                    GeoFire geoFire = new GeoFire(PatientDatabaseRef);
//                                    geoFire.setLocation(PatientPhoneNumber, new GeoLocation(location.getLatitude(), location.getLongitude()));
//
//                                    LatLng LatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                                    mMap.addMarker(new MarkerOptions().position(LatLng).title("I am a Patient"));

                                    findNurseBtn.setText("Getting your Nurse....");
                                    GetClosestNurse();
                                }
                            });


                        } else {
                            Toast.makeText(PatientGoogleMap.this, "Please on your Location App permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void GetClosestNurse() {
        if (Patientlocation != null) {
            GeoFire geoFire = new GeoFire(NurseAvailableRef);
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Patientlocation.getLatitude(), Patientlocation.getLongitude()), radius);
            geoQuery.removeAllListeners();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!nurseFound){
                        nurseFound = true;
                        nurseFoundPhoneNumber = key;

                        NurseRef = FirebaseDatabase.getInstance().getReference().child("nurses").child(nurseFoundPhoneNumber);
                        HashMap nurseMap = new HashMap();
                        nurseMap.put("PatientSearchID", patientPhoneNumber);
                        NurseRef.updateChildren(nurseMap);

                        GettingNurseLocation();
                        findNurseBtn.setText("Looking for Nurse Location");
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    if(!nurseFound){
                        radius = radius + 1;
                        GetClosestNurse();
                    }
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });

        }

    }

    private void GettingNurseLocation() {

        NurseLocationRef.child(nurseFoundPhoneNumber).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                       if (datasnapshot.exists()){
                           List<Object> NurseLocationmap = (List<Object>) datasnapshot.getValue();
                           double Locationlat = 0;
                           double Locationlng = 0;
                           findNurseBtn.setText("Nurse Found");

                           if (NurseLocationmap.get(0) != null){
                               Locationlat = Double.parseDouble(NurseLocationmap.get(0).toString());
                           }

                           if (NurseLocationmap.get(1) != null){
                               Locationlng = Double.parseDouble(NurseLocationmap.get(1).toString());
                           }

                           LatLng NurseLatLng = new LatLng(Locationlat, Locationlng);
                            if (NurseMarker != null) {
                                NurseMarker.remove();
                            }

                           Location location1 = new Location("");
                           location1.setLatitude(Patientlocation.getLatitude());
                           location1.setLongitude(Patientlocation.getLongitude());


                           Location location2 = new Location("");
                            location2.setLatitude(NurseLatLng.latitude);
                            location2.setLongitude(NurseLatLng.longitude);

                            float Distance = location1.distanceTo(location2);
                            findNurseBtn.setText("Nurse Found:" + String.valueOf(Distance));


                            NurseMarker = mMap.addMarker(new MarkerOptions().position(NurseLatLng).title("Your Nurse is here."));

                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
    @Override
    protected void onStop() {
        super.onStop();


    }
}