INSERT INTO investors (id, age, email, first_name, last_name) VALUES (1, 35, 'thabo.nkosi@example.com', 'Thabo', 'Nkosi');
INSERT INTO investors (id, age, email, first_name, last_name) VALUES (2, 70, 'naledi.dube@example.com', 'Naledi', 'Dube');
INSERT INTO investors (id, age, email, first_name, last_name) VALUES (3, 40, 'lindiwe.khumalo@example.com', 'Lindiwe', 'Khumalo');

INSERT INTO portfolios (id, balance, investor_id) VALUES (1, 148800.00, 1);
INSERT INTO portfolios (id, balance, investor_id) VALUES (2, 500000.00, 2);
INSERT INTO portfolios (id, balance, investor_id) VALUES (3, 90000.00, 3);

INSERT INTO products (id, amount, initial_amount, name, type, portfolio_id) VALUES (1, 100000.00, 100000.00, 'Flexible Unit Trust', 'UNIT_TRUST', 1);
INSERT INTO products (id, amount, initial_amount, name, type, portfolio_id) VALUES (2, 48800.00, 48800.00, 'Retirement Annuity Fund', 'RETIREMENT_ANNUITY', 1);

INSERT INTO products (id, amount, initial_amount, name, type, portfolio_id) VALUES (3, 200000.00, 200000.00, 'Flexible Unit Trust', 'UNIT_TRUST', 2);
INSERT INTO products (id, amount, initial_amount, name, type, portfolio_id) VALUES (4, 300000.00, 300000.00, 'Retirement Annuity Fund', 'RETIREMENT_ANNUITY', 2);

INSERT INTO products (id, amount, initial_amount, name, type, portfolio_id) VALUES (5, 40000.00, 40000.00, 'Flexible Unit Trust', 'UNIT_TRUST', 3);
INSERT INTO products (id, amount, initial_amount, name, type, portfolio_id) VALUES (6, 50000.00, 50000.00, 'Retirement Annuity Fund', 'RETIREMENT_ANNUITY', 3);

ALTER TABLE investors ALTER COLUMN id RESTART WITH 4;
ALTER TABLE portfolios ALTER COLUMN id RESTART WITH 4;
ALTER TABLE products ALTER COLUMN id RESTART WITH 7;