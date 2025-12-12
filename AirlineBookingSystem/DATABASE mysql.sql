CREATE DATABASE ByteAir;
USE ByteAir;

-- 1) CREATE TABLES

CREATE TABLE Admin (
    admin_id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(50),
    middle_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(15),
    gender ENUM('Male', 'Female')
);

CREATE TABLE Customer (
    customer_id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(50),
    middle_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(15),
    gender ENUM('Male', 'Female'),
    birth_date DATE,
    email VARCHAR(100),
    password VARCHAR(255)  -- بدل الـ ALTER TABLE، ضفناها مباشرة هنا
);

CREATE TABLE Login (
    login_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    role ENUM('Admin', 'Customer'),
    customer_id VARCHAR(50),
    admin_id VARCHAR(50),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (admin_id) REFERENCES Admin(admin_id)
);

CREATE TABLE Flight (
    flight_no VARCHAR(50) PRIMARY KEY,
    flight_name VARCHAR(100),
    from_location VARCHAR(100),
    destination VARCHAR(100),
    departure_time DATETIME,
    arrival_time DATETIME,
    price DECIMAL(10,2),
    capacity INT,
    flight_date DATE,
    flight_status ENUM('Scheduled', 'Delayed', 'Canceled', 'Completed') DEFAULT 'Scheduled'
);

CREATE TABLE Booking (
    booking_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50),
    flight_no VARCHAR(50),
    booking_date DATE,
    status ENUM('Confirmed','Cancelled','Pending'),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (flight_no) REFERENCES Flight(flight_no)
);

CREATE TABLE Ticket (
    ticket_id VARCHAR(50) PRIMARY KEY,
    booking_id VARCHAR(50),
    seat_no VARCHAR(10),
    class ENUM('Economy','Business','First'),
    issue_date DATE,
    ticket_code VARCHAR(100) UNIQUE,
    boarding_gate VARCHAR(10),
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);

CREATE TABLE Passenger (
    passenger_id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    passport_no VARCHAR(30) UNIQUE,
    nationality VARCHAR(50),
    gender ENUM('Male','Female'),
    age INT,
    booking_id VARCHAR(50),
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);

CREATE TABLE Seat (
    seat_no VARCHAR(10),
    flight_no VARCHAR(50),
    class ENUM('Economy','Business','First'),
    status ENUM('Available','Booked') DEFAULT 'Available',
    PRIMARY KEY(seat_no, flight_no),
    FOREIGN KEY (flight_no) REFERENCES Flight(flight_no)
);

CREATE TABLE Payment (
    payment_id VARCHAR(50) PRIMARY KEY,
    amount DECIMAL(10,2),
    payment_date DATE,
    customer_id VARCHAR(50),
    booking_id VARCHAR(50),
    payment_type ENUM('CreditCard'),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);

CREATE TABLE CreditCard (
    card_number VARCHAR(20) PRIMARY KEY,
    card_holder VARCHAR(100),
    expiry_date DATE,
    cvv VARCHAR(4),
    payment_id VARCHAR(50) UNIQUE,
    FOREIGN KEY (payment_id) REFERENCES Payment(payment_id)
);

CREATE TABLE Receipt (
    receipt_id INT PRIMARY KEY AUTO_INCREMENT,
    payment_id VARCHAR(50),
    issued_on DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2),
    FOREIGN KEY (payment_id) REFERENCES Payment(payment_id)
);

CREATE TABLE FlightDeal (
    deal_id VARCHAR(50) PRIMARY KEY,
    flight_no VARCHAR(50),
    title VARCHAR(100),
    description TEXT,
    discount_type ENUM('PERCENT','FIXED'),
    discount_value DECIMAL(10,2),
    start_date DATE,
    end_date DATE,
    is_active TINYINT(1) DEFAULT 1,
    FOREIGN KEY (flight_no) REFERENCES Flight(flight_no)
);

CREATE TABLE TourGuide (
    guide_id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(100),
    city VARCHAR(50),
    price_per_day DECIMAL(10,2),
    is_active TINYINT(1) DEFAULT 1
);

CREATE TABLE TourBooking (
    tour_booking_id VARCHAR(50) PRIMARY KEY,
    customer_id VARCHAR(50),
    guide_id VARCHAR(50),
    start_date DATE,
    end_date DATE,
    total_price DECIMAL(10,2),
    status ENUM('Pending','Confirmed','Cancelled') DEFAULT 'Pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (guide_id) REFERENCES TourGuide(guide_id)
);

