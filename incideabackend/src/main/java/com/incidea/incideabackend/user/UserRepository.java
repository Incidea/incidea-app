package com.incidea.incideabackend.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * Email is a mirror of Supabase Auth, so it is unique across the whole
	 * system and safe to look up without an organization scope.
	 */
	Optional<User> findByEmail(String email);

	List<User> findByOrganizationId(UUID organizationId);

	/**
	 * Single query with a {@code LEFT JOIN} to {@code Organization} to fetch the
	 * user and organization status in one round trip. If the user has no
	 * organization, {@code organizationActive} is {@code null}.
	 */
	@Query("""
		select new com.incidea.incideabackend.user.AccountStatus(u.active, u.role, o.active)
		from User u
		left join Organization o on o.id = u.organizationId
		where u.id = :id
		""")
	Optional<AccountStatus> findAccountStatusById(UUID id);
}
