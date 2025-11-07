package oeapi.repository;

import java.util.Optional;
import oeapi.model.ModeOfDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface ModeOfDelivery repository.
 *
 * @author Carlos Alonso - losalo@unavarra.es
 */
public interface ModeOfDeliveryRepository extends JpaRepository<ModeOfDelivery, Long> {

    public Optional<ModeOfDelivery> findModeOfDeliveryByName(String mode);
}
