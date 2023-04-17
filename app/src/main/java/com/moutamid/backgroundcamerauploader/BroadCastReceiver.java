package com.moutamid.backgroundcamerauploader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "Service Started", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(context, PhotoMonitorService.class);
            context.startService(i);
        }
    }

}
