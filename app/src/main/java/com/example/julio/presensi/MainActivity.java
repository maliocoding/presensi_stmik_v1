package com.example.julio.presensi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.nfc.Tag;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.security.Permission;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       cekPermission();

    }

    private  void navigasi(){
        DBHelper myDB;

        myDB=new DBHelper(this);
        Cursor cursor;



        //cek status
        Cursor hasil = myDB.getAllData();
        if(hasil.getCount() == 0) {
            //kalo database kosong pindah ke login
//            Intent intent = new Intent(MainActivity.this, Login.class);
            Intent intent = new Intent(MainActivity.this, Login.class);
            finish();
            startActivity(intent);


        }else{

            try {
//               int stat= hasil.getInt( hasil.getColumnIndex("status"));
                SQLiteDatabase db = myDB.getReadableDatabase();
                cursor = db.rawQuery("SELECT * FROM user ",null);
                cursor.moveToFirst();

                cursor.moveToPosition(0);
                String status= cursor.getString(3);
                if (status.equals("1")){
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    finish();
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    finish();
                    startActivity(intent);
                    Toast.makeText(this, "status 0" , Toast.LENGTH_SHORT).show();
                }


            }catch(Exception e){
                Intent intent = new Intent(MainActivity.this, Login.class);
                finish();
                startActivity(intent);
                Toast.makeText(this, "ada error" , Toast.LENGTH_SHORT).show();
            }

        }
    }
    private void  cekPermission() {
        Log.d(TAG, "cekPermission : asking user for permission");
        String[] permissions = {Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.INTERNET};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[0])== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[1])== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[2])== PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),permissions[3])== PackageManager.PERMISSION_GRANTED )
        {
                navigasi();
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,permissions,41231);
            cekPermission();
        }
    }
}
