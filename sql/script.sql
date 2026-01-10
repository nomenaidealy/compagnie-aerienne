-- =====================================================
-- 1. EMPLOYÉS
-- =====================================================
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'staff'
);

-- =====================================================
-- 2. AÉROPORTS
-- =====================================================
CREATE TABLE airports (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100),
    country VARCHAR(100)
);

-- =====================================================
-- 3. AVIONS
-- =====================================================
CREATE TABLE aircrafts (
    id SERIAL PRIMARY KEY,
    registration VARCHAR(20) UNIQUE NOT NULL,
    model VARCHAR(50) NOT NULL,
    total_seats INT NOT NULL CHECK (total_seats > 0)
);

-- =====================================================
-- 4. LIGNES DE VOL (ROUTES)
-- =====================================================
CREATE TABLE flight_routes (
    id SERIAL PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL,
    departure_airport_id INT NOT NULL REFERENCES airports(id),
    arrival_airport_id INT NOT NULL REFERENCES airports(id),
    CONSTRAINT unique_route UNIQUE (flight_number, departure_airport_id, arrival_airport_id),
    CONSTRAINT different_airports CHECK (departure_airport_id <> arrival_airport_id)
);

-- =====================================================
-- 5. EXÉCUTIONS DE VOL
-- =====================================================
CREATE TABLE flight_instances (
    id SERIAL PRIMARY KEY,
    route_id INT NOT NULL REFERENCES flight_routes(id) ON DELETE CASCADE,
    aircraft_id INT NOT NULL REFERENCES aircrafts(id),
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    flight_date DATE NOT NULL,
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price >= 0),
    status VARCHAR(20) DEFAULT 'scheduled',
    CONSTRAINT valid_time CHECK (arrival_time > departure_time)
);

-- =====================================================
-- 6. PASSAGERS
-- =====================================================
CREATE TABLE passengers (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    passport_number VARCHAR(50) UNIQUE,
    date_of_birth DATE
);

-- =====================================================
-- 7. RÉSERVATIONS
-- =====================================================
CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    booking_reference VARCHAR(15) UNIQUE NOT NULL,
    flight_instance_id INT NOT NULL REFERENCES flight_instances(id) ON DELETE CASCADE,
    passenger_id INT NOT NULL REFERENCES passengers(id) ON DELETE CASCADE,
    seat_number VARCHAR(10) NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) DEFAULT 'confirmed'
);

-- =====================================================
-- 8. PAIEMENTS
-- =====================================================
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    booking_id INT UNIQUE REFERENCES bookings(id) ON DELETE CASCADE,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(20) DEFAULT 'paid'
);

-- =====================================================
-- 9. CONTRAINTES MÉTIER
-- =====================================================
ALTER TABLE bookings
ADD CONSTRAINT unique_seat_per_flight_instance
UNIQUE (flight_instance_id, seat_number);
