package com.example.julio.presensi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Dashboard extends AppCompatActivity {
    IntentIntegrator intentIntegrator;

    String ruangan;
    String matkul;
    String tanggal;
    String hasil;
    String nama;
    String npm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        DBHelper myDB;
        myDB=new DBHelper(this);
        Cursor cursor;




        try {
//               int stat= hasil.getInt( hasil.getColumnIndex("status"));
            SQLiteDatabase db = myDB.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM user ", null);
            cursor.moveToFirst();

            cursor.moveToPosition(0);
             npm= cursor.getString(1);
            nama= cursor.getString(2);



        }catch(Exception e){

            Toast.makeText(this, "ada error" , Toast.LENGTH_SHORT).show();
        }

        TextView tnama=findViewById(R.id.edNama);
        tnama.setText(nama);
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        JSONObject jsonObject;
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);


        if (result != null){
            if (result.getContents() == null){
                Toast.makeText(this, "Hasil tidak ditemukan", Toast.LENGTH_SHORT).show();
            }else{
                // jika qrcode berisi data
                try{
                    //decrypt
                   byte[] temp= Base64.decode(result.getContents(), Base64.DEFAULT);
                    String text = new String(temp, "UTF-8");
                   byte[] temp2=Base64.decode(text, Base64.DEFAULT);

                    hasil=new String(temp2, "UTF-8");
                    // converting the data json
                    jsonObject = new JSONObject(hasil);
                    JSONArray res= jsonObject.getJSONArray("result");
                    JSONObject jo = res.getJSONObject(0);
                    // atur nilai ke textviews
                    ruangan=jo.getString("ruangan");
                    matkul=jo.getString("matkul");
                    tanggal=jo.getString("tanggal");


                    KirimAbsen ka = new KirimAbsen();
                    ka.execute();
//                      Toast.makeText(this, nama, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(this, npm, Toast.LENGTH_SHORT).show();

                 


//



                }catch (JSONException e){
                   Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                   e.printStackTrace();
                    // jika format encoded tidak sesuai maka hasil
                    // ditampilkan ke toast

                } catch (UnsupportedEncodingException e) {
                  e.printStackTrace();
                    Toast.makeText(this, "gagal decode", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            Toast.makeText(this, "result null", Toast.LENGTH_SHORT).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void QRcode(View view) {
        // inisialisasi IntentIntegrator(scanQR)
        intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }

    public void Logout(View view) {
        // inisialisasi DBHelper
        final DBHelper myDB;
        myDB=new DBHelper(this);

        //Alert konfirmasi
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Alert");
        builder.setMessage("Anda yakin akan logout?");
        builder.setPositiveButton("Ya",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //hapus db di android lalu balik ke mainactivity
                        int hasil=myDB.deleteAllData();
                        if(hasil>0){
                            Toast.makeText(Dashboard.this, "Anda Berhasil Logout", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Dashboard.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }





//    public static String enkripsi(String pesan, String key){
//        try {
//            SecretKeySpec KS = new SecretKeySpec(key.getBytes(), "Blowfish");
//            Cipher cipher = Cipher.getInstance("Blowfish");
//            cipher.init(Cipher.ENCRYPT_MODE, KS);
//            byte[] encrypted = cipher.doFinal(pesan.getBytes());
//            return Base64.encodeToString(encrypted, Base64.NO_PADDING);
//        } catch (Exception e) {
//            return "ERROR:"+e.getMessage();
//        }
//    }
//
//    public static String dekripsi(String chiperText, String key){
//        try {
//
//            SecretKeySpec KS = new SecretKeySpec(key.getBytes(), "Blowfish");
//            Cipher cipher = Cipher.getInstance("Blowfish");
//            cipher.init(Cipher.DECRYPT_MODE, KS);
//            byte[] decrypted = cipher.doFinal(Base64.decode(chiperText, Base64.NO_PADDING));
//            return new String(decrypted);
//        } catch (Exception e) {
//            return "ERROR";
//        }
//    }

    class KirimAbsen extends AsyncTask<Void,Void,String> {
        ProgressDialog loading;

        String hasil;
        String id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(Dashboard.this,"Proses...","Tunggu...",false,false);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hasil = s;
            if(hasil!=null) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(hasil);
                    JSONArray result = jsonObject.getJSONArray(Konfigurasi_Mysql.TAG_JSON_ARRAY);
                    JSONObject jo = result.getJSONObject(0);
                    id = jo.getString("status");
                    loading.dismiss();
                        if (id!=null) {
                            String msg = "Anda Berhasil Absen Di Mata Kuliah : ";
                            String msg2 = msg + matkul;
                            Toast.makeText(Dashboard.this, id, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(Dashboard.this, "ada error dari json", Toast.LENGTH_SHORT).show();
                        }

                } catch (JSONException e) {
                    Toast.makeText(Dashboard.this, "ada error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                }
            }else{
                    Toast.makeText(Dashboard.this, "nothing return", Toast.LENGTH_LONG).show();
                }


        }
        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String,String> params = new HashMap<>();
            params.put("npm",npm);
            params.put("nama",nama);
            params.put("key","udin");
            params.put("ruangan",ruangan);
            params.put("matkul",matkul);
            params.put("tanggal",tanggal);

            RequestHandler rh = new RequestHandler();
            String hasil_kirim = rh.sendPostRequest(Konfigurasi_Mysql.url_absen, params);
            return hasil_kirim;
        }
    }

}

