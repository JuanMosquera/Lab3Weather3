package co.edu.udea.compumovil.gr3.lab3weather3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServiceLab3 extends Service {
    public ServiceLab3() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            String city = intent.getStringExtra(MainActivity.TAG_Ciudad);
            String cityFormated = intent.getStringExtra(MainActivity.TAG_CityFormat);
            HttpGetTask httpGetTask = new HttpGetTask();
            httpGetTask.execute(city, cityFormated);
        }

        return START_REDELIVER_INTENT;
    }
}
