package oeapi.repository;

import java.util.List;
import oeapi.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface RoomRepository extends oeapiUnitaRepositoryBase<Room> {

    List<Room> findByBuilding_BuildingId(String buildingId);

    Page<Room> findByRoomType(String roomType, Pageable pageable);

}
