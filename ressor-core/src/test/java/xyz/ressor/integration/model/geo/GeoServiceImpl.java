package xyz.ressor.integration.model.geo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoServiceImpl implements GeoService {
    private final Map<String, GeoInfo> geoInfoMap;

    public GeoServiceImpl(JsonNode data) {
        Map<String, GeoInfo> geoInfoMap = new HashMap<>();
        if (!data.isArray()) {
            data = data.path("geoData");
        }
        data.forEach(n -> {
            if (n.has("ip")) {
                String ip = n.get("ip").asText();
                geoInfoMap.put(ip, new GeoInfo(n.path("country").asText(), n.path("lat").asDouble(),
                        n.path("lon").asDouble()));
            }
        });
        this.geoInfoMap = geoInfoMap;
    }

    public GeoServiceImpl(String[] data) {
        Map<String, GeoInfo> geoInfoMap = new HashMap<>();
        for (String line : data) {
            String[] split = line.split(",");
            geoInfoMap.put(split[0], new GeoInfo(split[1], Double.parseDouble(split[2]), Double.parseDouble(split[3])));
        }
        this.geoInfoMap = geoInfoMap;
    }

    public GeoServiceImpl(List<GeoData> geoData) {
        Map<String, GeoInfo> geoInfoMap = new HashMap<>();
        for (GeoData gd : geoData) {
            geoInfoMap.put(gd.getIp(), new GeoInfo(gd.getCountry(), gd.getLatitude(), gd.getLongitude()));
        }
        this.geoInfoMap = geoInfoMap;
    }

    @Override
    public GeoInfo detect(String ip) {
        return geoInfoMap.get(ip);
    }
}
