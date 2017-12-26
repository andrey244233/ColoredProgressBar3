package com.example.home_pc.coloredprogressbar3;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.PendingIntent.getService;


public class MainActivity extends AppCompatActivity {

    public  static boolean bound = false;
    ServiceConnection sConn;
    private static PendingIntent pendingIntent;
    public static final int REQUEST_CODE = 100;
    public static final int TASK_CODE = 100;
    public final static int STATUS_FINISH = 200;
    public final static int STATUS_RETURN = 300;
    public static final String PENDING_INTENT = "pendingIntent";
    public static final String PENDING_RESULT = "pendingResult";
    public static final String INTENT_WITH_PROGRESS = "PROGRESS";
    public static final String INTENT_WITH_PROGRESS_WICH_RETURN = "PROGRESS_RETURN";
    private ProgressBar progressBar;
    private Button button;
    private int progress;
    private static MyService myService;
    Intent intent;
    Intent emptyIntent;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        button = findViewById(R.id.button);
        button.setOnClickListener(onClickListener);

        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                myService = ((MyService.MyBinder) iBinder).getService();
                bound = true;
                Log.v("tag", "BOUND = " + bound);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                bound = false;
                Log.v("tag", "BOUND = " + bound);
            }
        };


        extras = getIntent().getExtras();

        if (extras != null) {

            intent = new Intent(this, MyService.class);
            startService(intent);

            int progressReturned = getIntent().getIntExtra(INTENT_WITH_PROGRESS_WICH_RETURN, 0);
            Log.v("tag", "progress returned ==== " + progressReturned);

            Intent emInt = new Intent(this, MyService.class);
            emInt.putExtra(PENDING_INTENT, pendingIntent);
            emInt.putExtra(INTENT_WITH_PROGRESS, progressReturned);
            // this.bindService(emptyIntent, sConn, 0);

            myService.onRebind(emInt);

        }

    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startLoad(progress);
        }
    };

    private void startLoad(int progress) {
        intent = new Intent(this, MyService.class);
        startService(intent);
        emptyIntent = new Intent(this, MyService.class);
        pendingIntent = createPendingResult(TASK_CODE, emptyIntent, FLAG_UPDATE_CURRENT);
        emptyIntent.putExtra(PENDING_INTENT, pendingIntent);
        emptyIntent.putExtra(INTENT_WITH_PROGRESS, progress);
        this.bindService(emptyIntent, sConn, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == STATUS_FINISH) {
            int result = data.getIntExtra(PENDING_RESULT, 0);
            setColoredProgress(result);
            if (result == 18) {
                stopService(intent);
                progressBar.setProgress(0);
            }
            Log.v("tag", "progress returned STATUS finish ");
        }

    }

    private void setColoredProgress(int result) {
        if (result <= 6) {
            progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            progressBar.setProgress(result);
        } else if (result <= 13) {
            progressBar.getProgressDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);
            progressBar.setProgress(result);
        } else {
            progressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
            progressBar.setProgress(result);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!bound) return;
        unbindService(sConn);
        bound = false;
        Log.v("tag", "UNBIND IN WORK");
    }

}
