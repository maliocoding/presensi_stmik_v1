package com.example.julio.presensi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class Login extends AppCompatActivity {

    TelephonyManager telephonyManager;

    //declarasi db
    DBHelper myDB;
    String imei = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myDB=new DBHelper(this);

    }

    private void deviceId() {
        telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
            return;
        }
    }


    public void Signin(View view) {

        final EditText npm=findViewById(R.id.editNPM);
        final EditText password=findViewById(R.id.editPassword);
        deviceId();

//
        class CekLogin extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;
            final String isiNpm=npm.getText().toString();
            final String isiPass=password.getText().toString();

            String hasil;
            String status;
            String nama;



            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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
                loading = ProgressDialog.show(Login.this,"Proses...","Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                hasil=s;
                if(hasil!=null){
                    // Do you work here on success
                    JSONObject jsonObject;
                    try{
                        jsonObject = new JSONObject(hasil);
                        JSONArray result = jsonObject.getJSONArray(Konfigurasi_Mysql.TAG_JSON_ARRAY);
                        JSONObject jo = result.getJSONObject(0);
                        status = jo.getString("status");
                        nama=jo.getString("nama");
                    }catch (JSONException e){
                       e.printStackTrace();
                        Toast.makeText(Login.this,"aduh error", Toast.LENGTH_LONG).show();
                    }
                    loading.dismiss();
//
                    if (status!=null && status.equals("sukses")){


                    Cursor hasil = myDB.getAllData();
                        if(hasil.getCount() == 0) {
                            // show message
                           // Toast.makeText(Login.this,"kosong pak eko",Toast.LENGTH_LONG).show();
                            //insert ke sql lite
                            boolean isInserted = myDB.insertData(isiNpm,nama,"1" );
                            if(isInserted == true) {
                              //  Toast.makeText(Login.this, "masuk pak eko", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Login.this, Dashboard.class);
                                finish();
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(Login.this, "gagal dong", Toast.LENGTH_LONG).show();
                            }
                        }
                    }else{
                        Toast.makeText(Login.this, status, Toast.LENGTH_LONG).show();


                    }
                }else{
                    Toast.makeText(Login.this, "nothing return", Toast.LENGTH_LONG).show();
                }

                loading.dismiss();

            }

            @Override
            protected String doInBackground(Void... v) {
                HashMap<String,String> params = new HashMap<>();
                params.put(Konfigurasi_Mysql.tag_npm,isiNpm);
                params.put(Konfigurasi_Mysql.tag_password,isiPass);
                params.put(Konfigurasi_Mysql.tag_key,"udin");
                params.put(Konfigurasi_Mysql.tag_imei,imei);

//                try {
//                    URL url = new URL(webservis);
//                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                    StringBuilder sb = new StringBuilder();
//                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                    String json;
//                    while ((json = bufferedReader.readLine()) != null) {
//                        sb.append(json + "\n");
//                    }
//                    return sb.toString().trim();
//                } catch (Exception e) {
//                    return null;
//                }



                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest(Konfigurasi_Mysql.URL_LOGIN, params);


                return  res;

            }
        }
        CekLogin cl = new CekLogin();
        cl.execute();
//        Intent intent = new Intent(Login.this, Dashboard.class);
//        startActivity(intent);
    }


    public void Register(View view) {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);

    }
}
