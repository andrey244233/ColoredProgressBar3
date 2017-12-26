package com.example.home_pc.coloredprogressbar3;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.example.home_pc.coloredprogressbar3.MainActivity.INTENT_WITH_PROGRESS;
import static com.example.home_pc.coloredprogressbar3.MainActivity.INTENT_WITH_PROGRESS_WICH_RETURN;
import static com.example.home_pc.coloredprogressbar3.MainActivity.PENDING_INTENT;
import static com.example.home_pc.coloredprogressbar3.MainActivity.PENDING_RESULT;
import static com.example.home_pc.coloredprogressbar3.MainActivity.STATUS_FINISH;
import static com.example.home_pc.coloredprogressbar3.MainActivity.STATUS_RETURN;
import static com.example.home_pc.coloredprogressbar3.MainActivity.bound;


public class MyService extends Service {

    ExecutorService executorService;
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    Notification notification;
    int mProgress;
    Intent myInt;
    MyTask myTask;
    PendingIntent pendingIntent;

    public MyBinder binder = new MyBinder();

    public void onCreate() {
        super.onCreate();
        executorService = Executors.newFixedThreadPool(1);
        myTask = new MyTask();
    }

    class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        pendingIntent = intent.getParcelableExtra(PENDING_INTENT);
        int progress = intent.getIntExtra(INTENT_WITH_PROGRESS, 0);
        myTask.pendingIntent = pendingIntent;
        myTask.progress = progress;
        executorService.execute(myTask);
        //createNotification();
        Log.v("tag", "it is the next time I'm do it ");
        return binder;
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
        pendingIntent = intent.getParcelableExtra(PENDING_INTENT);
        int progress = intent.getIntExtra(INTENT_WITH_PROGRESS, 0);
        //myTask = new MyTask(pendingIntent, progress);
        myTask.pendingIntent = pendingIntent;
        myTask.progress = progress;
        Log.v("tag", "PROGRESS = " + progress);
        executorService.execute(myTask);
      //  createNotification();

        Log.v("tag", "it is the next time I'm do it ");
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
//        return super.onUnbind(intent);
    }

    private void createNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(MyService.this);
        String channelId = "some_channel_id";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = "some_channel_name";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_WITH_PROGRESS_WICH_RETURN, mProgress);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 200, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent)
                .setChannelId(channelId)
                .setSmallIcon(R.drawable.tree)
                .setContentTitle("Task implementing");
    }

    private void showNotification(int i) {



        notification = builder.build();
        notificationManager.notify(1, notification);
        builder.setProgress(18, i, false);
        startForeground(1, notification);
    }

    class MyTask implements Runnable {
        PendingIntent pendingIntent;
        int progress;

        MyTask(PendingIntent pendingIntent, int progress) {
            this.pendingIntent = pendingIntent;
            this.progress = progress;
        }

        public MyTask() {
        }

        @Override
        public void run() {
                createNotification();
                for (int i = progress; i < 19; i++) {
                    Log.v("tag", "run RUN RUN " + i);
                    mProgress = i;
                    Intent intent = new Intent();
                    intent.putExtra(PENDING_RESULT, i);

                    // createNotification();

                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    try {
                        pendingIntent.send(MyService.this, STATUS_FINISH, intent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }

                    showNotification(i);

                    if (i == 18) {
                        notificationManager.cancel(1);
                        //stopService(intent);
                    }
                }

        }
    }
    
}

