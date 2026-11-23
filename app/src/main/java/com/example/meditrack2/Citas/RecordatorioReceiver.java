package com.example.meditrack2.Citas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.meditrack2.R;

public class RecordatorioReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID="meditrack_recordatorios";
    @Override public void onReceive(Context context, Intent intent) {
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT>=26) manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,"Recordatorios",NotificationManager.IMPORTANCE_HIGH));
        Intent abrir=new Intent(context,Recordatorios.class);
        PendingIntent pendiente=PendingIntent.getActivity(context,0,abrir,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder aviso=new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.logo).setContentTitle("Recordatorio MediTrack")
                .setContentText(intent.getStringExtra("mensaje")).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true).setContentIntent(pendiente);
        manager.notify(intent.getIntExtra("id",(int)System.currentTimeMillis()),aviso.build());
    }
}
