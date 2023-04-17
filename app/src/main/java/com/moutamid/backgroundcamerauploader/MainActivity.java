package com.moutamid.backgroundcamerauploader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    BroadCastReceiver bootUpReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Constants.checkApp(this);
        bootUpReceiver = new BroadCastReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bootUpReceiver, filter);

        Button btn = findViewById(R.id.start);
        if (isServiceRunning()) {
            btn.setText("Stop Service");
        }

        btn.setOnClickListener(v -> {
            if (isServiceRunning()) {
                btn.setText("Start Service");
                Intent intent = new Intent(this, PhotoMonitorService.class);
                stopService(intent);
                Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                        btn.setText("Stop Service");
                        Intent intent = new Intent(this, PhotoMonitorService.class);
                        startService(intent);
                    } else {
                        shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES);
                        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
                        shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_BOOT_COMPLETED);
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.POST_NOTIFICATIONS}, 1);
                    }
                } else {
                    if (
                            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
                        btn.setText("Stop Service");
                        Intent intent = new Intent(this, PhotoMonitorService.class);
                        startService(intent);
                    } else {
                        shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
                        shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_BOOT_COMPLETED);
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECEIVE_BOOT_COMPLETED, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }
            }
        });

    }

    public boolean isServiceRunning() {
        boolean s = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            if (PhotoMonitorService.class.getName().equals(runningService.service.getClassName())) {
                // The service is running
                // Do something here
                s = true;
                break;
            }
        }
        return s;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onStart() {
        super.onStart();
//                Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
    }
}