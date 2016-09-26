package co.edu.udea.compumovil.gr3.lab3weather3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private String message;
    private City currentCity;
    private AutoCompleteTextView tvSearch;
    private TextView tvCity, tvTemperature, tvHumidity, tvDescription,ultimaAct;
    private ImageView ivWeather;
    private boolean cityAvailable;
    private List<String> cities;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                new HttpGetTask().execute(city, cityFormated);
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

    private class HttpGetTask extends AsyncTask<String, Void, City> {

        private static final String TAG = "HttpGetTask";
        private ProgressDialog Dialog = new ProgressDialog(MainActivity.this);
        private String data;

        @Override
        protected void onPreExecute() {
            //Start Progress Dialog (Message)
            Dialog.setMessage("Cargando información del clima");
            Dialog.show();
        }

        @Override
        protected City doInBackground(String... params) {
            HttpClient client = new HttpClient();

            //Obtener los parametros
            String cityName=params[0];
            String cityURL = params[1];

            //Obtener el lenguaje para la consulta
            String language = getResources().getString(R.string.idioma);

            //Traer los datos del clima
            data = client.getJSONData(cityURL,language);

            //Convertir JSON a modelo de objetos Java
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Weather.class,new WeatherDeserializer());
            Gson gson = gsonBuilder.create();
            Weather currentWeather=null;
            try {
                currentWeather = gson.fromJson(data, Weather.class);
            }catch(com.google.gson.JsonSyntaxException ex){
                message = "Verifique su conexión a Internet";
            }
            if(currentWeather==null){
                message = "Ciudad invalida";
                return null;

            }else {
                publishProgress();

                //Descargar la imagen
                byte[] b = client.downloadImage(currentWeather.getIconCode());
                currentWeather.setImageWeather(b);
                City city = new City();
                city.setName(cityName);
                city.setWeather(currentWeather);
                return city;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Dialog.setMessage("Descargando Imagen");
        }

        @Override
        protected void onPostExecute(City city) {

            // Close progress dialog
            Dialog.dismiss();
            //Búsqueda exitosa: mostrar en pantalla el resultado
            if(city!=null) {
                currentCity = city;
                Weather weather = city.getWeather();
                tvCity.setText(city.getName());
                tvTemperature.setText(getResources().getString(R.string.temperature)+""+String.valueOf(weather.getTemperature())+"°");
                tvHumidity.setText(getResources().getString(R.string.humidity)+""+String.valueOf(weather.getHumidity()+"%"));
                tvDescription.setText(getResources().getString(R.string.description)+""+weather.getDescription());
                ultimaAct.setText(String.valueOf(weather.getId()));
                byte[] imgWeather = weather.getImageWeather();
                Bitmap bitmapWeather = BitmapFactory.decodeByteArray(imgWeather, 0, imgWeather.length);
                ivWeather.setImageBitmap(bitmapWeather);
                cityAvailable=true;
            }else{
                //Busqueda no exitosa: Mostrar una notificacion toast con el mensaje respectivo
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
                cityAvailable = false;
                clearScreen();
            }

        }
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
                ultimaAct.setText(String.valueOf(weather.getId()));
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
}
