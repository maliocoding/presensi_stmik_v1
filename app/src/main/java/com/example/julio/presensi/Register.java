package com.example.julio.presensi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Register extends AppCompatActivity {
EditText nama;
EditText npm;
EditText pass1;
EditText pass2;
Spinner jurusan;
String imei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nama=findViewById(R.id.edNama);
        npm=findViewById(R.id.edNPM);
        jurusan=findViewById(R.id.edJurusan);
        pass1=findViewById(R.id.edPass1);
        pass2=findViewById(R.id.edPass2);
    }
    public void reg(View view){

        if (pass1.getText().toString().equals(pass2.getText().toString())) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
             imei = tm.getDeviceId();




            class Regis extends AsyncTask<Void,Void,String> {
                ProgressDialog loading;
                String isinama=nama.getText().toString();
                String isinpm=npm.getText().toString();
                String isijurusan=jurusan.getSelectedItem().toString();
                String isipass=pass1.getText().toString();
                String hasil;
                String status;


                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    loading = ProgressDialog.show(Register.this,"Proses...","Tunggu...",false,false);
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    hasil=s;

                    //get result dari php
                    JSONObject jsonObject;
                    try{
                        jsonObject = new JSONObject(hasil);
                        JSONArray result = jsonObject.getJSONArray(Konfigurasi_Mysql.TAG_JSON_ARRAY);
                        JSONObject jo = result.getJSONObject(0);
                        status = jo.getString("status");
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    loading.dismiss();
                    if(status.equals("berhasil")){
                        Toast.makeText(Register.this, "Ok coy", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(Register.this, "Gagal Coy", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                protected String doInBackground(Void... voids) {
                    HashMap<String,String> params = new HashMap<>();
                    params.put(Konfigurasi_Mysql.tag_npm,isinpm);
                    params.put(Konfigurasi_Mysql.tag_nama,isinama);
                    params.put(Konfigurasi_Mysql.tag_password,isipass);
                    params.put(Konfigurasi_Mysql.tag_key,"udin");
                    params.put(Konfigurasi_Mysql.tag_jurusan,isijurusan);
                    params.put(Konfigurasi_Mysql.tag_imei,imei);



                    RequestHandler rh = new RequestHandler();
                    String res = rh.sendPostRequest(Konfigurasi_Mysql.url_register, params);


                    return res;
                }
            }

            Regis dreg = new Regis();
            dreg.execute();

        } else {
            Toast.makeText(Register.this, "Password tidak sama", Toast.LENGTH_SHORT).show();
        }

    }

}
