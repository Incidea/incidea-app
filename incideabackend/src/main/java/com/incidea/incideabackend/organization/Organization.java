package com.incidea.incideabackend.organization;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The maintenance company that uses Incidea (the tenant).
 */
@Entity
@Table(name = "organizations")
public class Organization {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "organization_id")
	private UUID id;

	@Column(nullable = false, length = 40)
	private String name;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
