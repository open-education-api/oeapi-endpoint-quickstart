package oeapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import static oeapi.oeapiUtils.ooapiObjectMapper;

/**
 *
 * @author itziar.urrutia
 */
public class oeapiExpandableMapper {

    private ObjectMapper objectMapper = ooapiObjectMapper();
    
    
    public <T> T expandFields(T dto, List<String> expand) {
        if (dto == null || expand == null || expand.isEmpty()) {
            return dto;
        }

        try {
            
            ObjectNode node = objectMapper.valueToTree(dto);  // Convert DTO to JSON

            // Dynamically expand requested fields
            for (String fieldToExpand : expand) {
                if (node.has(fieldToExpand)) {
                    // For nested objects, instantiate them

                    node.set(fieldToExpand, node.remove(fieldToExpand + "Object"));
                }
            }

            // Convert back to DTO if needed or return as JSON
            return (T) objectMapper.treeToValue(node, dto.getClass());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dto;
    }
}
