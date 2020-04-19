package com.example.mytravelbook;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
//address i almak için OnMapLongClickListener ı implement etmeliyiz
    private GoogleMap mMap;
    //Burada iki tane önemli değişkenimiz vardır : 1)LocationManager 2)LocationListener
    LocationManager locationManager;
    LocationListener locationListener;
    static SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    //Harita hazır olduğunda gösterecekleri :
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setOnMapLongClickListener(this); //implement ettikya en tepede onunla appi bağlamak için gereklidir.

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.matches("new")){





        locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//normalde latlng lu kısım sadece yapıldığında harita açılıyor haritada gezebiliyorsun fakat beğendiğin yeri seçemiyosun ilk haline dönüp duruyo
//bunu engellemek için kullanıcı ilk defa mı giriyor appe kontrol et eğer ilk değilse false'u true yap, ama eğer ilkse false kısmına girsin ve konumun güncellensin
                SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.example.mytravelbook", MODE_PRIVATE);
                boolean firstTimeCheck = sharedPreferences.getBoolean("NotFirstTime", false);

                if(firstTimeCheck == false){
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    sharedPreferences.edit().putBoolean("NotFirstTime", true).apply();
                }

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
        };


        //Herşeyden önce kullanıcnın yerini belirlemek, güncellemek vs hepsi için izinleri yazmamız lazım.Buidversionla başladım:
        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){ // Yani EĞER İZİN YOK İSE :
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);  //İZİN YOKSA İZİN İSTE
            }else {  //YANİ İZİN VARSA :
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //Burada kullanıcının konumunu almaya başladık(izin varsa eğer)

                mMap.clear();
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }



            }
        }else{ //eğer versiyon 23 ten küçükse zaten izin istemeden direk konum al
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //Burada kullanıcının konumunu almaya başladık(izin varsa eğer)
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastLocation != null){
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
            }
        }

    }else{  //EĞER INFO OLD İSE :
            mMap.clear();
            int position = intent.getIntExtra("position", 0);
            LatLng location = new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
            String placeName = MainActivity.names.get(position);

            mMap.addMarker(new MarkerOptions().title(placeName).position(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

        }

    }

    //Bu fonksiyon kullanıcının izni yoksa yukarıda izin istedik ve kullanıcı izni verirse ne olacağını, appa nası devam edecek:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0){

            if(requestCode == 1){

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //Burada kullanıcının konumunu almaya başladık(izin varsa eğer)
                    //location ı bul dedik

                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");

                    if(info.matches("new")){
                        //buradaki sorun şudur kullanıcı ilk defa açtığında lastlocationı yoktur enlem boylam null gelir ve app çöker  engellemek için : if kısmını ekledim
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }
                    }else{
                        mMap.clear();
                        int position = intent.getIntExtra("position", 0);
                        LatLng location = new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
                        String placeName = MainActivity.names.get(position);

                        mMap.addMarker(new MarkerOptions().title(placeName).position(location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

                    }

                }
            }
        }


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        //adresleri ve enlem boylamları eşleştirir
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";

        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if(addressList != null && addressList.size() >0){
                if(addressList.get(0).getThoroughfare() != null){
                    address += addressList.get(0).getThoroughfare();

                    if(addressList.get(0).getSubThoroughfare() != null){
                        address += addressList.get(0).getSubThoroughfare();
                    }
                }
            }else{
                address = "NEW PLACE";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().title(address).position(latLng));
        Toast.makeText(getApplicationContext(), "New Place OK!", Toast.LENGTH_SHORT).show();

        MainActivity.names.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged(); //söyle arrayadapter a yeni eklemeler yaptım güncellemeleri yapsım

        // SQLITE İŞLEMLERİ KISMI: Veritabanında ne tutuyoruz 1) Yerin adı(address diye tuttuk) 2)yerin konumu lat long fiye ayrı ayrı tutcam
        //En başta tüm SQLITE işlemlerinde yapıldığı gibi otomatik try and catch yaz
        try {

            Double l1 = latLng.latitude;
            Double l2= latLng.longitude;

            String coord1 = l1.toString();
            String coord2 = l2.toString();

            database = this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places (name VARCHAR, latitude VARCHAR, longitude VARCHAR)");

            String toCompile = "INSERT INTO places (name,latitude, longitude) VALUES (?,?,?)";
            //? DEMEK KOORDİNATŞARIN NERELER OLD BİLMİYORUZ DEMEK

            SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
            sqLiteStatement.bindString(1, address); //1. soru işaretiyle yerin ismini bind ettik
            sqLiteStatement.bindString(2, coord1); //2. soru işaretiyle coord1 i bind ettik
            sqLiteStatement.bindString(3, coord2);

            sqLiteStatement.execute();




        }catch (Exception e){

        }




    }
}
