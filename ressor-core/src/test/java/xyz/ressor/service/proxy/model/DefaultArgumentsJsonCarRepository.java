package xyz.ressor.service.proxy.model;

import com.fasterxml.jackson.databind.JsonNode;

public class DefaultArgumentsJsonCarRepository extends JsonCarRepository {

    /* Used by proxy class, which is generated dynamically as well as for reloads from the source */
    public DefaultArgumentsJsonCarRepository(JsonNode node) {
        super(node.path("model").asText(), node.path("manufacturer").asText());
    }

}
