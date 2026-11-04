ALTER TABLE organizations
ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE users ADD CONSTRAINT users_organization_required CHECK (
    (
        role <> 'PLATFORM_ADMIN'
        AND organization_id IS NOT NULL
    )
    OR (role = 'PLATFORM_ADMIN')
);