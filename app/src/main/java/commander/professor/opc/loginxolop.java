package commander.professor.opc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class loginxolop extends AppCompatActivity {
    public EditText accesskey,fioform,telform;


    SharedPreferences sPref;
    private void save(String slot, String text) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot,text);
        ed.apply();
    }
    public String load(String arr,String slot) {
        sPref = getSharedPreferences(arr, MODE_PRIVATE);
        if (sPref.contains(slot)) {
            return sPref.getString(slot, "");}
        return "";
    }



    @Override
    protected void onResume() {
        super.onResume();
        Ti();

    }
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginxolop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        accesskey = findViewById(R.id.compin);
        fioform = findViewById(R.id.editText);
        telform = findViewById(R.id.editText3);

        //This method is called to notify you that, within s,
        // the count characters beginning at start have just replaced old text that had length before.
        // It is an error to attempt to make changes to s from this callback.
        final TextWatcher tellistner = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() < 2) {
                    telform.setText("8(");
                    telform.setSelection(2);
                }
                if (s.length() == 5 && count > before) {
                    telform.setText(s + ")");
                    telform.setSelection(6);
                }
                if (s.length() == 9 && count > before) {
                    telform.setText(s + "-");
                    telform.setSelection(10);
                }
                if (s.length() == 12 && count > before) {
                    telform.setText(s + "-");
                    telform.setSelection(13);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        telform.addTextChangedListener(tellistner);
        Ti();
    }

void Ti(){
        String savekey;
        if(!((savekey =load("myapp","view")).equals(""))) {
                    switch (savekey) {//выбор вариантов перехода
                        case "Otd1":
                            Intent intent12 = new Intent(loginxolop.this, P1.class);//переход
                            startActivity(intent12);//переход
                            Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_LONG).show();
                            break;
                        case "Otd2":
                            Intent intent13 = new Intent(loginxolop.this, P2.class);//переход
                            startActivity(intent13);//переход
                            Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_LONG).show();
                            break;
                        case "Otd3":
                            Intent intent14 = new Intent(loginxolop.this, P3.class);//переход
                            startActivity(intent14);//переход
                            Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_LONG).show();
                            break;
                        case "Otd4":
                            Intent intent15 = new Intent(loginxolop.this, P4.class);//переход
                            startActivity(intent15);//переход
                            Toast.makeText(this, "Автовход выполнен...", Toast.LENGTH_LONG).show();
                            break;
                        //еще case сюда!
                    }//стоп выбора
                 }
    }
    //автовход

    public void Trans(View view){
        EditText accesskey;
        accesskey = findViewById(R.id.compin);
        fioform = findViewById(R.id.editText);
        telform = findViewById(R.id.editText3);
        Spinner ageform= findViewById(R.id.spinner2);
        String fio,tel;
        int age, pos = ageform.getSelectedItemPosition();
        fio=fioform.getText().toString();
        tel=telform.getText().toString();
        String key = (accesskey.getText().toString());
        EditText viewladID = findViewById(R.id.editText5);
        String ladID = viewladID.getText().toString();
        if(key.length()==8&&fio.length()>3&&tel.length()==15&&pos!=0&&ladID.length()==4){
            save("version","0");
            save("lagID",ladID);
            save("name",fio);

            age=pos+5;
            reg dd = new reg();
            dd.execute(key,fio,age+"",tel,ladID);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(loginxolop.this);
            builder.setTitle(R.string.sgw)
                    .setMessage(R.string.empty)
                    .setCancelable(false)
                    .setNegativeButton("ОK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    private class reg extends AsyncTask<String, Void, String> {

        boolean hasConnection() {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo;
            if (cm != null) {
                wifiInfo = cm.getActiveNetworkInfo();
                return wifiInfo != null && wifiInfo.isConnected();
            }
            return false;
        }
        @Override
        protected String doInBackground(String... s) {
            String a;
            a = "";

            if (hasConnection()) {
                try {
                    JSONObject d = new JSONObject();
                    d.put("lagID",s[4]);
                    d.put("name",s[1]);
                    d.put("age",s[2]);
                    d.put("tel",s[3]);
                    d.put("key",s[0]);
                    String url = "http://camps.astachov.ru/reg.php?json_reg="+URLEncoder.encode(d.toString(), "UTF-8");
                    HttpURLConnection connect = (HttpURLConnection) new URL(url).openConnection();
                    connect.setUseCaches(false);
                    connect.setAllowUserInteraction(true);
                    connect.connect();
                    connect.setConnectTimeout(7000);

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            connect.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();
                    a = sb.toString()+"--"+url;
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return a;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            AlertDialog.Builder builder = new AlertDialog.Builder(loginxolop.this);
            try {
                JSONArray arr_json = new JSONArray(result);
                save("view",arr_json.get(0).toString());
                save("rang",arr_json.get(1).toString());
                save("id",arr_json.get(2).toString());
                builder.setTitle("Ответ сервера:")
                        .setMessage("Регистрация пройдена!")
                        .setCancelable(false)
                        .setIcon(R.mipmap.ic_launcher)
                        .setNegativeButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Ti();
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            } catch (JSONException e) {
                e.printStackTrace();
                builder.setTitle(R.string.sgw)
                        .setMessage("Ошибка. Попробуйте снова! \n")
                        .setIcon(R.drawable.alert_icon)
                        .setCancelable(false)
                        .setNegativeButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }


    }


    }