CREATE TABLE Feedback (
    feedback_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id VARCHAR(50),
    booking_id VARCHAR(50),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id),
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);

-- =========================================
-- 2) INSERT DATA
-- =========================================

-- ADMINS
INSERT INTO Admin VALUES
('AD-1','Sara','Omar','Al-qahtani','sara72@gmail.com','0573195921','Female'),
('AD-2','Muneera','Abdullah','Al-khaldi','MK72@gmail.com','0573195922','Female'),
('AD-3','Ghala','Mohammed','Al-sharidah','GMS@gmail.com','0573195923','Female'),
('AD-4','Leena','Adel','Al-ibrahim','Lab@gmail.com','0573195920','Female'),
('AD-5','Ghala','Mana','Al-jafr','GMJ@gmail.com','0573195929','Female'),
('AD-6','Rawan','Abdullah','Al-qattan','RAQ72@gmail.com','0573195928','Female'),
('AD-7','Zahra','Haytham','Al-ismail','Zh72@gmail.com','0573185922','Female');

-- CUSTOMERS (C-1A .. C-8A)  + password = 'passXXXX' for example
INSERT INTO Customer VALUES
('C-1A','Sara','Ahmed','Kamal','0557654321','Female','1985-11-20','sara85@gmail.com','pass1234'),
('C-2A','Ali','Saad','Al-Ajmi','0501234567','Male','1999-04-10','ali55@gmail.com','pass5678'),
('C-3A','Fahad','Abdullah','Al-khaldi','0501200566','Male','2001-12-24','FAS90@gmail.com','pass91011'),
('C-4A','Khalid','Abdullah','Al-khaldi','0501289566','Male','2005-11-05','kaj70@gmail.com','pass1213'),
('C-5A','Alreem','Majed','Al-khaldi','0501934586','Female','2004-06-22','AMK0@gmail.com','pass1415'),
('C-6A','Reema','Khalid','Al-Zahrani','0501934566','Female','2000-10-24','RFG90@gmail.com','pass1617'),
('C-7A','Sara','Abdulaziz','Al-subaiey','0531234566','Female','2005-01-13','SAS0@gmail.com','pass1819'),
('C-8A','Fatimah','Hassn','Al-Hassan','0501234577','Female','1990-12-24','FHA55@gmail.com','pass2021');

-- LOGIN (Customers + Admins)
INSERT INTO Login (email,password,role,customer_id,admin_id) VALUES
('sara85@gmail.com','pass1234','Customer','C-1A',NULL),
('ali55@gmail.com','pass5678','Customer','C-2A',NULL),
('FAS90@gmail.com','pass91011','Customer','C-3A',NULL),
('kaj70@gmail.com','pass1213','Customer','C-4A',NULL),
('AMK0@gmail.com','pass1415','Customer','C-5A',NULL),
('RFG90@gmail.com','pass1617','Customer','C-6A',NULL),
('SAS0@gmail.com','pass1819','Customer','C-7A',NULL),
('FHA55@gmail.com','pass2021','Customer','C-8A',NULL),

('sara72@gmail.com','adminpass1','Admin',NULL,'AD-1'),
('MK72@gmail.com','adminpass2','Admin',NULL,'AD-2'),
('GMS@gmail.com','adminpass3','Admin',NULL,'AD-3'),
('Lab@gmail.com','adminpass4','Admin',NULL,'AD-4'),
('GMJ@gmail.com','adminpass5','Admin',NULL,'AD-5'),
('RAQ72@gmail.com','adminpass6','Admin',NULL,'AD-6'),
('Zh72@gmail.com','adminpass7','Admin',NULL,'AD-7');

-- FLIGHTS
INSERT INTO Flight VALUES
('FL100','BYTEAIR A','Riyadh','Dammam','2026-05-01 10:00:00','2026-05-01 13:00:00',300.00,180,'2026-05-01','Scheduled'),
('FL101','BYTEAIR B','Riyadh','London','2026-05-01 07:00:00','2026-05-01 14:00:00',1000.00,180,'2026-05-01','Delayed'),
('FL102','BYTEAIR C','Dammam','Istanbul','2026-05-05 05:00:00','2026-05-05 09:00:00',900.00,180,'2026-05-05','Completed'),
('FL103','BYTEAIR D','Dammam','Jiddah','2026-05-06 05:00:00','2026-05-06 07:05:00',180.00,180,'2026-05-06','Canceled'),
('FL104','BYTEAIR E','Hail','Alqissim','2026-05-01 08:00:00','2026-05-01 10:10:00',190.00,180,'2026-05-01','Scheduled'),
('FL105','BYTEAIR F','Riyadh','Dubai','2026-05-09 08:00:00','2026-05-09 12:00:00',200.00,180,'2026-05-09','Delayed'),
('FL106','BYTEAIR G','Jiddah','Mayami','2026-05-10 08:00:00','2026-05-10 17:30:00',1200.00,180,'2026-05-10','Completed');

