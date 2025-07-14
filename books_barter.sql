
CREATE DATABASE IF NOT EXISTS books_barter;
USE books_barter;

--  Drop tables if already exist
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS students;


CREATE TABLE students (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  email VARCHAR(100),
  year ENUM('FE','SE','TE','BE'),
  branch VARCHAR(50)
);


CREATE TABLE books (
  book_id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(100),
  author VARCHAR(100),
  subject VARCHAR(100),
  branch VARCHAR(50),
  semester INT,
  donated_by INT,
  FOREIGN KEY (donated_by) REFERENCES students(id) ON DELETE CASCADE
);


CREATE TABLE requests (
  request_id INT AUTO_INCREMENT PRIMARY KEY,
  book_id INT,
  requested_by INT,
  status ENUM('pending','approved','denied') DEFAULT 'pending',
  FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
  FOREIGN KEY (requested_by) REFERENCES students(id) ON DELETE CASCADE
);