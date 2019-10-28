package xyz.ressor.integration.model.geo;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoServiceImpl implements GeoService {
    private final Map<String, GeoInfo> geoInfoMap;

    public GeoServiceImpl(JsonNode data) {
        var geoInfoMap = new HashMap<String, GeoInfo>();
        data.forEach(n -> {
            if (n.has("ip")) {
                var ip = n.get("ip").asText();
                geoInfoMap.put(ip, new GeoInfo(n.path("country").asText(), n.path("lat").asDouble(),
                        n.path("lon").asDouble()));
            }
        });
        this.geoInfoMap = geoInfoMap;
    }

    public GeoServiceImpl(String[] data) {
        var geoInfoMap = new HashMap<String, GeoInfo>();
        for (String line : data) {
            var split = line.split(",");
            geoInfoMap.put(split[0], new GeoInfo(split[1], Double.parseDouble(split[2]), Double.parseDouble(split[3])));
        }
        this.geoInfoMap = geoInfoMap;
    }

    public GeoServiceImpl(List<GeoData> geoData) {
        var geoInfoMap = new HashMap<String, GeoInfo>();
        for (var gd : geoData) {
            geoInfoMap.put(gd.getIp(), new GeoInfo(gd.getCountry(), gd.getLatitude(), gd.getLongitude()));
        }
        this.geoInfoMap = geoInfoMap;
    }

    @Override
    public GeoInfo detect(String ip) {
        return geoInfoMap.get(ip);
    }
}
