package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Sprint;

import java.util.Collection;

@Component
public interface SprintRepository extends BaseRepositoryLong<Sprint> {

    @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE endDate < sysdate() AND status != 2")
    Collection<Sprint> sprintsToClose();

   @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE startDate > sysdate() AND status = 0")
    Collection<Sprint> sprintsToDraft();

   @Query(nativeQuery = true, value = "SELECT * FROM sprints WHERE startDate < sysdate() AND endDate > sysdate() AND (status = 0 OR status = 2)")
    Collection<Sprint> sprintsToActive();

}
