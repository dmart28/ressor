package xyz.ressor.integration.model.geo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoData {
    private final String ip;
    private final String country;
    private final double latitude;
    private final double longitude;

    public String getIp() {
        return ip;
    }

    public String getCountry() {
        return country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public GeoData(@JsonProperty("ip") String ip,
                   @JsonProperty("country") String country,
                   @JsonProperty("lat") double latitude,
                   @JsonProperty("lon") double longitude) {
        this.ip = ip;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
