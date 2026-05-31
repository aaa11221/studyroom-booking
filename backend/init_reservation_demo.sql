SET NAMES utf8mb4;

DROP DATABASE IF EXISTS reservation_demo;
CREATE DATABASE reservation_demo
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE reservation_demo;

CREATE TABLE student (
  s_id VARCHAR(32) PRIMARY KEY,
  s_name VARCHAR(64) NOT NULL,
  password VARCHAR(128) NULL,
  s_class VARCHAR(32) NULL,
  s_year VARCHAR(16) NULL,
  s_major VARCHAR(64) NULL,
  s_phone_number VARCHAR(32) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_building (
  building_id VARCHAR(32) PRIMARY KEY,
  building_name VARCHAR(64) NOT NULL,
  building_phone_number VARCHAR(32) NULL,
  building_location VARCHAR(64) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE classroom (
  room_id VARCHAR(32) PRIMARY KEY,
  room_name VARCHAR(64) NOT NULL,
  building_id VARCHAR(32) NOT NULL,
  room_floor VARCHAR(16) NULL,
  available_seat INT NOT NULL DEFAULT 0,
  is_multimedia_room VARCHAR(8) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE time_table (
  time_id VARCHAR(32) NOT NULL,
  room_id VARCHAR(32) NOT NULL,
  building_id VARCHAR(32) NOT NULL,
  time_name VARCHAR(64) NULL,
  time_begin VARCHAR(16) NOT NULL,
  time_end VARCHAR(16) NOT NULL,
  PRIMARY KEY (time_id, room_id, building_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE room_available_time_info (
  time_id VARCHAR(32) NOT NULL,
  room_id VARCHAR(32) NOT NULL,
  building_id VARCHAR(32) NOT NULL,
  available_date DATE NOT NULL,
  reservation_num INT NOT NULL DEFAULT 0,
  available_num INT NOT NULL DEFAULT 0,
  PRIMARY KEY (time_id, room_id, building_id, available_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE student_reservation (
  s_id VARCHAR(32) NOT NULL,
  time_id VARCHAR(32) NOT NULL,
  room_id VARCHAR(32) NOT NULL,
  building_id VARCHAR(32) NOT NULL,
  reservation_date DATE NOT NULL,
  state VARCHAR(32) NOT NULL,
  room_name VARCHAR(64) NULL,
  PRIMARY KEY (s_id, time_id, room_id, building_id, reservation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blacklist (
  s_id VARCHAR(32) PRIMARY KEY,
  date_begin DATE NULL,
  date_end DATE NULL,
  state VARCHAR(32) NULL,
  blacker_id VARCHAR(32) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO student (s_id, s_name, password, s_class, s_year, s_major, s_phone_number) VALUES
('32001033', '李尚鑫', '111', NULL, NULL, NULL, NULL),
('32001041', '文权', '123', NULL, NULL, NULL, NULL),
('32001042', '王紫龙', '123', NULL, NULL, NULL, NULL),
('32001120', '余家好', '123', NULL, NULL, NULL, NULL),
('32001137', '宋明明', '123', NULL, NULL, NULL, NULL),
('32001173', '张景宣', '123', NULL, NULL, NULL, NULL),
('32001199', '余钧豪', '123', NULL, NULL, NULL, NULL),
('32001257', '诸思成', '123', NULL, NULL, NULL, NULL),
('32001261', '舒恒鑫', '123', NULL, NULL, NULL, NULL),
('32001272', '徐彬涵', NULL, NULL, NULL, NULL, NULL),
('32001280', '李鼎辉', '123', NULL, NULL, NULL, NULL),
('32001286', '何彬玮', '123', NULL, NULL, NULL, NULL),
('32009081', '傅玉', '123', NULL, NULL, NULL, NULL),
('admin', 'mango', '111', NULL, NULL, NULL, NULL);

INSERT INTO t_building (building_id, building_name, building_phone_number, building_location) VALUES
('B01', 'Main Building', '010-00000000', 'North Campus');

INSERT INTO classroom (room_id, room_name, building_id, room_floor, available_seat, is_multimedia_room) VALUES
('R101', 'Room 101', 'B01', '1', 60, '1');

INSERT INTO time_table (time_id, room_id, building_id, time_name, time_begin, time_end) VALUES
('T01', 'R101', 'B01', 'Period 1', '08:00', '10:00'),
('T02', 'R101', 'B01', 'Period 2', '10:00', '12:00');

INSERT INTO room_available_time_info (time_id, room_id, building_id, available_date, reservation_num, available_num) VALUES
('T01', 'R101', 'B01', '2026-04-12', 0, 60),
('T02', 'R101', 'B01', '2026-04-12', 0, 60),
('T01', 'R101', 'B01', '2026-04-13', 0, 60),
('T02', 'R101', 'B01', '2026-04-13', 0, 60);
