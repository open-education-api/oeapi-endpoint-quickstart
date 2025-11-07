package oeapi.repository;

import java.util.List;
import oeapi.model.oeapiIdentifierEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 *
 * @author itziar.urrutia
 */
@NoRepositoryBean
public interface oeapiUnitaRepositoryBase<T> extends JpaRepository<T, String> {

    List<T> findByPrimaryCode(oeapiIdentifierEntry code);

    Page<T> findByPrimaryCode(oeapiIdentifierEntry primaryCode, Pageable pageable);
}
