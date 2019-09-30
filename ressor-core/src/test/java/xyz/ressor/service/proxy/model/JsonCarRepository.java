package xyz.ressor.service.proxy.model;

import com.fasterxml.jackson.databind.JsonNode;
import xyz.ressor.commons.annotations.ServiceFactory;

public class JsonCarRepository {
    private final String model;
    private final String manufacturer;

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public JsonCarRepository(String model, String manufacturer) {
        this.model = model;
        this.manufacturer = manufacturer;
    }

    @ServiceFactory
    public static JsonCarRepository create(JsonNode node) {
        return new JsonCarRepository(node.path("model").asText(), node.path("manufacturer").asText());
    }

}
