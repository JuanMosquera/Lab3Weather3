package co.edu.udea.compumovil.gr3.lab3weather3;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by johnny.castaneda on 27/09/16.
 */
public class HttpGetTask extends AsyncTask<String, Void, City> {

    private static final String TAG = "HttpGetTask";
    private ProgressDialog Dialog;// = new ProgressDialog(this);
    private String data;
    private Context context;
    private String message;

   public void setContext(Context c){
       context = c;
   }

    @Override
    protected void onPreExecute() {
        //Start Progress Dialog (Message)
        //Dialog = new ProgressDialog(context);
        //Dialog.setMessage("Cargando información del clima");
        //Dialog.show();
    }

    @Override
    protected City doInBackground(String... params) {
        HttpClient client = new HttpClient();

        //Obtener los parametros
        String cityName=params[0];
        String cityURL = params[1];

        //Obtener el lenguaje para la consulta
        String language = context.getResources().getString(R.string.idioma);

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
            Log.d("tag", currentWeather.getIconCode());

            currentWeather.setImageWeather(b);
            City city = new City();
            city.setName(cityName);
            city.setWeather(currentWeather);
            return city;
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        //Dialog.setMessage("Descargando Imagen");
    }

    @Override
    protected void onPostExecute(City city) {

        // Close progress dialog
        //Dialog.dismiss();
        Weather weather = city.getWeather();
        Intent i = new Intent();
        i.setAction(MainActivity.TAG_Action);
        i.setAction(MainActivity.TAG_Action);
        i.putExtra(MainActivity.TAG_Ciudad, city.getName());
        i.putExtra(MainActivity.TAG_Temperatura, context.getResources().getString(R.string.temperature)+""+String.valueOf(weather.getTemperature())+"°");
        i.putExtra(MainActivity.TAG_Humedad, context.getResources().getString(R.string.humidity)+""+String.valueOf(weather.getHumidity()+"%"));
        i.putExtra(MainActivity.TAG_Descripcion, context.getResources().getString(R.string.description)+""+weather.getDescription());

        DateFormat f = DateFormat.getDateInstance();
        String fecha = f.format(new Date((long) weather.getId()*1000));

        i.putExtra(MainActivity.TAG_UltimaVez, fecha);

        byte[] imgWeather = weather.getImageWeather();

        i.putExtra(MainActivity.TAG_Imagen, imgWeather);

        context.sendBroadcast(i);

        //Búsqueda exitosa: mostrar en pantalla el resultado


    }
}