-- Skema Database Relasional Bengkelin Malang Raya (PostgreSQL / MySQL)

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(25) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('PENGGUNA', 'ADMIN')),
    region VARCHAR(50) NOT NULL CHECK (region IN ('Kota Malang', 'Kabupaten Malang', 'Kota Batu')),
    is_verified BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indeks untuk pencarian cepat
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_region ON users(region);

-- SEED AKUN ADMINISTRATOR BAWAAN (Tersedia sejak awal, tidak perlu mendaftar)
-- Kredensial: admin@bengkelin.id / Sandi: admin123
INSERT INTO users (id, name, email, password_hash, phone_number, role, region, is_verified)
VALUES (
    'admin-default-malang-01',
    'Administrator Bengkelin Malang',
    'admin@bengkelin.id',
    '$2a$10$wN1rYk3dKjV8UsampleHashAdmin123',
    '081299998888',
    'ADMIN',
    'Kota Malang',
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- Tabel Bengkel Rekanan
CREATE TABLE IF NOT EXISTS workshops (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address TEXT NOT NULL,
    region VARCHAR(50) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    rating NUMERIC(2,1) DEFAULT 4.8,
    is_verified BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
