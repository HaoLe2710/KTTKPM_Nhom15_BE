CREATE TABLE otps (
                      id UUID PRIMARY KEY,
                      user_id VARCHAR(255),
                      email VARCHAR(255) NOT NULL,
                      otp_code VARCHAR(6) NOT NULL,
                      expiry_time TIMESTAMP NOT NULL,
                      is_used BOOLEAN DEFAULT FALSE,
                      type VARCHAR(50),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_otps_user_email ON otps(user_id, email);