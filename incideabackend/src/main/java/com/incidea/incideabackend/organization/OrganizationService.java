package com.incidea.incideabackend.organization;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public UUID createOrganization(String name) {
        Organization organization = new Organization();
        organization.setName(name);
        return saveOrganization(organization).getId();
    }

    // Transactional operations
    @Transactional
    public Organization saveOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }
}
