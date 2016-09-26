package co.edu.udea.compumovil.gr3.lab3weather3;

/**
 * Created by user on 26/04/2016.
 */

public class Weather {

    private double temperature;
    private double id;
    private int humidity;
    private String description;
    private String iconCode;
    private byte [] imageWeather;

    //Constructors
    public Weather(){}


    //Getters and setters
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public byte[] getImageWeather() {
        return imageWeather;
    }

    public void setImageWeather(byte[] imageWeather) {
        this.imageWeather = imageWeather;
    }

    public double getId() {return id;}

    public void setId(double id) { this.id = id;}
}
