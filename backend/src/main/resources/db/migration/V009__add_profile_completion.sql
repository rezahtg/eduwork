-- Migration: Add profile completion fields
-- Author: Eduwork Team
-- Date: 2025-12-23

-- Add student profile columns
ALTER TABLE users ADD COLUMN grade_level INTEGER;
ALTER TABLE users ADD COLUMN school_name VARCHAR(200);
ALTER TABLE users ADD COLUMN student_bio VARCHAR(500);

-- Add mentor profile columns
ALTER TABLE users ADD COLUMN mentor_bio TEXT;
ALTER TABLE users ADD COLUMN qualifications VARCHAR(500);
ALTER TABLE users ADD COLUMN education VARCHAR(500);
ALTER TABLE users ADD COLUMN certifications VARCHAR(500);
ALTER TABLE users ADD COLUMN portfolio_website VARCHAR(200);
ALTER TABLE users ADD COLUMN hourly_rate_idr BIGINT;

-- Add constraints
ALTER TABLE users ADD CONSTRAINT chk_grade_level 
    CHECK (grade_level IS NULL OR (grade_level >= 1 AND grade_level <= 12));

ALTER TABLE users ADD CONSTRAINT chk_hourly_rate 
    CHECK (hourly_rate_idr IS NULL OR hourly_rate_idr >= 0);

-- Add indexes for common queries
CREATE INDEX idx_users_grade_level ON users(grade_level) WHERE grade_level IS NOT NULL;
CREATE INDEX idx_users_hourly_rate ON users(hourly_rate_idr) WHERE hourly_rate_idr IS NOT NULL;

-- Comments
COMMENT ON COLUMN users.grade_level IS 'Student grade level (1-12)';
COMMENT ON COLUMN users.school_name IS 'Student school name';
COMMENT ON COLUMN users.student_bio IS 'Student bio/description';
COMMENT ON COLUMN users.mentor_bio IS 'Mentor professional biography';
COMMENT ON COLUMN users.qualifications IS 'Mentor qualifications summary';
COMMENT ON COLUMN users.education IS 'Mentor education (university, degree, year)';
COMMENT ON COLUMN users.certifications IS 'Mentor professional certifications';
COMMENT ON COLUMN users.portfolio_website IS 'Mentor portfolio/LinkedIn URL';
COMMENT ON COLUMN users.hourly_rate_idr IS 'Mentor hourly rate in Indonesian Rupiah';
