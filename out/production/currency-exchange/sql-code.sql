CREATE TABLE currency(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    sign TEXT NOT NULL
);

CREATE TABLE exchange_rate(
    id INTEGER PRIMARY KEY AUTOINCREMENT ,
    base_currency_id INTEGER NOT NULL REFERENCES currency(id) ON DELETE CASCADE,
    target_currency_id INTEGER NOT NULL REFERENCES currency(id) ON DELETE CASCADE,
    rate REAL(6) NOT NULL
);

INSERT INTO currency (code, full_name, sign) VALUES ('USD', 'US Dollar', '$');
INSERT INTO currency (code, full_name, sign) VALUES ('EUR', 'Euro', '€');
INSERT INTO currency (code, full_name, sign) VALUES ('RUB', 'Russian Ruble', '₽');
INSERT INTO currency (code, full_name, sign) VALUES ('UAH', 'Hryvnia', '₴');
INSERT INTO currency (code, full_name, sign) VALUES ('KZT', 'Tenge', '₸');
INSERT INTO currency (code, full_name, sign) VALUES ('GBP', 'Pound Sterling', '£');

INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES (1, 2,0.94);
INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES (1, 3, 63.75);
INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES (1, 4, 36.95);
INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES (1, 5, 469.88);
INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) VALUES (1, 6, 0.81);

