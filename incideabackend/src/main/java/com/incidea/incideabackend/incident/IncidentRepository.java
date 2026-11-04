package com.incidea.incideabackend.incident;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

	List<Incident> findByOrganizationId(UUID organizationId);

	List<Incident> findByOrganizationIdAndStatus(UUID organizationId, IncidentStatus status);

	List<Incident> findByOrganizationIdAndSiteId(UUID organizationId, UUID siteId);
}
