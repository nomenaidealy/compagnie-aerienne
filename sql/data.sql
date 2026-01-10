-- =====================================================
-- 1. EMPLOYÉS
-- =====================================================
INSERT INTO employees (username, password, full_name, role) VALUES
('admin', 'admin123', 'Administrateur', 'admin'),
('staff1', 'staff123', 'Andry Razafy', 'staff'),
('compta', 'compta123', 'Mamy Rasoanaivo', 'accountant');

-- =====================================================
-- 2. AÉROPORTS
-- =====================================================
INSERT INTO airports (code, name, city, country) VALUES
('TNR', 'Ivato International Airport', 'Antananarivo', 'Madagascar'),
('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
('JNB', 'OR Tambo International Airport', 'Johannesburg', 'South Africa'),
('RUN', 'Roland Garros Airport', 'Saint-Denis', 'Réunion'),
('NBE', 'Fascene Airport', 'Nosy Be', 'Madagascar');

-- =====================================================
-- 3. AVIONS
-- =====================================================
INSERT INTO aircrafts (registration, model, total_seats) VALUES
('5R-MDA', 'Airbus A320', 180),
('5R-MDB', 'Boeing 737-800', 162),
('5R-EAA', 'ATR 72-500', 70);

-- =====================================================
-- 4. LIGNES DE VOL (ROUTES)
-- =====================================================
INSERT INTO flight_routes (flight_number, departure_airport_id, arrival_airport_id) VALUES
('MD101', 1, 2),  -- TNR -> CDG
('MD102', 2, 1),  -- CDG -> TNR
('MD201', 1, 3),  -- TNR -> JNB
('MD301', 1, 4),  -- TNR -> RUN
('MD401', 1, 5);  -- TNR -> NBE

-- =====================================================
-- 5. EXÉCUTIONS DE VOL (flight_instances)
-- =====================================================
INSERT INTO flight_instances (route_id, aircraft_id, departure_time, arrival_time, flight_date, base_price, status) VALUES
-- MD101 TNR -> CDG
(1, 1, '2026-02-15 08:00:00', '2026-02-15 18:30:00', '2026-02-15', 450.00, 'scheduled'),
(1, 2, '2026-02-15 20:00:00', '2026-02-16 06:30:00', '2026-02-15', 480.00, 'scheduled'),
-- MD102 CDG -> TNR
(2, 1, '2026-02-20 20:00:00', '2026-02-21 06:30:00', '2026-02-20', 480.00, 'scheduled'),
-- MD201 TNR -> JNB
(3, 3, '2026-02-10 10:30:00', '2026-02-10 14:00:00', '2026-02-10', 220.00, 'scheduled'),
-- MD301 TNR -> RUN
(4, 3, '2026-02-12 07:45:00', '2026-02-12 09:15:00', '2026-02-12', 150.00, 'scheduled'),
-- MD401 TNR -> NBE
(5, 1, '2026-01-12 12:00:00', '2026-01-12 13:30:00', '2026-01-12', 120.00, 'scheduled');

-- =====================================================
-- 6. PASSAGERS
-- =====================================================
INSERT INTO passengers (first_name, last_name, email, phone, passport_number, date_of_birth) VALUES
('John', 'Doe', 'john.doe@example.com', '0341234567', 'AB123456', '1990-05-15');

-- =====================================================
-- 7. RÉSERVATIONS
-- =====================================================
INSERT INTO bookings (booking_reference, flight_instance_id, passenger_id, seat_number, total_amount, status) VALUES
('BKG001', 6, 1, '1A', 120.00, 'confirmed');  -- le vol TNR -> NBE correspond à flight_instance id = 6

-- =====================================================
-- 8. PAIEMENTS
-- =====================================================
INSERT INTO payments (booking_id, amount, payment_method, status) VALUES
(1, 120.00, 'card', 'paid');
