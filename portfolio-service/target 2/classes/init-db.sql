INSERT INTO portfolios (id, name, user_id, cash_balance) VALUES (1, 'Tech Growth', 1, 10000.00), (2, 'Blue Chips', 1, 5000.00), (3, 'Innovation', 2, 7500.00);
SELECT setval('portfolios_id_seq', 3, true);
INSERT INTO stocks (id, symbol, name, purchase_price, quantity, current_price, portfolio_id) VALUES (1, 'AAPL', 'Apple Inc.', 150.00, 10, 191.56, 1), (2, 'GOOGL', 'Alphabet Inc.', 120.00, 5, 133.20, 1), (3, 'MSFT', 'Microsoft Corporation', 350.00, 8, 374.58, 2);
SELECT setval('stocks_id_seq', 3, true);
