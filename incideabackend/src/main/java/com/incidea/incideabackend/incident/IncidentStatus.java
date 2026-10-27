package com.incidea.incideabackend.incident;

/**
 * Lifecycle states of an incident (docs/functionality.md).
 *
 * <p>Status changes must only happen through the state machine, never a
 * bare UPDATE of the status column. CLOSED and CANCELLED are terminal.
 *
 * <p>Stored as text in the database with a CHECK constraint that must
 * list exactly these values.
 */
public enum IncidentStatus {
	UNASSIGNED,
	ASSIGNED,
	IN_PROGRESS,
	ON_HOLD,
	FINALIZED,
	CLOSED,
	CANCELLED
}
