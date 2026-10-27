package com.incidea.incideabackend.user;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A login user. The id is NOT generated here: it is the identifier of the
 * Supabase Auth user this row mirrors.
 */
@Entity
@Table(name = "users")
public class User {

	/**
	 * Supabase Auth assigns this id, not the database default.
	 */
	@Id
	@Column(name = "user_id")
	private UUID id;

	@Column(nullable = false, length = 40)
	private String name;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private boolean active;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;


	@Column(name = "organization_id")
	private UUID organizationId;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}
}
