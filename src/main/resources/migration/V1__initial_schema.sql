CREATE TABLE IF NOT EXISTS outbox (
                                      pk BIGSERIAL PRIMARY KEY,
                                      uuid UUID,
                                      version BIGINT NOT NULL DEFAULT 0,
                                      created_at TIMESTAMP,
                                      created_by TEXT,
                                      updated_at TIMESTAMP,
                                      updated_by TEXT,
                                      exchange_key TEXT NOT NULL,
                                      routing_key VARCHAR(256) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    published BOOLEAN NOT NULL,
    attempts INT NOT NULL,
    next_attempt_at TIMESTAMP,
    last_error VARCHAR(4000),
    payload_json TEXT NOT NULL,
    headers_json TEXT
    );