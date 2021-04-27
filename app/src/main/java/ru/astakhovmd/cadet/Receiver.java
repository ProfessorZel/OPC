package ru.astakhovmd.cadet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("reciever_pp","Intent recived");
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("reciever_pp","Start Updater");
            Intent intentService = new Intent(context, Updater.class);
            context.startService(intentService);
        }
    }
}