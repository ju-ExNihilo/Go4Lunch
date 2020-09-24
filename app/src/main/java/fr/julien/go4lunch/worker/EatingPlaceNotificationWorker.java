package fr.julien.go4lunch.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.*;
import fr.julien.go4lunch.R;

import java.util.concurrent.TimeUnit;

public class EatingPlaceNotificationWorker extends Worker {

    public static final String KEY_EATING_PLACE = "KEY_EATING_PLACE";
    private Context context;

    public EatingPlaceNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        String eatingPlace = data.getString(KEY_EATING_PLACE);
        if (!eatingPlace.equals(Resources.getSystem().getString(R.string.none))){
            displayNotification(Resources.getSystem().getString(R.string.notification_title), Resources.getSystem().getString(R.string.notification_message , eatingPlace));
            eatingPlaceWorker(context);
        }

        return Result.success();
    }

    private void displayNotification(String task, String desc) {

        NotificationManager manager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("go4Lunch", "go4Lunch", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "go4Lunch")
                .setContentTitle(task)
                .setContentText(desc)
                .setSmallIcon(R.mipmap.ic_launcher);

        manager.notify(1, builder.build());

    }


    private void eatingPlaceWorker(Context context){
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ClearEatingPlaceWorker.class)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork("clear", ExistingWorkPolicy.REPLACE, workRequest) ;
    }
}
