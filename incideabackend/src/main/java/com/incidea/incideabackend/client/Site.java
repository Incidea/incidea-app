package com.incidea.incideabackend.client;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A location of a client where incidents happen.
 */
@Entity
@Table(name = "sites")
public class Site {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "site_id")
	private UUID id;

	@Column(nullable = false, length = 60)
	private String name;

	@Column(nullable = false, length = 60)
	private String address;

	@Column(name = "client_id", nullable = false)
	private UUID clientId;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public UUID getClientId() {
		return clientId;
	}

	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}
}
