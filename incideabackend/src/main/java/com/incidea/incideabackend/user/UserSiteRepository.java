package com.incidea.incideabackend.user;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSiteRepository extends JpaRepository<UserSite, UserSiteId> {

	/**
	 * The userId/siteId properties live inside the embedded id, so these
	 * methods cannot be derived queries: they are explicit JPQL.
	 */
	@Query("""
			select us from UserSite us
			where us.id.userId = :userId
			""")
	List<UserSite> findByUserId(@Param("userId") UUID userId);

	@Query("""
			select us from UserSite us
			where us.id.siteId = :siteId
			""")
	List<UserSite> findBySiteId(@Param("siteId") UUID siteId);

	@Query("""
			select count(us) > 0 from UserSite us
			where us.id.userId = :userId and us.id.siteId = :siteId
			""")
	boolean existsByUserIdAndSiteId(@Param("userId") UUID userId, @Param("siteId") UUID siteId);
}
