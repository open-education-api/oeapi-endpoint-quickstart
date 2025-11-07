package oeapi.controller.requestparameters;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author itziar.urrutia
 */
public class ooapiRoomRequestParam extends oeapiRequestParam {

    private static final List<String> validSortAttributes = Arrays.asList("groupId", "name", "startDate");

    /**
     * @return the roomType
     */
    public String getRoomType() {
        return roomType;
    }

    /**
     * @param roomType the roomType to set
     */
    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    private String roomType;

    public Map.Entry<String, String> getFilter() {
        return this.getFilter(true);
    }

    public Map.Entry<String, String> getFilter(boolean hasPrimaryCodeFilter) {
        if (roomType != null) {
            return new AbstractMap.SimpleEntry<>("roomType", roomType);
        }
        if (hasPrimaryCodeFilter) {
            return super.getFilter();
        } else {
            return null;
        }
    }

    public Pageable toPageable() {
        return super.toPageable(validSortAttributes); // Pass the valid sort attributes for Person
    }

}
