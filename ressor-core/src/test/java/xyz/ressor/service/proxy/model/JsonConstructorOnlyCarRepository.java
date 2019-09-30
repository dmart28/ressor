package xyz.ressor.service.proxy.model;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonConstructorOnlyCarRepository extends JsonCarRepository {

    /* Used by proxy class, which is generated dynamically */
    public JsonConstructorOnlyCarRepository() {
        super(null, null);
    }

    /* Used for each reload from the source */
    public JsonConstructorOnlyCarRepository(JsonNode node) {
        super(node.path("model").asText(), node.path("manufacturer").asText());
    }

}
