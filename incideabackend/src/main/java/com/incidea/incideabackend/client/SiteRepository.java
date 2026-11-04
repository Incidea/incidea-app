package com.incidea.incideabackend.client;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteRepository extends JpaRepository<Site, UUID> {

	/**
	 * The sites table has no organization_id: the tenant is inferred through
	 * the owning client, so this is a join, not a derived query.
	 */
	@Query("""
			select s from Site s
			join Client c on c.id = s.clientId
			where c.organizationId = :organizationId
			""")
	List<Site> findByOrganizationId(@Param("organizationId") UUID organizationId);

	/**
	 * The organization filter is mandatory even when narrowing by client,
	 * so a compromised client id cannot leak sites from another tenant.
	 */
	@Query("""
			select s from Site s
			join Client c on c.id = s.clientId
			where s.clientId = :clientId and c.organizationId = :organizationId
			""")
	List<Site> findByClientIdAndOrganizationId(@Param("clientId") UUID clientId,
			@Param("organizationId") UUID organizationId);
}
