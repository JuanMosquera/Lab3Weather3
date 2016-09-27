package co.edu.udea.compumovil.gr3.lab3weather3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by user on 26/04/2016.
 */
public class HttpClient {

    private final String TAG = "HttpClient";
    private static final String API_KEY = "d85777356583ffdb73584d0c2090e3bf";
    private static final String URL_BASE ="http://api.openweathermap.org";
    private static final String [] REQUESTS = {"/data/2.5/weather","/img/w/"};

    //metodo getJsonData
    public String getJSONData(String city,String language) {

        String data = null;
        String URL = getDataURL(city,language);
        Log.d(TAG,URL);
        HttpURLConnection httpUrlConnection = null;

        try {
            httpUrlConnection = (HttpURLConnection) new URL(URL).openConnection();
            InputStream in = new BufferedInputStream(httpUrlConnection.getInputStream());
            data = readStream(in);
        } catch (MalformedURLException exception) {
            Log.e(TAG, "MalformedURLException");
        } catch (IOException exception) {
            Log.e(TAG, "IOException");
        } finally {
            if (null != httpUrlConnection) {
                httpUrlConnection.disconnect();
            }
        }
        return data;

    }




    /*Obtener la URL para la consulta de los datos del clima*/
    private String getDataURL(String city,String language) {
        return URL_BASE+REQUESTS[0]+"?q="+city+"&units=metric&lang="+language+"&APPID=" + API_KEY;
    }





    /*Obtener la URL para la decarga de la imagen*/
    private String getImageURL(String iconCode){
        return URL_BASE + REQUESTS[1]+iconCode+".png";
    }






    /*Leer los datos obtenidos del clima*/
    @NonNull
    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder data = new StringBuilder("");
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data.toString();
    }






    public byte[] downloadImage(String code){
        HttpURLConnection con = null ;
        String URL = getImageURL(code);
        InputStream is = null;
        try {
            con = (HttpURLConnection) ( new URL(URL)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();

            // Let's read the response
            is = con.getInputStream();
            //is = con.getOutputStream();
            //con.str
            byte[] buffer = new byte[1024];
            Bitmap b = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try {
                assert is != null;
                is.close(); } catch(Throwable ignored) {}
            try {
                assert con != null;
                con.disconnect(); } catch(Throwable ignored) {}
        }
        return null;
    }
}
