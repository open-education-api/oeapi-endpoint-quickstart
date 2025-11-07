package oeapi.controller;

import oeapi.controller.requestparameters.ooapiRoomRequestParam;
import java.util.List;
import java.util.Map;
import oeapi.model.Room;
import oeapi.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/rooms")
public class RoomController extends oeapiController<Room> {

    /*
    @Autowired
    private ooapiObjectsValidator validator;
     */
    @Autowired
    private RoomService service;

    @GetMapping
    public ResponseEntity<?> getAll(@ModelAttribute ooapiRoomRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();

        if (filter == null) {
            return super.getAll(requestParam.toPageable(), service);
            //return super.getAllByPrimaryCode(requestParam.getPrimaryCode(), requestParam, service)
        }
        if (filter.getKey() == "primaryCode") {
            return this.getAllByPrimaryCode(filter.getValue(), requestParam.toPageable(), service);
        } else {
            return this.getAllByFieldValue(filter.getKey(), filter.getValue(), requestParam.toPageable(), service);
        }
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> get(@PathVariable String id) {
        return super.get(id, service);

    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Room room) {
        return super.create(room, service);

    }

    @PostMapping(value = {"/loadbyjson"}, produces = "application/json")
    public ResponseEntity<String> createByJSON(@RequestBody List<Room> items) {

        /*
        /* Check if this entry point allows updates using REST or is it read-only
        if (!allowRestToModify) {
            throw new ooapiException(HttpStatus.NOT_FOUND, "This OOAPI entry point does not allow updates using REST");
        }

         */
        return super.createByJSON(items, service);

    }

}
