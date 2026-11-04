package com.incidea.incideabackend.user;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Composite id of {@link UserSite}: (user_id, site_id), as defined by the
 * primary key of the user_sites table.
 */
@Embeddable
class UserSiteId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "site_id", nullable = false)
	private UUID siteId;

	UserSiteId() {
	}

	UserSiteId(UUID userId, UUID siteId) {
		this.userId = userId;
		this.siteId = siteId;
	}

	UUID getUserId() {
		return userId;
	}

	UUID getSiteId() {
		return siteId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof UserSiteId that)) {
			return false;
		}
		return Objects.equals(userId, that.userId) && Objects.equals(siteId, that.siteId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, siteId);
	}
}
