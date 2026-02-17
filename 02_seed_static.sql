-- 02_seed_static.sql

-- Employees (mix of managers + cashiers)
INSERT INTO Employees(first_name, last_name, role) VALUES
('Kevin','Nguyen','Manager'),
('Daniel','Bruni','Manager'),
('Sebastian','Warren','Cashier'),
('Sidharth','Kammili','Cashier'),
('Sri','Middela','Cashier'),
('Alex','Tran','Cashier'),
('Jordan','Lee','Cashier'),
('Taylor','Kim','Cashier'),
('Morgan','Patel','Cashier'),
('Casey','Rivera','Cashier');

-- Menu items (δ = 20)
INSERT INTO Menu_Items(item_name, base_price) VALUES
('Classic Milk Tea', 5.50),
('Taro Milk Tea', 6.25),
('Thai Milk Tea', 6.00),
('Matcha Latte', 6.75),
('Brown Sugar Boba', 6.95),
('Honeydew Milk Tea', 6.25),
('Mango Green Tea', 5.75),
('Peach Green Tea', 5.75),
('Strawberry Slush', 6.50),
('Taro Slush', 6.75),
('Coffee Milk Tea', 6.25),
('Jasmine Milk Tea', 5.75),
('Oolong Milk Tea', 5.75),
('Wintermelon Tea', 5.50),
('Passionfruit Green Tea', 5.95),
('Lychee Green Tea', 5.95),
('Chocolate Milk Tea', 6.25),
('Vanilla Milk Tea', 6.10),
('Ube Milk Tea', 6.60),
('Salted Cream Tea', 6.90);

-- Inventory (examples: ingredients + supplies)
INSERT INTO Inventory(item_name, quantity_in_stock, reorder_level) VALUES
('Black Tea Leaves (bags)', 500, 100),
('Green Tea Leaves (bags)', 450, 100),
('Oolong Tea Leaves (bags)', 300, 75),
('Matcha Powder (g)', 20000, 5000),
('Taro Powder (g)', 25000, 6000),
('Ube Powder (g)', 15000, 4000),
('Milk (liters)', 1200, 250),
('Half & Half (liters)', 300, 60),
('Non-dairy Creamer (kg)', 100, 20),
('Brown Sugar Syrup (liters)', 200, 40),
('Honey Syrup (liters)', 120, 25),
('Fruit Syrup - Mango (liters)', 120, 25),
('Fruit Syrup - Peach (liters)', 120, 25),
('Fruit Syrup - Lychee (liters)', 120, 25),
('Fruit Syrup - Passionfruit (liters)', 120, 25),
('Tapioca Pearls (kg)', 500, 100),
('Popping Boba (kg)', 120, 25),
('Jelly Topping (kg)', 120, 25),
('Ice (kg)', 2000, 400),
('Cups 16oz (count)', 50000, 10000),
('Cups 24oz (count)', 30000, 6000),
('Sealing Film (rolls)', 300, 60),
('Straws (count)', 60000, 12000),
('Napkins (count)', 80000, 15000),
('Bags (count)', 20000, 4000);