-- BOOKINGS
INSERT INTO Booking VALUES
('B-K1','C-1A','FL100','2026-05-01','Confirmed'),
('B-K2','C-2A','FL101','2026-05-01','Cancelled'),
('B-K3','C-3A','FL102','2026-05-05','Pending'),
('B-K4','C-4A','FL103','2026-05-06','Confirmed'),
('B-K5','C-5A','FL104','2026-05-01','Cancelled'),
('B-K6','C-6A','FL105','2026-05-09','Pending'),
('B-K7','C-7A','FL106','2026-05-10','Confirmed');

-- TICKETS (TKT100..TKT106) تناسب دالتك في Java
INSERT INTO Ticket VALUES
('TKT100','B-K1','12A','Economy','2026-05-01','a1b2c3d4','G1'),
('TKT101','B-K2','14B','Business','2026-05-01','e5f6g7h8','G2'),
('TKT102','B-K3','10A','First','2026-05-05','i9j0k1l2','G3'),
('TKT103','B-K4','15C','Economy','2026-05-06','m3n4o5p6','G4'),
('TKT104','B-K5','22B','Business','2026-05-01','q7r8s9t0','G5'),
('TKT105','B-K6','8A','First','2026-05-09','u1v2w3x4','G1'),
('TKT106','B-K7','9B','Economy','2026-05-10','y5z6a7b8','G2');

-- PASSENGERS
INSERT INTO Passenger VALUES
('PAS-S0','Fahad','Alkhaldi','PPT00','Saudi Arabia','Male',25,'B-K1'),
('PAS-S1','Alreem','Alkhaldi','PPT11','Saudi Arabia','Female',30,'B-K2'),
('PAS-S2','Khalid','Alkhaldi','PPT22','Saudi Arabia','Male',40,'B-K3'),
('PAS-S3','Sara','Alsubaie','PPT33','Saudi Arabia','Female',35,'B-K4'),
('PAS-S4','Sara','Kamal','PPT44','Egypt','Female',28,'B-K5'),
('PAS-S5','Fatimah','Alhassan','PPT55','Saudi Arabia','Female',38,'B-K6'),
('PAS-S6','Ali','Alajmi','PPT66','Qatar','Male',29,'B-K7');

-- PAYMENTS (PAY001..PAY007) تناسب الفكرة اللي تبينها
INSERT INTO Payment VALUES
('PAY001',300.00,'2026-05-01','C-1A','B-K1','CreditCard'),
('PAY002',1000.00,'2026-05-01','C-2A','B-K2','CreditCard'),
('PAY003',900.00,'2026-05-05','C-3A','B-K3','CreditCard'),
('PAY004',180.00,'2026-05-06','C-4A','B-K4','CreditCard'),
('PAY005',190.00,'2026-05-01','C-5A','B-K5','CreditCard'),
('PAY006',200.00,'2026-05-09','C-6A','B-K6','CreditCard'),
('PAY007',1200.00,'2026-05-10','C-7A','B-K7','CreditCard');

-- CREDIT CARDS
INSERT INTO CreditCard VALUES
('2233-4354-3331-2332','Sara Kamal','2028-12-01','123','PAY001'),
('1134-5466-2211-3355','Ali Al-Ajmi','2029-05-01','456','PAY002'),
('5676-0099-9988-9889','Fahad Al-khaldi','2028-11-01','789','PAY003'),
('2234-0909-7886-9999','Khalid Al-khaldi','2030-03-01','321','PAY004'),
('1134-5533-5511-1133','Alreem Al-khaldi','2029-07-01','147','PAY005'),
('5544-9000-6622-1134','Reema Al-Zahrani','2029-10-01','654','PAY006'),
('1212-7777-8888-9999','Sara Al-subaiey','2027-08-01','951','PAY007');

-- RECEIPTS
INSERT INTO Receipt (payment_id,total_amount) VALUES
('PAY001',300.00),
('PAY002',1000.00),
('PAY003',900.00),
('PAY004',180.00),
('PAY005',190.00),
('PAY006',200.00),
('PAY007',1200.00);

