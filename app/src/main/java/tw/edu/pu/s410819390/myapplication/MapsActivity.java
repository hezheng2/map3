package tw.edu.pu.s410819390.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import tw.edu.pu.s410819390.myapplication.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        android.location.LocationListener{

    private GoogleMap mMap;

    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;

    LocationManager locationManager;
    private ActivityMapsBinding binding;

    @Override
    public void onLocationChanged(Location location) {
        if (location != null){
            String msg = "緯度：" + location.getLatitude()  + "\n經度：" + location.getLongitude();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            LatLng UserPlace = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(UserPlace)
                            .zoom(mMap.getCameraPosition().zoom)
                            .bearing(location.getBearing())
                            .build();
            // 使用動畫的效果移動地圖
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else{
            Toast.makeText(this, "無法取得定位資訊", Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "GPS已經開啟", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "GPS已關閉", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.  詢問使用者開啟權限

            // 如果裝置版本是6.0（包含）以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 取得授權狀態，參數是請求授權的名稱
                int hasPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

                // 如果未授權，向使用者請求
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    // 請求授權
                    //     第一個參數是請求授權的名稱，第二個參數是請求代碼
                    requestPermissions(
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_FINE_LOCATION_PERMISSION);
                }
            }
        } else if (mMap != null) {
            // 在地圖上啟用「我的位置」圖層
            mMap.setMyLocationEnabled(true);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);

        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (permissions.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(this, "需允許位置資訊授權，\n才能顯示位置圖層", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "鏡頭轉至目前位置", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // 直接讓鏡頭轉至使用者目前位置
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();

        // 如果裝置版本是6.0（包含）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            int hasPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

            // 如果已授權，關掉更新
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }
        }
        else{
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        // 如果裝置版本是6.0（包含）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            int hasPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);

            // 如果已授權，處理我的位置圖層
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();  //處理我的位置圖層
            }
        }
        else {
            enableMyLocation();  //處理我的位置圖層
        }
    }
    private ArrayList<LatLng> traceOfMe;
    private void trackToMe(double lat, double lng){
        if (traceOfMe == null) {
            traceOfMe = new ArrayList<LatLng>();
        }
        traceOfMe.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }

        polylineOpt.color(Color.RED);

        Polyline line = mMap.addPolyline(polylineOpt);
        line.setWidth(10);
    }

}