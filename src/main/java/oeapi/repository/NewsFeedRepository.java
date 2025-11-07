package oeapi.repository;

import oeapi.model.NewsFeed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

/**
 *
 * @author itziar.urrutia
 */
@Repository
public interface NewsFeedRepository extends oeapiUnitaRepositoryBase<NewsFeed> {

    Page<NewsFeed> findByNewsFeedType(String newsFeedType, Pageable pageable);

}
