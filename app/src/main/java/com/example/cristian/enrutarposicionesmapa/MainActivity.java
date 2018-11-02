package com.example.cristian.enrutarposicionesmapa;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowLongClickListener{

    private boolean mostarTurorial = true;

    private Button btnTrazarRutaOptima;
    private FloatingActionButton btnAddPos;
    private ProgressBar progressRutaOptima;
    private MapView mapView;
    private GoogleMap mapa;
    private TextView txtPosNoDisponible;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient locationClient;
    private Location posicion;
    private ArrayList<Marker> marcadores = new ArrayList<>();
    private boolean modoAddPos;// Modo esperando a que toquen mapa para agregar un marcador

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {// si gps esta activo
            Toast.makeText(this, "Por favor activar GPS", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        modoAddPos = false;

        progressRutaOptima = findViewById(R.id.progress_ruta_optima);
        btnTrazarRutaOptima = findViewById(R.id.btn_trazar_ruta_optima);
        btnAddPos = findViewById(R.id.btn_add_pos);
        mapView = findViewById(R.id.map_view);
        txtPosNoDisponible = findViewById(R.id.txt_pos_no_disponible);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, new PosicionCallback(), null);
        }

        btnTrazarRutaOptima.setOnClickListener(this);
        btnAddPos.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        validarPermisos();

        mapa = googleMap;
        mapa.setOnMapClickListener(this);
        mapa.setOnInfoWindowLongClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (validarPermisos()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mapa.setMyLocationEnabled(true);
                locationClient.requestLocationUpdates(locationRequest, new PosicionCallback(), null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_trazar_ruta_optima:

                break;
            case R.id.btn_add_pos:
                modoAddPos = true;
                btnAddPos.hide();

                // Mostramos solo la primera vez que se le de click
                if ( mostarTurorial) {
                    Toast.makeText(this, "Toque el mapa para agregar marcador", Toast.LENGTH_LONG).show();
                    mostarTurorial = false;
                }

                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if( modoAddPos ) {
            String index = String.valueOf( (marcadores.size() + 1) );

            Marker marcador = mapa.addMarker(
                    new MarkerOptions()
                            .title( index )
                            .position(latLng)
            );

            marcadores.add(marcador);

            // Termina el modo para agregar marcador
            modoAddPos = false;
            btnAddPos.show();
        }
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {



    }

    private class PosicionCallback extends LocationCallback{

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

            if( locationAvailability.isLocationAvailable() ) {
                txtPosNoDisponible.setVisibility(View.GONE);
            } else {
                txtPosNoDisponible.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if( txtPosNoDisponible.getVisibility() == View.VISIBLE )
                txtPosNoDisponible.setVisibility(View.GONE);

            posicion =  locationResult.getLastLocation();
        }
    }

    private boolean validarPermisos() {
        ArrayList<String> permisos = new ArrayList<>();
        boolean estanCumplidos = true;

        if( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
            estanCumplidos = false;
        }

        if( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            estanCumplidos = false;
        }

        if( !permisos.isEmpty() ) {
            String[] permisosArray = permisos.toArray(new String[permisos.size()]);

            ActivityCompat.requestPermissions(this, permisosArray, 432);
        }

        return estanCumplidos;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
