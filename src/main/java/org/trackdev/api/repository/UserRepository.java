package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepositoryUUID<User> {

    Optional<User> findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    boolean existsByEmail(@Param("email") String email);

    boolean existsByUsername(@Param("username") String username);

    List<User> findByRoles_UserType(@Param("userType") UserType userType);

    // Query methods for user deletion validation
    
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.owner.id = :userId")
    long countSubjectsOwnedByUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.reporter.id = :userId")
    long countTasksReportedByUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :userId")
    long countTasksAssignedToUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :userId")
    long countCommentsAuthoredByUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(i) FROM CourseInvite i WHERE i.invitedBy.id = :userId")
    long countInvitesSentByUser(@Param("userId") String userId);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.owner.id = :userId")
    long countCoursesOwnedByUser(@Param("userId") String userId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.workspace.id = :workspaceId AND r.userType IN :userTypes")
    List<User> findByWorkspaceIdAndRolesIn(@Param("workspaceId") Long workspaceId, @Param("userTypes") List<UserType> userTypes);

}
