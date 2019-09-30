package xyz.ressor.integration.model.geo;

public class GeoInfo {
    private final String countryCode;
    private final double latitude;
    private final double longitude;

    public String getCountryCode() {
        return countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public GeoInfo(String countryCode, double latitude, double longitude) {
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
