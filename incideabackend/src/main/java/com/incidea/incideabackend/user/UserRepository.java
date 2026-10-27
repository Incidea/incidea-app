package com.incidea.incideabackend.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * Email is a mirror of Supabase Auth, so it is unique across the whole
	 * system and safe to look up without an organization scope.
	 */
	Optional<User> findByEmail(String email);

	List<User> findByOrganizationId(UUID organizationId);
}
