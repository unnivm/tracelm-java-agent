CREATE TABLE traces (
    trace_id VARCHAR(128) PRIMARY KEY,
    model VARCHAR(120) NOT NULL,
    prompt TEXT,
    response TEXT,
    latency BIGINT NOT NULL,
    timestamp BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    token_length BIGINT NOT NULL,
    prompt_tokens INTEGER NOT NULL,
    response_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL,
    cost DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_traces_timestamp ON traces (timestamp DESC);
CREATE INDEX idx_traces_model ON traces (model);
CREATE INDEX idx_traces_status ON traces (status);
