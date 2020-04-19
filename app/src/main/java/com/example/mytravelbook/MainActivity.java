package com.example.mytravelbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // ilk aktivitede map olmadığı için main activity empty activity olarak seçtim
    // Bu main activity içine bir tane menü koydum(yer eklemek için) ve bir tanede listview(eklediğim yerleri göstermek için) koydum
    // XML tarafında sayfanın tamamına bir listview koyduk birde res -> New -> Directory -> menu yazdık
    // sonrada menuye geldik sağ tıkladık new -> Menu Resource File -> add_place dedik

    //mapsactivitydeki işlemler bitince veri tabanından veri çekmek için :
    static ArrayList<String> names = new ArrayList<String>();
    static ArrayList<LatLng> locations = new ArrayList<LatLng>();
    static ArrayAdapter arrayAdapter;
//bu arraylistlere mapsactivitydeden de erimek gerektği için static yaptık


    //bu fonksiyonda menuyü bağlıyoeuz
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_place, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //bu fonksiyonda da menu ye tıklandığında ne olacağını yazıyoruz
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.add_place){ //eğer tıklanan item add_place ise haritaya geçmeli ki istediğini eklesin

            //bURAYA yazmadan önce com.example.mytravelbook  -> sağ tıkla new -> Activity -> Gallery ->GoogleMapsActivity ekle.
            //sonra açılan mapsactivity ekranındaki https:// ile başlayan cümleyi tüm kopyala internete git yapıltır
            // açılan sayfada devam'a bas, yani API'yi etkin hale getirecek bir key verecek o keyi kopyala YOUR KEYHERE'a yapıştır
            //sıra da INTENT kısmı var :
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("info", "new");  //yeni kayıt yapıacağıni belli ediyo

            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);


        try{
            MapsActivity.database = this.openOrCreateDatabase("Places", MODE_PRIVATE, null);
            Cursor cursor = MapsActivity.database.rawQuery("SELECT * FROM places", null);

            int nameIx  =cursor.getColumnIndex("name");
            int latitudeIx = cursor.getColumnIndex("latitude");
            int longitudeIx = cursor.getColumnIndex("longitude");

            while(cursor.moveToNext()){

                String nameFromDatabase = cursor.getString(nameIx);
                String latitudeFromDatabase = cursor.getString(latitudeIx);
                String longitudeFromDatabase = cursor.getString(longitudeIx);

                names.add(nameFromDatabase);

                Double l1 = Double.parseDouble(latitudeFromDatabase);
                Double l2  =Double.parseDouble(longitudeFromDatabase);

                LatLng locationFromDatabase = new LatLng(l1, l2);
                locations.add(locationFromDatabase);
                System.out.println("name : "+ nameFromDatabase);

            }
            cursor.close();


        }catch (Exception e){

        }

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(arrayAdapter);


        //Listview da herhangi bir cell e tıklandığında ne olacak mesela kaydedilen bşyer var onun üstüne tıkladığında haritaya gitsin o yeri bana göstersin:
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("info", "old");  //yani kayıtlı yerigöster diyoruz
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });




        //Menuyü buraya bağlamak için iki tane fonksiyon var 1)onCreateOptionsMenu 2)onOptionsItemSelected
    }
}
