package com.incidea.incideabackend.user;

/**
 * User roles of the application. One role per user, fixed in code.
 *
 * <p>Stored as text in the database (see V1__init_schema.sql) with a CHECK
 * constraint that must list exactly these values.
 */
public enum Role {
	CLIENT,
	MANAGER,
	TECHNICIAN,
	ORGANIZATION_ADMIN,
	PLATFORM_ADMIN
}
