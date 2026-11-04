package com.incidea.incideabackend.incident;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A reported incident. organization_id is duplicated on purpose (in
 * addition to the one reachable through site) because every query must
 * filter by it (docs/ROADMAP.md sprint 3).
 */
@Entity
@Table(name = "incidents")
public class Incident {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "incident_id")
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IncidentStatus status;

	@Column(nullable = false, length = 60)
	private String title;

	@Column(nullable = false)
	private String description;

	@Column(name = "site_id", nullable = false)
	private UUID siteId;

	/**
	 * Null while status is UNASSIGNED.
	 */
	@Column(name = "team_id")
	private UUID teamId;

	@Column(name = "created_by", nullable = false)
	private UUID createdBy;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	public UUID getId() {
		return id;
	}

	public IncidentStatus getStatus() {
		return status;
	}

	public void setStatus(IncidentStatus status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getSiteId() {
		return siteId;
	}

	public void setSiteId(UUID siteId) {
		this.siteId = siteId;
	}

	public UUID getTeamId() {
		return teamId;
	}

	public void setTeamId(UUID teamId) {
		this.teamId = teamId;
	}

	public UUID getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UUID createdBy) {
		this.createdBy = createdBy;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(UUID organizationId) {
		this.organizationId = organizationId;
	}
}
