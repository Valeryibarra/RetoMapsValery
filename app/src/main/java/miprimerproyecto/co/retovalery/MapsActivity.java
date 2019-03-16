package miprimerproyecto.co.retovalery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, DialogInput.iDialogInterfaceActions {

    private GoogleMap mMap;
    private LocationManager manager;
    private Marker me;
    private boolean mapEditable;

    private ArrayList<Marker> listLugares;

    private LatLng currentPointClicked;
    private LatLng currentMe;

    private FloatingActionButton fab;
    private TextView tv_info;

    //CONSTANTS
    private static final int REQUEST_CODE = 11;
    private final static double CLOSE_DISTANCE = 50.0;
    private final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    private final static int METER_CONVERSION = 1609;
    private final static  double KM_CONVERSION = 1.6093;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Graphic elements
        tv_info = findViewById(R.id.tv_info);
        fab = findViewById(R.id.fab);

        listLugares = new ArrayList<Marker>();

        mapEditable = false;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapEditable) {
                    mapEditable = false;
                    //This is to change the color of the button when the map is editable or not
                    view.setBackgroundTintList(new ColorStateList(new int[][]
                            {new int[]{0}}, new int[]{getResources().getColor(R.color.colorAccent)}));
                } else if (!mapEditable) {
                    mapEditable = true;
                    view.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{getResources().getColor(R.color.colorPrimaryDark)}));
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
                    currentMe = new LatLng(location.getLatitude(), location.getLongitude());
                    String address = getAddress(currentMe);
                    //The information drawer is changed to indicate that it is in that place since there are still no places
                    if(listLugares.isEmpty()){
                        tv_info.setText("You are in "+address);
                    }
                    me = mMap.addMarker(new MarkerOptions()
                            .position(currentMe)
                            .title("You")
                            .snippet(address)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maps_add))
                    );
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    //Update places and its distances
                    updateDistances(currentMe);
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

                    if (me != null) {
                        me.remove();
                    }

                    currentMe = new LatLng(location.getLatitude(), location.getLongitude());

                    String address = getAddress(currentMe);

                    //The information drawer is changed to indicate that it is in that place since there are still no places
                    if(listLugares.isEmpty()){
                        tv_info.setText("You are in "+address);
                    }

                    me = mMap.addMarker(new MarkerOptions()
                            .position(currentMe)
                            .title("You")
                            .snippet(address)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maps_add))
                    );
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    //Update places and its distances
                    updateDistances(currentMe);
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
        //Only this method works when the button to add elements to the map is activated
        if(mapEditable){
            currentPointClicked= point;
            openDialogNewPlace();
        }

    }

    public void updateDistances(LatLng yo){

        //Only update distances if there are distances
        if(!listLugares.isEmpty()){
            Double shorterDistance=0.0;
            Marker shorteMarker=me;

            Double yoLat= yo.latitude;
            Double yoLong=yo.longitude;

            for (int i=0; i<listLugares.size(); i++){
                //Marker of the actual place
                Marker placeMarker = listLugares.get(i);

                Double placeLat= placeMarker.getPosition().latitude;
                Double placeLong= placeMarker.getPosition().longitude;
                Double distance = getDistance(yoLat,yoLong,placeLat, placeLong);


                //The user moves son the distance at the place change
                placeMarker.setSnippet("You are at "+distance+"m distance from the place ");

                if(shorterDistance== 0.0){
                    //This is for the first time
                    shorterDistance=distance;
                    shorteMarker=placeMarker;
                }else{
                    if(distance<shorterDistance){
                        shorterDistance=distance;
                        shorteMarker=placeMarker;
                    }
                }
            }
            //Now I have the closest one, so I look if that one I found is below the close distance
            if(shorterDistance <= CLOSE_DISTANCE) {
                //Yes it is in a close distance then I change the information box
                tv_info.setText("The closest place to your location is "+shorteMarker.getTitle()+"");
            }
        }
    }



    public void openDialogNewPlace(){
        DialogInput dialogInput =new DialogInput();
        dialogInput.show(getSupportFragmentManager(), "DialogInput");
    }



    @Override
    public void createNewPlace(String newPlace) {
        Toast toast1 =
                Toast.makeText(getApplicationContext(),
                        "El lugar agregado  es " +newPlace.toUpperCase(), Toast.LENGTH_LONG);
        toast1.show();

        Double distance= getDistance(currentMe.latitude,currentMe.longitude,currentPointClicked.latitude,currentPointClicked.longitude);


        //Marker of the place that the user wants to add
        Marker placeMarker =mMap.addMarker(new MarkerOptions()
                .position(currentPointClicked)
                .title("" + newPlace.toUpperCase())
                .snippet("You are at "+distance+"m distance from the place ")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
        //Here I have obtained its distance and I have the marker then I verify that it is below than the closes distance
        if(distance <= CLOSE_DISTANCE) {
            //Yes it is in a close distance then I change the information box
            tv_info.setText("The closest place to your location is "+placeMarker.getTitle()+"");
        }

        listLugares.add(placeMarker);
    }

    private String getAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        String result = "";
        Double latitude= latLng.latitude;
        Double longitude= latLng.longitude;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            result = address;

        } catch (IOException e) {
            e.printStackTrace();
            result = "Location not found";
        }
        return result;

    }

    public static Double getDistance(Double lat_a, Double lng_a, Double lat_b, Double lng_b) {
        // earth radius is in mile
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(Math.toRadians(lat_a))
                * Math.cos(Math.toRadians(lat_b)) * Math.sin(lngDiff / 2)
                * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;
        double kmConvertion = 1.6093;
        return distance * meterConversion;
//        return String.format("%.2f", new Float(distance * kmConvertion).floatValue()) + " km";
        //return String.format("%.2f", distance)+" m";
    }


}
