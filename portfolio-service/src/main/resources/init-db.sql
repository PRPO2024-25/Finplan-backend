-- First, clear existing data
TRUNCATE TABLE stocks CASCADE;
TRUNCATE TABLE portfolios CASCADE;

-- Reset and create sequences
DROP SEQUENCE IF EXISTS portfolios_id_seq CASCADE;
DROP SEQUENCE IF EXISTS stocks_id_seq CASCADE;

CREATE SEQUENCE portfolios_id_seq START WITH 1;
CREATE SEQUENCE stocks_id_seq START WITH 1;

ALTER TABLE portfolios ALTER COLUMN id SET DEFAULT nextval('portfolios_id_seq');
ALTER TABLE stocks ALTER COLUMN id SET DEFAULT nextval('stocks_id_seq');

-- Insert portfolios
INSERT INTO portfolios (id, user_id, cash_balance) VALUES (1, 1, 10000.00),(2, 1, 5000.00),(3, 2, 7500.00);

-- Update the portfolios sequence
SELECT setval('portfolios_id_seq', (SELECT MAX(id) FROM portfolios));

-- Insert stocks
INSERT INTO stocks (id, symbol, name, purchase_price, quantity, current_price, portfolio_id) VALUES (1, 'AAPL', 'Apple Inc.', 150.00, 10, 191.56, 1),(2, 'GOOGL', 'Alphabet Inc.', 120.00, 5, 133.20, 1),(3, 'MSFT', 'Microsoft Corporation', 350.00, 8, 374.58, 2),(4, 'TSLA', 'Tesla Inc.', 180.50, 15, 185.30, 1),(5, 'NVDA', 'NVIDIA Corporation', 420.00, 3, 450.75, 2),(6, 'META', 'Meta Platforms Inc.', 280.00, 7, 295.40, 3),(7, 'AMZN', 'Amazon.com Inc.', 130.00, 12, 145.25, 3),(8, 'AMD', 'Advanced Micro Devices', 95.00, 20, 105.80, 2);

-- Update the stocks sequence
SELECT setval('stocks_id_seq', (SELECT MAX(id) FROM stocks));
