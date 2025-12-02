CREATE TABLE api_key (
    id BIGSERIAL PRIMARY KEY,
    key_hash VARCHAR(64) NOT NULL UNIQUE,
    label VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    last_used_at TIMESTAMP
);

CREATE INDEX idx_api_key_key_hash ON api_key(key_hash);
CREATE INDEX idx_api_key_active ON api_key(active);
