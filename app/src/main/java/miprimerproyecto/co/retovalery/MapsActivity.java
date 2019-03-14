package miprimerproyecto.co.retovalery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final int REQUEST_CODE = 11;
    private GoogleMap mMap;
    private LocationManager manager;
    private Marker me;
    private FloatingActionButton fab;
    private boolean editarMapa;

    private Button bt_ingreso_lugar;
    private EditText et_nombre_lugar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        bt_ingreso_lugar = (Button) findViewById(R.id.bt_ingreso_lugar);/*
        bt_ingreso_lugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lugar=et_nombre_lugar.getText().toString();
                if(lugar==null || lugar.equals("")){
                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    "Ingrese una informacion en el campo", Toast.LENGTH_LONG);
                }else{
                    //guardar y lo toma

                }
            }
        });*/


        fab = (FloatingActionButton) findViewById(R.id.fab);
        editarMapa = false;
        fab.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (editarMapa) {
                    //la persona quiere editar el mapa
                    editarMapa = false;
                    view.setBackgroundTintList(new ColorStateList(new int[][]
                            {new int[]{0}}, new int[]{getResources().getColor(R.color.colorAccent)}));
                    //tiene que activar el metodo de agregar marcador


                } else if (!editarMapa) {
                    editarMapa = true;
                    view.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{getResources().getColor(R.color.colorPrimary)}));
                    //tiene que desactivar el marcador
                }


            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        mMap.setOnMapClickListener(this);

        //si tiene los permisos con la red
        if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if (me != null) {
                        me.remove();
                    }
                    LatLng latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());

                    me = mMap.addMarker(new MarkerOptions()
                            .position(latLngCurrent)
                            .title("Me")
                    );
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        } else if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }

    @Override
    public void onMapClick(final LatLng point) {
        //Por lo general esta desactivado

        if(editarMapa){
            showCustomDialog();

            mMap.addMarker(new MarkerOptions().position(point));

        }

    }

    private void showCustomDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
