CREATE TABLE otps (
                      id UUID PRIMARY KEY,
                      email VARCHAR(255) NOT NULL,
                      otp_code VARCHAR(6) NOT NULL,
                      expiry_time TIMESTAMP NOT NULL,
                      is_used BOOLEAN DEFAULT FALSE,
                      type VARCHAR(50),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_otps_email_type_created_at ON otps(email, type, created_at DESC);
