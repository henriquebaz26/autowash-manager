

PRAGMA foreign_keys = ON;


CREATE TABLE IF NOT EXISTS customer (

id INTEGER PRIMARY KEY AUTOINCREMENT,

name TEXT NOT NULL,

phone TEXT,

email TEXT,

cep TEXT,

street TEXT,

number TEXT,

neighborhood TEXT,

city TEXT,

state TEXT

);


CREATE TABLE IF NOT EXISTS vehicle (

id INTEGER PRIMARY KEY AUTOINCREMENT,

plate TEXT NOT NULL,

brand TEXT,

model TEXT,

color TEXT,

customer_id INTEGER,

FOREIGN KEY (customer_id) REFERENCES customer(id)

);


CREATE TABLE IF NOT EXISTS employee (

id INTEGER PRIMARY KEY AUTOINCREMENT,

name TEXT NOT NULL,

phone TEXT,

role TEXT NOT NULL,

username TEXT UNIQUE,

password TEXT,

active INTEGER

);


CREATE TABLE IF NOT EXISTS product (

id INTEGER PRIMARY KEY AUTOINCREMENT,

name TEXT NOT NULL,

price REAL,

stock INTEGER,

min_stock INTEGER

);


CREATE TABLE IF NOT EXISTS service_order (

id INTEGER PRIMARY KEY AUTOINCREMENT,

entry_time TEXT,

delivery_time TEXT,

price REAL,

status TEXT,

vehicle_id INTEGER,

employee_id INTEGER,

notes TEXT,

FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),

FOREIGN KEY (employee_id) REFERENCES employee(id)

);


CREATE TABLE IF NOT EXISTS order_item (

id INTEGER PRIMARY KEY AUTOINCREMENT,

order_id INTEGER,

product_id INTEGER,

quantity INTEGER,

FOREIGN KEY (order_id) REFERENCES service_order(id),

FOREIGN KEY (product_id) REFERENCES product(id)

);


CREATE TABLE IF NOT EXISTS expense (

id INTEGER PRIMARY KEY AUTOINCREMENT,

description TEXT NOT NULL,

value REAL,

due_date TEXT,

payment_date TEXT,

status TEXT

);