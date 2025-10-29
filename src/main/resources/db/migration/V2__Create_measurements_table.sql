CREATE TABLE measurements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    weight DECIMAL(5, 2) NOT NULL CHECK (weight > 0),
    height DECIMAL(5, 2) NOT NULL CHECK (height > 0),
    chest_circumference DECIMAL(5, 2) CHECK (chest_circumference >= 0),
    arm_circumference DECIMAL(5, 2) CHECK (arm_circumference >= 0),
    leg_circumference DECIMAL(5, 2) CHECK (leg_circumference >= 0),
    waist_circumference DECIMAL(5, 2) CHECK (waist_circumference >= 0),
    measurement_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_measurements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_measurements_user_id ON measurements(user_id);
CREATE INDEX idx_measurements_date ON measurements(measurement_date);
