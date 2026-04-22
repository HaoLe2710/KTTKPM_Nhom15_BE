INSERT INTO users (
    id,
    email,
    password,
    full_name,
    role,
    is_active
) VALUES (
    'seed-admin-0001',
    'admin.local@kttkpm.dev',
    '$2a$10$8ONRaEf7/V3YnLm9ify.BOoRXTJFNp9L0Q8gx24HZ5zUv1748xvGK',
    'System Admin',
    'ADMIN',
    TRUE
)
ON CONFLICT (email) DO UPDATE
SET
    password = EXCLUDED.password,
    role = 'ADMIN',
    is_active = TRUE,
    full_name = COALESCE(users.full_name, EXCLUDED.full_name),
    updated_at = CURRENT_TIMESTAMP;
