CREATE TABLE
    organizations (
        organization_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        name VARCHAR(40) NOT NULL
    );

CREATE TABLE
    clients (
        client_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        organization_id UUID REFERENCES organizations (organization_id) NOT NULL
    );

CREATE TABLE
    teams (
        team_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        organization_id UUID REFERENCES organizations (organization_id) NOT NULL,
        name VARCHAR(40) NOT NULL
    );

CREATE TABLE
    sites (
        site_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        name VARCHAR(60) NOT NULL,
        address VARCHAR(60) NOT NULL,
        client_id UUID REFERENCES clients (client_id) NOT NULL
    );

CREATE TABLE
    users (
        user_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        name VARCHAR(40) NOT NULL,
        email TEXT NOT NULL,
        active BOOLEAN NOT NULL DEFAULT true,
        role TEXT NOT NULL CHECK (
            role IN (
                'CLIENT',
                'MANAGER',
                'TECHNICIAN',
                'ORGANIZATION_ADMIN',
                'PLATFORM_ADMIN'
            )
        ),
        organization_id UUID REFERENCES organizations (organization_id)
    );

CREATE TABLE
    user_sites (
        user_id UUID REFERENCES users (user_id) NOT NULL,
        site_id UUID REFERENCES sites (site_id) NOT NULL,
        PRIMARY KEY (user_id, site_id)
    );

CREATE TABLE
    incidents (
        incident_id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        status TEXT NOT NULL CHECK (
            status IN (
                'UNASSIGNED',
                'ASSIGNED',
                'IN_PROGRESS',
                'ON_HOLD',
                'FINALIZED',
                'CLOSED',
                'CANCELLED'
            )
        ),
        title VARCHAR(60) NOT NULL,
        description TEXT NOT NULL,
        site_id UUID REFERENCES sites (site_id) NOT NULL,
        team_id UUID REFERENCES teams (team_id),
        created_by UUID REFERENCES users (user_id) NOT NULL,
        organization_id UUID REFERENCES organizations (organization_id) NOT NULL
    );

CREATE INDEX incidents_organization_id_idx ON incidents (organization_id);

CREATE INDEX incidents_site_id_idx ON incidents (site_id);

CREATE INDEX incidents_team_idx ON incidents (team_id);

CREATE INDEX clients_organization_id_idx ON clients (organization_id);

CREATE INDEX teams_organization_id_idx ON teams (organization_id);

CREATE INDEX sites_client_id_idx ON sites (client_id);

CREATE INDEX users_organization_id_idx ON users (organization_id);