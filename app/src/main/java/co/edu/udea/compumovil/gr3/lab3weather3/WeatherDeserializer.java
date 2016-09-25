package co.edu.udea.compumovil.gr3.lab3weather3;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by user on 27/04/2016.
 */
public class WeatherDeserializer implements JsonDeserializer<Weather> {

    @Override
    public Weather deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        Weather weather=null;
        int responseCode = jsonObject.get("cod").getAsInt();
        if(responseCode==200) {
            JsonArray jsonDescriptionArray = jsonObject.getAsJsonArray("weather");
            String description = jsonDescriptionArray.get(0).getAsJsonObject().get("description").getAsString();
            String iconCode = jsonDescriptionArray.get(0).getAsJsonObject().get("icon").getAsString();
            JsonObject jsonMain = jsonObject.get("main").getAsJsonObject();
            double temperature = jsonMain.get("temp").getAsDouble();
            int humidity = jsonMain.get("humidity").getAsInt();
            weather = new Weather();
            weather.setDescription(description);
            weather.setHumidity(humidity);
            weather.setTemperature(temperature);
            weather.setIconCode(iconCode);
        }
        return weather;
    }
}
