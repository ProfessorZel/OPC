package ru.astakhovmd.cadet;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.sleep;

/**Сервис получения новых сообщений */
public class Updater extends Service {

    SharedPreferences sPref;
    Integer period = 15;
    Integer loop = 0;
    Integer min_time = 0;

    final int EC_NO_MSG = 0;
    final int EC_SYS_MSG = 1;
    final int EC_MSG = 2;

    final Handler h = new Handler();
    Runnable run = new Runnable() {
        @Override
        public void run() {
            Get gg = new Get();
            gg.execute();
        }
    };
    private void save(String slot, String text) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(slot, text);
        ed.apply();
    }


    /** извлекает строку из файла насторек
     * @param slot имя настройки в файле arr
     * @return строку или если отсутствует настройка то null
     */
    public String load(String slot) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        return sPref.getString(slot, "");

    }

    private void save_user_list(String json) {
        sPref = getSharedPreferences("myapp", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("users_json_list", json);
        ed.apply();
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = getResources().getString(R.string.app_name);
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
    /** Выполняет команды при старте сервиса
     * @param intent намерения (входные данные)
     * @param flags флаг запуска- указывает тип запуска 0, START_FLAG_RETRY или START_FLAG_REDELIVERY
     * @param startId идентификатор сервиса в системе
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //These three lines makes Notification to open main activity after clicking on it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else{
            Intent notificationIntent = new Intent(this, loginxolop.class);
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getResources().getString(R.string.app_name)) //Заголовок
                    .setContentText("Ожидание новых оповещений") // Текст уведомления
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_HIGH);
            Notification notification;
            notification = builder.build();
            startForeground(111,notification);}


        Get gg = new Get();
        gg.execute();
        return START_STICKY;
    }

    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo;
        if (cm != null) {
            wifiInfo = cm.getActiveNetworkInfo();
            return wifiInfo != null && wifiInfo.isConnected();
        }
        return false;
    }
    /** Класс взаимодействия с сервисом*/

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Intent restartIntent = new Intent(this, getClass());

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getService(this, 1, restartIntent,
                PendingIntent.FLAG_ONE_SHOT);
        if (am != null) {
            am.setExact(AlarmManager.RTC, System.currentTimeMillis() + 3000, pi);
        }

    }
    void setPeriod(int exit_code){
        switch (exit_code){
            case EC_NO_MSG:
                if (loop >=4){
                    if (period == min_time+15){
                        period = min_time+30;
                    }else
                    if (period == min_time+30){
                        period = min_time+60;
                    }else
                    if (period == min_time+60){
                        period = min_time+120;
                    }else
                    if (period == min_time+120){
                        period = min_time+300;
                    }else
                    if (period >= min_time+300){
                        Calendar cal = Calendar.getInstance();
                        int hourofday = cal.get(Calendar.HOUR_OF_DAY);
                        if (hourofday>=23 || hourofday <= 7){
                            period = min_time+600;
                        }else{
                            period = min_time+300;
                        }
                    }
                    loop = 0;
                }
                break;
            case EC_SYS_MSG:
                loop = 0;
                period = 120;
                break;
            case EC_MSG:
                loop = 0;
                period =15;
                break;
        }
        loop++;

    }
    void alram(int type, String stmsg) {
        switch (type){
            case 0:
                Intent notificationIntent = new Intent(this, loginxolop.class);
                notificationIntent.setAction(Intent.ACTION_MAIN);
                notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                builder.setContentIntent(contentIntent)
                        .setAutoCancel(true)  //Can't be swiped out
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setPriority(Notification.PRIORITY_DEFAULT)
                        .setContentTitle(getResources().getString(R.string.app_name)) //Заголовок
                        .setContentText(stmsg) // Текст уведомления
                        .setWhen(System.currentTimeMillis());

                Notification notification;
                notification = builder.build();
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager != null) {
                    mNotificationManager.notify(103, notification);
                }
                break;
            case 1:
                Intent notificationIntent1 = new Intent(this, loginxolop.class);
                notificationIntent1.setAction(Intent.ACTION_MAIN);
                notificationIntent1.addCategory(Intent.CATEGORY_LAUNCHER);

                PendingIntent contentIntent1 = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent1, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder builder1 = new Notification.Builder(this);
                builder1.setContentIntent(contentIntent1)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)//Can't be swiped out
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Команда вызова!") //Заголовок
                        .setContentText(stmsg) // Текст уведомления
                        .setWhen(System.currentTimeMillis());

                Notification notification1;
                notification1 = builder1.build();
                NotificationManager mNotificationManager1 =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (mNotificationManager1 != null) {
                    mNotificationManager1.notify(102, notification1);
                }
                Call_animation d = new Call_animation();
                d.execute();
                break;

        }
    }
    /**Сервис получения сообщений в фоне */
    private class Get extends AsyncTask<Void, Void, String> {
        void remove(String slot){
            SharedPreferences sPref;
            sPref = getSharedPreferences("myapp", MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.remove(slot);
            ed.apply();
        }


        @Override
        protected String doInBackground(Void... params) {
            String msg="";


            if (hasConnection()) {
                try {
                    JSONObject req = new JSONObject();
                    req.put("id",load("id"));
                    req.put("visit_time",load("visit_time"));
                    req.put("lagID",load("lagID"));
                    String url = "https://camps.astachov.ru/msg.php?json_msg="+ URLEncoder.encode(req.toString(), "UTF-8");
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
                    msg = sb.toString();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }


            }else{
                //stopSelf();
            }
            return msg;

        }

        @Override
        protected void onPostExecute(String json_msg) {
            String msg_file = load("msg");
            Integer exit_code = EC_NO_MSG;
            if (json_msg.length()>0){
                try {
                    JSONArray result = new JSONArray(json_msg);
                    save("visit_time",result.getString(0));
                    Boolean call = false;
                    Boolean notify = false;
                    String call_msg ="";
                    JSONArray msgs = result.getJSONArray(1);
                    for(int i=0;i<msgs.length();i++){

                        JSONObject msg = msgs.getJSONObject(i);
                        Integer sender_id;
                        if ((sender_id=msg.getInt("from"))<0){
                            if(sender_id==-1){
                                tasks(msg.getString("msg"));
                                exit_code = (exit_code==EC_MSG)?EC_MSG:EC_SYS_MSG;
                            }
                        }else{
                            exit_code = EC_MSG;
                            if (msg.getBoolean("call")){
                                call_msg = call_msg.concat(","+msg.getString("name"));
                                msg_file = ("@["+msg.getString("datetime")+"]("+msg.getString("from")+","+msg.getString("name")+"):"+msg.getString("msg")+"\n").concat(msg_file);
                                call = true;
                            }else{
                                msg_file = ("["+msg.getString("datetime")+"]("+msg.getString("from")+","+msg.getString("name")+"):"+msg.getString("msg")+"\n").concat(msg_file);
                                notify = true;
                            }
                        }
                    }
                    if (call){
                        alram(1,call_msg);
                        sendMessage(2);
                    }else if (notify){
                        alram(0,"("+msgs.length()+")Cообщения!");
                        sendMessage(2);
                    }
                } catch (JSONException ignored) {
                    exit_code = EC_NO_MSG;
                    //ignored.printStackTrace();
                    Log.e("json_err->",json_msg);
                }}
            setPeriod(exit_code);
            save("msg",msg_file);
            Log.e("handler",loop+":"+period);
            h.postDelayed(run,period*1000);
        }



        void tasks(String json){

            try {
                JSONObject arr = new JSONObject(json);
                int task_id = arr.getInt("action");
                String parms = arr.getString("json");
                switch (task_id){
                    case 0:
                        try {
                            String author;
                            String result;
                            JSONObject rep = new JSONObject (parms);
                            result = rep.getString("timetable_text");
                            author = rep.getString("timetable_author");

                            save("timetable",result);
                            save("timetable_author",author);
                            sendMessage(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            new JSONArray (parms);
                            save_user_list(parms);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendMessage(1);
                        break;
                    case 66:
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
                        sendMessage(3);
                        stopSelf();
                        break;
                    case 2:
                        min_time = parseInt(parms);
                        loop =99;
                        setPeriod(EC_NO_MSG);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private void sendMessage(int action_id) {
            Intent intent = new Intent("new_info_received");
            // You can also include some extra data.
            intent.putExtra("action", action_id);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }
    /**Сервис получения сообщений в фоне */
    private class Call_animation extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.a6);
            mp.setVolume(1,1);
            for (int i =0; i<4;i++){
                if (vibrator != null) {
                    vibrator.vibrate(3000);
                }
                mp.start();

                try {
                    sleep(4500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}