-- SEATS
INSERT INTO Seat (seat_no, flight_no, class, status) VALUES
('12A', 'FL100', 'Economy', 'Booked'),
('12B', 'FL100', 'Economy', 'Available'),
('1A',  'FL100', 'Business', 'Available'),
('2A',  'FL100', 'First',    'Available'),

('14B', 'FL101', 'Business', 'Booked'),
('14C', 'FL101', 'Business', 'Available'),
('3A',  'FL101', 'First',    'Available'),
('20A', 'FL101', 'Economy',  'Available'),

('10A', 'FL102', 'First',    'Booked'),
('10B', 'FL102', 'First',    'Available'),
('5A',  'FL102', 'Economy',  'Available'),
('6A',  'FL102', 'Business', 'Available');

-- FLIGHT DEALS
INSERT INTO FlightDeal (deal_id, flight_no, title, description,
                        discount_type, discount_value,
                        start_date, end_date, is_active)
VALUES
('DEAL-100', 'FL100', 'Weekend Saver Riyadh–Dammam',
 'Save on weekend flights between Riyadh and Dammam.',
 'PERCENT', 15.00, '2025-12-01', '2026-01-31', 1),

('DEAL-101', 'FL101', 'London Winter Special',
 'Special winter offer on Riyadh to London flights.',
 'FIXED', 200.00, '2025-11-15', '2026-01-15', 1),

('DEAL-102', 'FL102', 'Istanbul Escape Deal',
 'Discounted fares on Dammam → Istanbul.',
 'PERCENT', 10.00, '2025-02-01', '2025-03-01', 1),

('DEAL-200', 'FL103', 'Jeddah Flash Sale',
 'Limited-time discount on Dammam → Jeddah flights.',
 'FIXED', 50.00, '2025-02-10', '2025-03-01', 1),

('DEAL-201', 'FL105', 'Dubai Weekend Booster',
 'Exclusive Dubai weekend deal with extra savings.',
 'PERCENT', 12.00, '2025-02-01', '2025-03-10', 1),

('DEAL-202', 'FL106', 'USA Long-Haul Mega Discount',
 'Save big on Jeddah → Miami long-haul flights.',
 'FIXED', 300.00, '2025-03-01', '2025-04-01', 1);

-- TOUR GUIDES
INSERT INTO TourGuide (guide_id, full_name, city, price_per_day, is_active) VALUES
('TG-1', 'Ahmed Al-Faraj', 'Riyadh',   350.00, 1),
('TG-2', 'Fatimah Al-Hassan', 'London', 700.00, 1),
('TG-3', 'Mehmet Yilmaz', 'Istanbul',  500.00, 1),
('TG-4', 'Sara Al-Mutairi', 'Dubai',   450.00, 0);

-- TOUR BOOKINGS
INSERT INTO TourBooking (tour_booking_id, customer_id, guide_id,
                         start_date, end_date, total_price, status)
VALUES
('TB-001', 'C-1A', 'TG-1', '2026-05-02', '2026-05-03', 700.00, 'Confirmed'),
('TB-002', 'C-2A', 'TG-3', '2026-05-06', '2026-05-08', 1500.00, 'Pending'),
('TB-003', 'C-3A', 'TG-2', '2026-05-10', '2026-05-12', 2100.00, 'Cancelled');

-- FEEDBACK
INSERT INTO Feedback (customer_id, booking_id, rating, comments) VALUES
('C-1A', 'B-K1', 5, 'Amazing experience! Smooth check-in and friendly staff.'),
('C-2A', 'B-K2', 3, 'Flight was cancelled, but refund was processed quickly.'),
('C-3A', 'B-K3', 4, 'Good service, but boarding was a bit delayed.'),
('C-4A', 'B-K4', 2, 'Seats were uncomfortable and gate area was crowded.'),
('C-5A', 'B-K5', 4, 'Overall good, but would like more meal options.'),
('C-6A', 'B-K6', 5, 'Excellent! Great value for the price.'),
('C-7A', 'B-K7', 5, 'Loved the crew and in-flight entertainment.');

ALTER TABLE Ticket
    ADD COLUMN deal_id VARCHAR(50) NULL,
    ADD CONSTRAINT fk_ticket_deal
        FOREIGN KEY (deal_id) REFERENCES FlightDeal(deal_id);

ALTER TABLE Ticket
ADD COLUMN status ENUM('Active','Cancelled') DEFAULT 'Active';

