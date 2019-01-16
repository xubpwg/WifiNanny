package io.github.xubpwg.wifinanny.parent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import io.github.xubpwg.wifinanny.R;

public class AlertHandlingService extends Service {

    private static final String AH_SERVICE_TAG = "AlertHandlingService";

    public static final String ACTION_START_ALERT_HANDLING_SERVICE = "ACTION_START_ALERT_HANDLING_SERVICE";
    public static final String ACTION_STOP_ALERT_HANDLING_SERVICE = "ACTION_STOP_ALERT_HANDLING_SERVICE";

    public static final int AH_NOTE_ID = 201;
    public static final int ALERT_NOTE = 202;

    private Looper alertLooper;
    private AlertEventHandler alertHandler;
    private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread alertThread = new HandlerThread("AlertMessages",
                Process.THREAD_PRIORITY_BACKGROUND);
        alertThread.start();

        ParentHostThread parentHostThread = new ParentHostThread();
        parentHostThread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        alertLooper = alertThread.getLooper();
        alertHandler = new AlertEventHandler(alertLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            assert action != null;
            switch (action) {
                case ACTION_START_ALERT_HANDLING_SERVICE:
                    startForegroundService();
                    Log.d(AH_SERVICE_TAG, "onStartCommand: service started");
                    break;
                case ACTION_STOP_ALERT_HANDLING_SERVICE:
                    stopForegroundService();
                    Log.d(AH_SERVICE_TAG, "onStartCommand: service stopped");
                    break;
            }
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void startForegroundService() {
        Intent notificationIntent = new Intent(this, ParentActivity.class);
        notificationIntent.putExtra("START_FROM_SERVICE", 200);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = createAlertServiceChannel();
            notification =
                    new NotificationCompat.Builder(this, channel.getId())
                            .setContentTitle(getText(R.string.ah_note_title))
                            .setContentText(getText(R.string.ah_note_text))
                            .setSmallIcon(R.drawable.ah_service_icon)
                            .setContentIntent(pendingIntent)
                            .build();
        } else {
            notification =
                    new NotificationCompat.Builder(this)
                            .setContentTitle(getText(R.string.ah_note_title))
                            .setContentText(getText(R.string.ah_note_text))
                            .setSmallIcon(R.drawable.ah_service_icon)
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .build();
        }

        startForeground(AH_NOTE_ID, notification);

        Toast.makeText(this, "Monitoring is started.", Toast.LENGTH_SHORT).show();
    }

    private void stopForegroundService() {
        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();

        Toast.makeText(this, "Monitoring is stopped.", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ParentHostThread extends Thread{

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress("0.0.0.0", 8888));
                Socket clientSocket = serverSocket.accept();

                DataInputStream clientStream = new DataInputStream(clientSocket.getInputStream());
                String clientMessage = clientStream.readUTF();

                if (clientMessage.equals("alert")) {
                    Message msg = new Message();
                    msg.setTarget(alertHandler);
                    msg.sendToTarget();
                }

                clientSocket.close();
                serverSocket.close();

                Thread.currentThread().join();

            } catch (IOException e) {
                e.fillInStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Handler that shows alert message when "child" freqs occurred.
    private final class AlertEventHandler extends Handler {

        public AlertEventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Log.d(AH_SERVICE_TAG, "handleMessage: called");
                Notification alertNote;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = createAlertNotesChannel();
                    alertNote =
                            new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                                    .setContentTitle(getText(R.string.alert_note_title))
                                    .setStyle(
                                            new NotificationCompat.BigTextStyle()
                                                    .bigText(getText(R.string.alert_note_text))
                                                    .setBigContentTitle(getText(R.string.alert_note_title)))
                                    .setSmallIcon(R.drawable.alert_note_icon)
                                    .setOnlyAlertOnce(true)
                                    .setAutoCancel(true)
                                    .build();
                } else {
                    alertNote =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setContentTitle(getText(R.string.alert_note_title))
                                    .setStyle(
                                            new NotificationCompat.BigTextStyle()
                                                    .bigText(getText(R.string.alert_note_text))
                                                    .setBigContentTitle(getText(R.string.alert_note_title)))
                                    .setSmallIcon(R.drawable.alert_note_icon)
                                    .setOnlyAlertOnce(true)
                                    .setAutoCancel(true)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .build();
                }

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.notify(ALERT_NOTE, alertNote);

                ParentHostThread parentHostThread = new ParentHostThread();
                parentHostThread.start();

            } catch (Exception e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
        }
    }

    private NotificationChannel createAlertNotesChannel() {
        NotificationChannel channel = null;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AlertNotesChannel";
            String description = "alert notes channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel("an_chan", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }

    private NotificationChannel createAlertServiceChannel() {
        NotificationChannel channel = null;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "AlertServiceChannel";
            String description = "alert service channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel("as_chan", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }
}
