package co.edu.udea.compumovil.gr3.lab3weather3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class ServiceLab3 extends Service {
    String city = "";
    String cityFormated = "";
    Timer timer;

    public ServiceLab3() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null; }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        timer = new Timer();
        TimerTask tiempo = new TimerTask() {
            @Override
            public void run() {

                if (intent != null && isNetDisponible() && isOnlineNet()) {
                    city = intent.getStringExtra(MainActivity.TAG_Ciudad);
                    cityFormated = intent.getStringExtra(MainActivity.TAG_CityFormat);
                    HttpGetTask httpGetTask = new HttpGetTask();
                    httpGetTask.setContext(getApplicationContext());
                    httpGetTask.execute(city, cityFormated);

                }
            }
        };
        Log.d("algo", "actualizando");
        timer.scheduleAtFixedRate(tiempo, 0, 5000);
        return START_REDELIVER_INTENT;
    }

    private boolean isNetDisponible() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo actNetInfo = connectivityManager.getActiveNetworkInfo();

        return (actNetInfo != null && actNetInfo.isConnected());
    }

    public Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

}
