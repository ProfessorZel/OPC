package commander.professor.opc;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Integer.parseInt;


/**Класс Activity руководителя
 * @author Astachov Maxim */
public class P4 extends AppCompatActivity {

    /** Текущий контекст Activity */
    Context activity = (P4.this);

    /**Ссылка на файл ресурса с внешним видом Activity */
    int activit = R.layout.activity_p4;

    /**
     *Cоздание меню из ресурса menumain.xml
     * @param menu обьект типа меню
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menumain, menu);
        return true;
    }


    SharedPreferences sPref;

    private BroadcastReceiver UI_updater = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Integer action = intent.getIntExtra("action",0);
            switch (action){
                case 0:
                    ((TextView)findViewById(R.id.text)).setText(load("timetable"));
                    ((TextView)findViewById(R.id.textView5)).setText(load("timetable_author"));
                    break;
                case 1:
                    ArrayList<User_profile> names = user_list();
                    Spinner spinner = findViewById(R.id.spinner);
                    Spinner_adapter adapter_spiner = new Spinner_adapter(activity, names);
                    spinner.setAdapter(adapter_spiner);
                    break;
                case 2:
                    TextView textview = findViewById(R.id.textviewun);
                    textview.setText(load("msg"));
                    break;
                case 3:
                    Intent intent16 = new Intent(activity, loginxolop.class);//переход
                    startActivity(intent16);
                    break;
            }

        }
    };
    private void save(String slot, String text) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot, text);
        ed.apply();
    }
    public void remove(String slot){
        SharedPreferences sPref;
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.remove(slot);
        ed.apply();
    }
    private ArrayList<User_profile> user_list(){
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        String json = sPref.getString("users_json_list","");
        ArrayList<User_profile> names = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(json);
            for (int i =0; i<arr.length();i++){
                JSONObject user = arr.getJSONObject(i);
                names.add(new User_profile(user.getInt("id"),user.getString("name"),user.getString("rang"),user.getInt("age")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return names;
    }

    public String load(String slot) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        return sPref.getString(slot,"");}

    /** Провереряет запущен ли сервис
     * @param servname обьект сервиса
     * @return true если сервис запущен и false если не запущен
     */
    public boolean isMyServiceRunning(Class<?> servname) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (servname.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Производит действия при старте Activity
     * @param savedInstanceState обьект Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(activit);
        String name = load("name");
        setTitle(name.length() > 0 ? name:"ПриказПРО");


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }

        TabHost tabHost = findViewById(R.id.host);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Сообщения");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator("Расписание");
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);

        TextView textview = findViewById(R.id.textviewun);
        String text;
        if (!(text = load("msg")).equals("")){
            textview.setText(text);
        }

        if (!(isMyServiceRunning(Updater.class))) {
            Intent intent = new Intent(this, Updater.class);
            startService(intent);

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(UI_updater,
                new IntentFilter("new_info_received"));

        NotificationManager mNotificationManager1 =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager1 != null) {
            mNotificationManager1.cancel(102);
            mNotificationManager1.cancel(103);
        }

    }

    /**Обновляет контент при продолжении работы*/
    @Override
    protected void onResume() {
        super.onResume();
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.cancel();
        }
        NotificationManager mNotificationManager1 =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager1 != null) {
            mNotificationManager1.cancel(102);
            mNotificationManager1.cancel(103);
        }
        ((TextView)findViewById(R.id.text)).setText(load("timetable"));
        ((TextView)findViewById(R.id.textView5)).setText(load("timetable_author"));
        ArrayList<User_profile> names = user_list();
        Spinner spinner = findViewById(R.id.spinner);
        Spinner_adapter adapter_spiner = new Spinner_adapter(activity, names);
        spinner.setAdapter(adapter_spiner);
        TextView textview = findViewById(R.id.textviewun);
        textview.setText(load("msg"));
    }
    public  void F5(View v){
        timetable d = new timetable();
        d.execute();
    }



    /** Обработка нажатия на кнопку "Отправить" - отправка или вызов адресата
     * @param view обьект вызывающий класс
     */
    public void Send(View view) {
        Spinner spinner = findViewById(R.id.spinner); //селектор пользователей
        final String selected = ((TextView)spinner.getSelectedView().findViewById(R.id.sub_text)).getText().toString().replace(" ","").split(":")[1];
        EditText edit = findViewById(R.id.editText2); //поле ввода сообщения
        String msg = edit.getText().toString();
        if (msg.length()>0){
            Get call = new Get();
            call.execute(selected, msg);
        }
        edit.setText(""); //поле ввода сообщения к ""
    }
    public void regout(MenuItem item){
        remove("name");
        remove("rang");
        remove("visit_time");
        remove("lagID");
        remove("view");
        remove("msg");
        remove("timetable");
        remove("timetable_author");
        remove("users_json_list");
        remove("ms_tmp_marks");
        Intent intent9 = new Intent(activity, loginxolop.class);//переход
        startActivity(intent9);
    }
    public void myidgo (MenuItem d){
        profile sd = new profile();
        sd.execute(load("id"),load("name"));
    }


    /** Отправка сообщений серверу (server/msg.php) в отдельном потоке*/
    public class Get extends AsyncTask<String, Void, String> {
        /**иницаизация нового обьекта ProgressDialog в текущем Activity */
        ProgressDialog p = new ProgressDialog(activity);
        /**указатель на кнопку отправки или вызова*/
        Button buttononstartprof = (Button) findViewById(R.id.Senamsg);
        /** показывает ProgresDialog и делает кнопку не активной*/
        protected void onPreExecute() {

            buttononstartprof.setEnabled(false);
            p.setMessage("Отправка сообщения...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }


        @Override
        protected String doInBackground(String... Strings) {
            String a;

            a = "";
            try {
                if(Strings.length >= 2){
                    if (Strings[0].length() > 0) {
                        Strings[1] = Strings[1].replaceAll("call","саll");
                        //заготовка для коментированного вызова
                        if (hasConnection()) {
                            JSONObject msg = new JSONObject();
                            msg.put("lagID",load("lagID"));
                            msg.put("id",load("id"));
                            msg.put("to_id",parseInt(Strings[0]));
                            msg.put("msg",Strings[1]);
                            if (Strings.length == 3){
                                if(Strings[2].equals("call")){
                                    msg.put("iscall", true);
                                }else{
                                    msg.put("iscall",false);
                                }
                            }else{
                                msg.put("iscall",false);
                            }

                            String url = "http://camps.astachov.ru/msg.php?json_msg="+URLEncoder.encode(msg.toString(),"utf8");
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
                                sb.append(line).append("\r\n");
                            }
                            br.close();
                            a = sb.toString();
                            if (a.length() == 0) {
                                a = "Ошибка связи с сервером...\r\n";
                            }
                        } else {

                            a = "Error ,No Internet...\r\n";
                        }
                    }}
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return a;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            String msgfile;
            if (!((msgfile = load( "msg")).equals(""))) {
                if (msgfile.length() > 5000) {
                    msgfile = msgfile.substring(0, 5000);
                }
            }

            TextView textviewonstart = findViewById(R.id.textviewun);
            if (result.length() > 0) {

                Calendar cal = Calendar.getInstance();
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat sdf = new SimpleDateFormat("HH-mm-ss");
                String time = sdf.format(cal.getTime());
                String textus = "[" + time + "]" + result + msgfile;
                save( "msg", textus);

                textviewonstart.setText(textus);
            } else {
                textviewonstart.setText(msgfile);
            }


            p.dismiss();
            buttononstartprof.setEnabled(true);
        }


    }


    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;

        if (cm != null) {
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }

    /**Получение личного дела курсанта с сервера в отдельном потоке */
    private class profile extends AsyncTask<String, Void, String> {
        /**иницаизация нового обьекта ProgressDialog в текущем Activity */
        ProgressDialog p = new ProgressDialog(activity);
        /**Создает ProgressDialog*/
        protected void onPreExecute() {
            p.setMessage("Получение Л/Д...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(true);
            p.show();
        }

        /**Отправляет запрос серверу и получает личное дело
         * @param str строка вида группа:фио
         * @return личное дело курсанта
         */
        @Override
        protected String doInBackground(String... str) {
            String a = "Не удалось получить...";


            if (hasConnection()&& str.length==2) {
                try {
                    JSONObject d = new JSONObject();
                    d.put("lagID",load("lagID"));
                    d.put("id",parseInt(str[0]));
                    d.put("name",str[1]);

                    String url = "http://camps.astachov.ru/myid.php?json_myid="+URLEncoder.encode(d.toString(), "UTF-8");
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
                        sb.append(line).append("\n");
                    }
                    br.close();
                    a = sb.toString();
                    a = a.replaceAll("<br>", "\n");
                } catch (IOException | JSONException ignored) {
                    ignored.printStackTrace();
                }

            }
            return a;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try{
                Log.e("profile", result);
                JSONArray arr = new JSONArray(result);
                JSONObject user_data = arr.getJSONObject(0);
                String lk =""
                        .concat("ФИО: ")
                        .concat(user_data.getString("name"))
                        .concat("\n")
                        .concat("Ранг: ")
                        .concat(user_data.getString("rang"))
                        .concat("\n")
                        .concat("Возраст: ")
                        .concat(user_data.getString("age"))
                        .concat("\n")
                        .concat("Телефон: ")
                        .concat(user_data.getString("tel"))
                        .concat("\n")
                        .concat("ID:")
                        .concat(user_data.getString("id"))
                        .concat("\n")
                        .concat("Web-пароль (beta):")
                        .concat(user_data.getString("web_pass"))
                        .concat("\n")
                        .concat("Версия (спроси макса): ")
                        .concat(user_data.getString("last_visit"))
                        .concat("\n")
                        .concat("Особые отметки: \n")
                        .concat(user_data.getString("sm"));
                LinearLayout ll = new LinearLayout(activity);
                ll.setOrientation(LinearLayout.VERTICAL);

                final ListView listView= new ListView(activity);
                ArrayList<Courses_marks_ID> marks_list = new ArrayList<>();
                if (!arr.isNull(1)) {
                    try {
                        JSONArray marks = arr.getJSONArray(1);
                        for (int i =0; i<marks.length();i++){
                            JSONObject mark = marks.getJSONObject(i);
                            try{
                                marks_list.add(new Courses_marks_ID(mark.getInt("id"), mark.getString("date"), mark.getString("author"), mark.getString("name"), mark.getString("mark")));
                            }catch (JSONException ignored){
                            }
                        }
                    }catch (JSONException ignored){

                    }
                }
                Courses_marks_adapter ad = new Courses_marks_adapter(activity,marks_list);
                listView.setAdapter(ad);
                TextView textView = new TextView(activity);
                textView.setText(lk);
                textView.setTextSize(14);
                ll.addView(textView);
                ll.addView(listView);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                builder.setTitle("Личное дело")
                        .setView(ll)

                        .setCancelable(true)
                        .setPositiveButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }catch (JSONException e){
                e.printStackTrace();

                AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                builder.setTitle("Личное дело")
                        .setMessage("Не удалось получить.")
                        .setCancelable(true)
                        .setPositiveButton("ОК",

                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            p.dismiss();
        }


    }



    /**Поучает расписание с сервера в отдельном потоке*/
    class timetable extends AsyncTask<String, Void, String> {
        /**иницаизация нового обьекта ProgressDialog в текущем Activity */
        ProgressDialog p = new ProgressDialog(activity);
        /**Отображает ProgressDialog и делает кнопку не активной */
        protected void onPreExecute() {

            Button f;
            f = findViewById(R.id.f51);
            f.setEnabled(false);

            p.setMessage("Получение расписания...");
            p.setIndeterminate(false);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setCancelable(false);
            p.show();
        }

        /**Получает расписание с сервера(server/timetable.php) в отдельном потоке */
        @Override
        protected String doInBackground(String... s) {

            String a;
            a = "";

            if (hasConnection()) {
                try {
                    JSONObject zam = new JSONObject();
                    zam.put("lagID",load("lagID"));
                    if (s.length>0){
                        zam.put("new_timetable",s[0]);
                        zam.put("id",load("id"));
                    }
                    String url = "http://camps.astachov.ru/timetable.php?json_timetable="+URLEncoder.encode(zam.toString(),"Utf-8");//timetable
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
                        sb.append(line).append("\n");
                    }
                    br.close();
                    a = sb.toString();
                } catch (IOException ignored) {
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return a;
        }
        /**Обновляет контент TextView с расписанием или загружает из памяти предыдущее
         * @param result текст расписания полученого с сервера
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView texte = findViewById(R.id.text);
            TextView author_view = findViewById(R.id.textView5);
            String author;
            try {
                JSONObject rep = new JSONObject(result);
                result = rep.getString("timetable_text");
                author = rep.getString("timetable_author");

                save("timetable",result);
                save("timetable_author",author);

                texte.setText(result);
                author_view.setText(author);


                if (rep.has("status")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity); //Alert Dialog
                    builder.setTitle("Результат сохранения")
                            .setMessage(rep.getString("status"))
                            .setCancelable(true)
                            .setPositiveButton("ОК",

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();}
            } catch (JSONException e) {
                result = "Из памяти!\n"+load("timetable");
                texte.setText(result);
                author = load("timetable_author");
                author_view.setText(author);
                e.printStackTrace();
            }
            p.dismiss();
            Button f;
            f = findViewById(R.id.f51);
            f.setEnabled(true);
        }


    }

    /** Выполняет действия при разрушении Activity*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!(isMyServiceRunning(Updater.class))) {
            Intent intent = new Intent(this, Updater.class);
            startService(intent);

        }
    }
}