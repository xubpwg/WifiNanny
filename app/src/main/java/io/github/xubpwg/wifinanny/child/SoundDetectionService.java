package io.github.xubpwg.wifinanny.child;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import io.github.xubpwg.wifinanny.R;

public class SoundDetectionService extends Service {

    private static final String SD_SERVICE_TAG = "SoundDetectionService";

    public static final String ACTION_START_SOUND_DETECTION_SERVICE = "ACTION_START_SOUND_DETECTION_SERVICE";
    public static final String ACTION_STOP_SOUND_DETECTION_SERVICE = "ACTION_STOP_SOUND_DETECTION_SERVICE";

    public static final int SD_NOTE_ID = 101;

    private Looper soundDetectionLooper;
    private SoundDetectionHandler soundDetectionHandler;

    private AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 4096, 0);
    private PitchDetectionHandler pitchDetectionHandler;
    private AudioProcessor audioProcessor;

    private Notification notification;
    private String host;

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.

        HandlerThread sdThread = new HandlerThread("SoundDetection", Process.THREAD_PRIORITY_BACKGROUND);
        sdThread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        soundDetectionLooper = sdThread.getLooper();
        soundDetectionHandler = new SoundDetectionHandler(soundDetectionLooper);

        // Sound detection setup
        pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                Message pitchMessage = new Message();
                pitchMessage.obj = pitchInHz;
                pitchMessage.setTarget(soundDetectionHandler);
                pitchMessage.sendToTarget();
            }
        };
        audioProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 4096, pitchDetectionHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        host = intent.getStringExtra("HOST ADDRESS");
        Log.d(SD_SERVICE_TAG, "onStartCommand: host address is " + host);

        String action = intent.getAction();

        assert action != null;
        switch (action) {
            case ACTION_START_SOUND_DETECTION_SERVICE:
                startForegroundService();
                Log.d(SD_SERVICE_TAG, "onStartCommand: service started");
                break;
            case ACTION_STOP_SOUND_DETECTION_SERVICE:
                stopForegroundService();
                Log.d(SD_SERVICE_TAG, "onStartCommand: service stopped");
                break;
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void startForegroundService() {

        Intent notificationIntent = new Intent(this, ChildActivity.class);
        notificationIntent.putExtra("START_FROM_SERVICE", 100);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = createSdNoteChannel();
            notification =
                    new NotificationCompat.Builder(this, channel.getId())
                            .setContentTitle(getText(R.string.sd_note_title))
                            .setContentText(getText(R.string.sd_note_text))
                            .setSmallIcon(R.drawable.sd_service_icon)
                            .setContentIntent(pendingIntent)
                            .build();
        } else {
            notification =
                    new NotificationCompat.Builder(this)
                            .setContentTitle(getText(R.string.sd_note_title))
                            .setContentText(getText(R.string.sd_note_text))
                            .setSmallIcon(R.drawable.sd_service_icon)
                            .setContentIntent(pendingIntent)
                            .build();
        }

        audioDispatcher.addAudioProcessor(audioProcessor);
        new Thread(audioDispatcher, "sound detection thread").start();

        startForeground(SD_NOTE_ID, notification);

        Toast.makeText(this, "Monitoring is started.", Toast.LENGTH_SHORT).show();
    }

    private void stopForegroundService() {
        audioDispatcher.stop();
        audioDispatcher.removeAudioProcessor(audioProcessor);

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();

        Toast.makeText(this, "Monitoring is stopped.", Toast.LENGTH_SHORT).show();
    }

    // Handler that shows all freqs from the thread
    private final class SoundDetectionHandler extends Handler {

        public SoundDetectionHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Update notification with frequency detected from mic
            try {
                Intent notificationIntent = new Intent(getApplicationContext(), ChildActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                Notification sdNotification;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = createSdNoteChannel();
                    sdNotification =
                            new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                                    .setContentTitle(getText(R.string.sd_note_title))
                                    .setContentText(msg.obj + " Hz")
                                    .setSmallIcon(R.drawable.sd_service_icon)
                                    .setContentIntent(pendingIntent)
                                    .build();
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.notify(SD_NOTE_ID, sdNotification);
                } else {
                    sdNotification =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setContentTitle(getText(R.string.sd_note_title))
                                    .setContentText(msg.obj + " Hz")
                                    .setSmallIcon(R.drawable.sd_service_icon)
                                    .setContentIntent(pendingIntent)
                                    .build();
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.notify(SD_NOTE_ID, sdNotification);
                }

                if ((float) msg.obj >= 400 && (float) msg.obj <= 600) {
                    sendAlertToParent();
                }
            } catch (Exception e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
        }

        private void sendAlertToParent() {
            try {
                Log.d(SD_SERVICE_TAG, "sendAlertToParent: called");
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, 13910), 500);

                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                outputStream.writeUTF("alert");
                outputStream.flush();

                socket.close();
            } catch (IOException e) {
                e.fillInStackTrace();
            }
        }

    }

    private NotificationChannel createSdNoteChannel() {
        NotificationChannel channel = null;
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SoundDetectionChannel";
            String description = "sound detection channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            channel = new NotificationChannel("sd_chan", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        return channel;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
