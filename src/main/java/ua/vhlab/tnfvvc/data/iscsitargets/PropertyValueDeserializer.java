/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ua.vhlab.tnfvvc.data.iscsitargets;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class PropertyValueDeserializer extends JsonDeserializer<PropertyValue> {

    @Override
    public PropertyValue deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        PropertyValue pv = new PropertyValue();

        if (node.isTextual()) {
            pv.parsed = node.asText();
            pv.rawvalue = node.asText();
            pv.value = node.asText();
            pv.source = "UNKNOWN";
        } else if (node.isObject()) {
            JsonNode parsedNode = node.get("parsed");
            if (parsedNode != null && parsedNode.isObject() && parsedNode.has("$date")) {
                pv.parsed = parsedNode.get("$date").asLong();
            } else {
                pv.parsed = parsedNode;
            }
            pv.rawvalue = node.path("rawvalue").asText(null);
            pv.value = node.path("value").asText(null);
            pv.source = node.path("source").asText(null);
            pv.source_info = node.get("source_info");
        }

        return pv;
    }
}
