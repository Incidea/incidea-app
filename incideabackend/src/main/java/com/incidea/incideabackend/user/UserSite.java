package com.incidea.incideabackend.user;

import java.util.UUID;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Which sites a CLIENT user can see and report for. Clients have no site of
 * their own: access is granted through rows in this join table.
 */
@Entity
@Table(name = "user_sites")
public class UserSite {

    @EmbeddedId
    private UserSiteId id;

    UserSite() {
    }

    UserSite(UUID userId, UUID siteId) {
        this.id = new UserSiteId(userId, siteId);
    }

    UUID getUserId() {
        return id.getUserId();
    }

    UUID getSiteId() {
        return id.getSiteId();
    }

}
