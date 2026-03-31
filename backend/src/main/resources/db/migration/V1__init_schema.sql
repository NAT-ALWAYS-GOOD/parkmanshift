CREATE TABLE skeleton_log (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(255),
    timestamp TIMESTAMP
);

CREATE TABLE parking_spot (
    label VARCHAR(3) PRIMARY KEY,
    type VARCHAR(50) NOT NULL
);

CREATE TABLE reservation (
    id UUID PRIMARY KEY,
    parking_spot_label VARCHAR(3) NOT NULL,
    employee_id VARCHAR(50) NOT NULL,
    reservation_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reservation_spot FOREIGN KEY (parking_spot_label) REFERENCES parking_spot(label)
);
