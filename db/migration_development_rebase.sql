-- Migration: add columns required for frontend integration (development-rebase branch)

-- Projects: add started_at and completed_at timestamps
ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- Applications: add message field and updated_at timestamp
ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS message TEXT,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
