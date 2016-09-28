package co.edu.udea.compumovil.gr3.lab3weather3;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    public static String TAG_Ciudad = "Ciudad";
    public static String TAG_CityFormat = "CiudadFormat";
    public static String TAG_Temperatura = "Temperatura";
    public static String TAG_Descripcion = "Descripcion";
    public static String TAG_Humedad = "Humedad";
    public static String TAG_UltimaVez = "UltimaVez";
    public static String TAG_Imagen = "Imagen";
    public static String TAG_Mensaje = "Mensaje";
    public static String TAG_Action = "co.edu.udea.compumovil.gr3.lab3weather3.ACTION";


    private String message;
    private IntentFilter filter;
    MyBroadcastReceiver myBroadcast;
    private City currentCity;
    private AutoCompleteTextView tvSearch;
    private TextView tvCity, tvTemperature, tvHumidity, tvDescription,ultimaAct;
    private ImageView ivWeather;
    private boolean cityAvailable;
    private List<String> cities;


    @Override
    protected void onResume() {
        super.onResume();
        myBroadcast = new MyBroadcastReceiver();
        registerReceiver(myBroadcast, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myBroadcast);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filter = new IntentFilter();
        filter.addAction(TAG_Action);


        //Obtener el estado de la variable guardada
        if(savedInstanceState!=null){
            cityAvailable = savedInstanceState.getBoolean("State");
        }

        //Capturar las vistas
        tvSearch = (AutoCompleteTextView) findViewById(R.id.tv_search);
        tvCity = (TextView) findViewById(R.id.tv_city);
        tvTemperature = (TextView) findViewById(R.id.tv_temperature);
        tvHumidity = (TextView) findViewById(R.id.tv_humidity);
        tvDescription = (TextView) findViewById(R.id.tv_description);
        ivWeather = (ImageView) findViewById(R.id.iv_weather_image);
        ultimaAct = (TextView) findViewById(R.id.ultimaVisualizacion);



        //Asignamos un adapter con la lista de las ciudades capitales al textview para la funcion de autocompletar
        cities = Arrays.asList(getResources().getStringArray(R.array.capital_cities));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,cities);
        tvSearch.setAdapter(adapter);
        tvSearch.setThreshold(2);

    }

    public void onClick(View v){
        String city = tvSearch.getText().toString();
        if(city.equals("")){
            //Validar si el texto entrado es vacio
            Toast.makeText(MainActivity.this,"Por favor ingrese una ciudad",Toast.LENGTH_LONG).show();
        }else {
            //Formatear el string de la ciudad en caso de que tenga espacios
            String cityFormated = formatCity(city);
            if (checkConnection()) {
                Intent i = new Intent(this, ServiceLab3.class);
                i.putExtra(TAG_Ciudad, city);
                i.putExtra(TAG_CityFormat, cityFormated);
                stopService(i);
                startService(i);
            } else {
                Toast.makeText(this, "Verifique su conexión a internet", Toast.LENGTH_LONG).show();
            }
            tvSearch.setText("");
        }

        //Esconder el teclado virtual cuando se de click en el icono para buscar
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private String formatCity(String city) {
        //Formatear el string de la ciudad para cuando contenga mas de una palabra
        StringTokenizer tokenizer = new StringTokenizer(city);
        String responseCity=tokenizer.nextToken();
        while(tokenizer.hasMoreTokens()){
            responseCity+="%20"+tokenizer.nextToken();
        }
        return responseCity;
    }


    /*Chequear la conexión a Internet*/
    private boolean checkConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    /*Guardar el estado del clima para cuando se gire la pantalla*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(currentCity!=null) {
            outState.putString("City", new Gson().toJson(currentCity));
            outState.putBoolean("State",cityAvailable);
        }
    }

    /*Restaurar el estado del clima*/
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String jsonMyObject;
        if (savedInstanceState != null) {
            boolean isAvailable = savedInstanceState.getBoolean("State");
            if(isAvailable) {
                jsonMyObject = savedInstanceState.getString("City");
                currentCity = new Gson().fromJson(jsonMyObject, City.class);
                Weather weather = currentCity.getWeather();
                tvCity.setText(currentCity.getName());
                tvTemperature.setText(getResources().getString(R.string.temperature) + "" + String.valueOf(weather.getTemperature()) + "°C");
                tvHumidity.setText(getResources().getString(R.string.humidity) + "" + String.valueOf(weather.getHumidity())+"%");
                tvDescription.setText(getResources().getString(R.string.description) + "" + weather.getDescription());


                ultimaAct.setText(String.valueOf(weather.getId()*1000));
                byte[] imgWeather = weather.getImageWeather();
                Bitmap bitmapWeather = BitmapFactory.decodeByteArray(imgWeather, 0, imgWeather.length);
                ivWeather.setImageBitmap(bitmapWeather);
            }
        }
    }

    /*Limpiar la pantalla*/
    private void clearScreen(){
        tvCity.setText("");
        tvTemperature.setText("");
        tvHumidity.setText("");
        tvDescription.setText("");
        ivWeather.setImageBitmap(null);
        ultimaAct.setText("");
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        public MyBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent != null) {

                tvCity.setText(intent.getStringExtra(TAG_Ciudad));
                tvTemperature.setText(intent.getStringExtra(TAG_Temperatura));

                tvHumidity.setText(intent.getStringExtra(TAG_Humedad));

                tvDescription.setText(intent.getStringExtra(TAG_Descripcion));

                ultimaAct.setText(intent.getStringExtra(TAG_UltimaVez));

                Bitmap bitmapWeather = BitmapFactory.decodeByteArray(intent.getByteArrayExtra(TAG_Imagen), 0, intent.getByteArrayExtra(TAG_Imagen).length);
                ivWeather.setImageBitmap(bitmapWeather);


                cityAvailable=true;
            }else{
                //Busqueda no exitosa: Mostrar una notificacion toast con el mensaje respectivo
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                cityAvailable = false;
                clearScreen();
            }
            Log.d("estado","actualizando");


        }
    }
}
