package miprimerproyecto.co.retovalery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, DialogEntrada.iDialogInterfaceActions {

    private static final int REQUEST_CODE = 11;
    private GoogleMap mMap;
    private LocationManager manager;
    private Marker me;
    private FloatingActionButton fab;
    private boolean editarMapa;
    private HashMap<LatLng,String> hashMapLugares;

    private LatLng currentPointClicked;

    private Button bt_ingreso_lugar;
    private EditText et_nombre_lugar;

    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        hashMapLugares = new HashMap<LatLng, String>();



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

                    Float yoLat= convertToFloat(location.getLatitude());
                    Float yoLong=convertToFloat(location.getLongitude());



                    me = mMap.addMarker(new MarkerOptions()
                            .position(latLngCurrent)
                            .title("Me")
                            .snippet(getAddress(location.getLatitude(), location.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_maps_add))
                    );
                    mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

                    //Verificar cuanto llevo con las demas distancias

                    calculoDistancias(latLngCurrent);


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
            currentPointClicked= point;
            openDialogNuevoLugar();
        }

    }

    public void calculoDistancias(LatLng yo){


        Float yoLat= convertToFloat(yo.latitude);
        Float yoLong=convertToFloat(yo.longitude);

        if(hashMapLugares.isEmpty()){
            //no ha agregado nada
        }else{
            //ver cual es el lugar mas cercano y si ese esta a 20m o menos poner el snack bar
            //por cada lugar hallar la distancia
            //por cada lugar que haya en el hash map cambiar su snippet a la distancia entre el y ese punto
            Float menor=0.0f;
            for (Map.Entry<LatLng, String> entry : hashMapLugares.entrySet()) {
                LatLng key = entry.getKey();
                String value = entry.getValue();

                Float lugarLat= convertToFloat(key.latitude);
                Float lugarLong=convertToFloat(key.longitude);
                Float distance = distFrom(yoLat,yoLong,lugarLat, lugarLong);



                if((Float.compare(menor, 0.0f)==0)){

                }else{
                    if (Float.compare(menor, distance) == 0) {

                        //son iguales entonces menor sigue siendo menor

                        System.out.println("f1=f2");
                    }
                    else if (Float.compare(menor, distance) < 0) {
                        //entonces menor sigue siendo menor que distance
                    }
                    else {
                        menor=distance;
                        Toast toast1 =
                                Toast.makeText(getApplicationContext(),
                                        "la menor distantcia es "+menor+"", Toast.LENGTH_LONG);
                        toast1.show();

                    }
                }

            }
        }

    }

    public void openDialogNuevoLugar(){
        DialogEntrada dialogEntrada =new DialogEntrada();
        dialogEntrada.show(getSupportFragmentManager(), "DialogEntrada");
    }



    @Override
    public void crearNuevoLugar(String nuevoLugar) {

        //Lo guardo en la lista de lugares agregados
        hashMapLugares.put(currentPointClicked, nuevoLugar);
        Toast toast1 =
                Toast.makeText(getApplicationContext(),
                        "El lugar agregado  es" +nuevoLugar, Toast.LENGTH_LONG);
        toast1.show();



        mMap.addMarker(new MarkerOptions()
                .position(currentPointClicked)
                .title("Este lugar es: " + nuevoLugar.toUpperCase())
                .snippet(getAddress(currentPointClicked.latitude, currentPointClicked.longitude)+"")
        );
    }

    private String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        String res = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            res = address;

        } catch (IOException e) {
            e.printStackTrace();
            res = "paila";
        }
        return res;

    }

    public  float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    public float distance (float lat_a, float lng_a, float lat_b, float lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    public static Float convertToFloat(Double doubleValue) {
        return doubleValue == null ? null : doubleValue.floatValue();
    }
}
