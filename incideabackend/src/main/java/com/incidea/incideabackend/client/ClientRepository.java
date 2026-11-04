package com.incidea.incideabackend.client;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {

	/**
	 * Only organization-scoped reads: every business query must filter by
	 * the caller's organization.
	 */
	List<Client> findByOrganizationId(UUID organizationId);
}
