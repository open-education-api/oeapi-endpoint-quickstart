package oeapi.controller;

import oeapi.controller.requestparameters.ooapiRoomRequestParam;
import oeapi.controller.requestparameters.oeapiRequestParam;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import oeapi.model.Building;
import oeapi.model.Room;
import oeapi.service.BuildingService;
import oeapi.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/buildings")
public class BuildingController extends oeapiController<Building> {

    @Autowired
    private BuildingService service;

    @Autowired
    private RoomService roomService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(Arrays.asList("buildingId", "name")), service);

    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Building program) {
        return super.create(program, service);

    }

    @GetMapping(value = "/{id}/rooms", produces = "application/json")
    public ResponseEntity<?> getRooms(@PathVariable String id, ooapiRoomRequestParam requestParam) {

        Map.Entry<String, String> filter = requestParam.getFilter(false);
        Optional<Building> existing = service.getById(id);
        if (existing.isPresent()) {
            return ResponseEntity.notFound().build();

        }
        List<Room> rooms = roomService.getByBuildingId(id);
        return super.getResponse(requestParam.toPageable(), rooms);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Building> items) {

        return super.createByJSON(items, service);

    }
}
