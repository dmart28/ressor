package xyz.ressor.service.proxy.model;

import com.fasterxml.jackson.databind.JsonNode;
import xyz.ressor.commons.annotations.ServiceFactory;

public class JsonNestedCarRepository extends JsonCarRepository implements CarInterface {
    private final double clearance;

    public double getClearance() {
        return clearance;
    }

    public JsonNestedCarRepository(String model, String manufacturer, double clearance) {
        super(model, manufacturer);
        this.clearance = clearance;
    }

    @ServiceFactory
    public static JsonNestedCarRepository create(JsonNode node) {
        return new JsonNestedCarRepository(node.path("model").asText(),
                node.path("manufacturer").asText(), node.path("clearance").asDouble());
    }

    @Override
    public double computeClearance(double ratio) {
        return clearance * ratio;
    }
}
