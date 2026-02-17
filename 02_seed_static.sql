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
INSERT INTO Menu_Items(item_name, base_price, item_type) VALUES
('Classic Milk Tea', 5.50, 'Drink'),
('Taro Milk Tea', 6.25, 'Drink'),
('Thai Milk Tea', 6.00, 'Drink'),
('Matcha Latte', 6.75, 'Drink'),
('Brown Sugar Boba', 6.95, 'Drink'),
('Honeydew Milk Tea', 6.25, 'Drink'),
('Mango Green Tea', 5.75, 'Drink'),
('Peach Green Tea', 5.75, 'Drink'),
('Strawberry Slush', 6.50, 'Drink'),
('Taro Slush', 6.75, 'Drink'),
('Coffee Milk Tea', 6.25, 'Drink'),
('Jasmine Milk Tea', 5.75, 'Drink'),
('Oolong Milk Tea', 5.75, 'Drink'),
('Wintermelon Tea', 5.50, 'Drink'),
('Passionfruit Green Tea', 5.95, 'Drink'),
('Lychee Green Tea', 5.95, 'Drink'),
('Chocolate Milk Tea', 6.25, 'Drink'),
('Vanilla Milk Tea', 6.10, 'Drink'),
('Ube Milk Tea', 6.60, 'Drink'),
('Salted Cream Tea', 6.90, 'Drink');


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

-- RECIPES FOR ALL MENU ITEMS
-- Common Supplies Used in Every Drink:
-- Ice, Cups 24oz, Sealing Film, Straws, Napkins, Bags

INSERT INTO Recipes (menu_item_id, inventory_id, quantity_used)
SELECT m.menu_item_id, i.inventory_id, r.qty
FROM (
VALUES


-- CLASSIC MILK TEA
('Classic Milk Tea','Black Tea Leaves (bags)',1),
('Classic Milk Tea','Milk (liters)',0.25),
('Classic Milk Tea','Non-dairy Creamer (kg)',0.05),
('Classic Milk Tea','Brown Sugar Syrup (liters)',0.05),
('Classic Milk Tea','Tapioca Pearls (kg)',0.10),

-- TARO MILK TEA
('Taro Milk Tea','Black Tea Leaves (bags)',1),
('Taro Milk Tea','Milk (liters)',0.25),
('Taro Milk Tea','Taro Powder (g)',30),

-- THAI MILK TEA
('Thai Milk Tea','Black Tea Leaves (bags)',1),
('Thai Milk Tea','Milk (liters)',0.25),
('Thai Milk Tea','Half & Half (liters)',0.10),

-- MATCHA LATTE
('Matcha Latte','Matcha Powder (g)',25),
('Matcha Latte','Milk (liters)',0.30),

-- BROWN SUGAR BOBA
('Brown Sugar Boba','Milk (liters)',0.30),
('Brown Sugar Boba','Brown Sugar Syrup (liters)',0.10),
('Brown Sugar Boba','Tapioca Pearls (kg)',0.15),

-- HONEYDEW MILK TEA
('Honeydew Milk Tea','Black Tea Leaves (bags)',1),
('Honeydew Milk Tea','Milk (liters)',0.25),
('Honeydew Milk Tea','Honey Syrup (liters)',0.05),

-- MANGO GREEN TEA
('Mango Green Tea','Green Tea Leaves (bags)',1),
('Mango Green Tea','Fruit Syrup - Mango (liters)',0.10),

-- PEACH GREEN TEA
('Peach Green Tea','Green Tea Leaves (bags)',1),
('Peach Green Tea','Fruit Syrup - Peach (liters)',0.10),

-- STRAWBERRY SLUSH
('Strawberry Slush','Fruit Syrup - Mango (liters)',0.05),
('Strawberry Slush','Ice (kg)',0.40),

-- TARO SLUSH
('Taro Slush','Taro Powder (g)',35),
('Taro Slush','Ice (kg)',0.40),

-- COFFEE MILK TEA
('Coffee Milk Tea','Black Tea Leaves (bags)',1),
('Coffee Milk Tea','Milk (liters)',0.25),

-- JASMINE MILK TEA
('Jasmine Milk Tea','Green Tea Leaves (bags)',1),
('Jasmine Milk Tea','Milk (liters)',0.25),

-- OOLONG MILK TEA
('Oolong Milk Tea','Oolong Tea Leaves (bags)',1),
('Oolong Milk Tea','Milk (liters)',0.25),

-- WINTERMELON TEA
('Wintermelon Tea','Black Tea Leaves (bags)',1),

-- PASSIONFRUIT GREEN TEA
('Passionfruit Green Tea','Green Tea Leaves (bags)',1),
('Passionfruit Green Tea','Fruit Syrup - Passionfruit (liters)',0.10),

-- LYCHEE GREEN TEA
('Lychee Green Tea','Green Tea Leaves (bags)',1),
('Lychee Green Tea','Fruit Syrup - Lychee (liters)',0.10),

-- CHOCOLATE MILK TEA
('Chocolate Milk Tea','Black Tea Leaves (bags)',1),
('Chocolate Milk Tea','Milk (liters)',0.25),

-- VANILLA MILK TEA
('Vanilla Milk Tea','Black Tea Leaves (bags)',1),
('Vanilla Milk Tea','Milk (liters)',0.25),

-- UBE MILK TEA
('Ube Milk Tea','Black Tea Leaves (bags)',1),
('Ube Milk Tea','Ube Powder (g)',30),
('Ube Milk Tea','Milk (liters)',0.25),

-- SALTED CREAM TEA
('Salted Cream Tea','Black Tea Leaves (bags)',1),
('Salted Cream Tea','Half & Half (liters)',0.15),

-- COMMON SUPPLIES FOR ALL DRINKS
('Classic Milk Tea','Ice (kg)',0.20),
('Taro Milk Tea','Ice (kg)',0.20),
('Thai Milk Tea','Ice (kg)',0.20),
('Matcha Latte','Ice (kg)',0.20),
('Brown Sugar Boba','Ice (kg)',0.20),
('Honeydew Milk Tea','Ice (kg)',0.20),
('Mango Green Tea','Ice (kg)',0.20),
('Peach Green Tea','Ice (kg)',0.20),
('Coffee Milk Tea','Ice (kg)',0.20),
('Jasmine Milk Tea','Ice (kg)',0.20),
('Oolong Milk Tea','Ice (kg)',0.20),
('Wintermelon Tea','Ice (kg)',0.20),
('Passionfruit Green Tea','Ice (kg)',0.20),
('Lychee Green Tea','Ice (kg)',0.20),
('Chocolate Milk Tea','Ice (kg)',0.20),
('Vanilla Milk Tea','Ice (kg)',0.20),
('Ube Milk Tea','Ice (kg)',0.20),
('Salted Cream Tea','Ice (kg)',0.20),

-- packaging for all
('Classic Milk Tea','Cups 24oz (count)',1),
('Taro Milk Tea','Cups 24oz (count)',1),
('Thai Milk Tea','Cups 24oz (count)',1),
('Matcha Latte','Cups 24oz (count)',1),
('Brown Sugar Boba','Cups 24oz (count)',1),
('Honeydew Milk Tea','Cups 24oz (count)',1),
('Mango Green Tea','Cups 24oz (count)',1),
('Peach Green Tea','Cups 24oz (count)',1),
('Strawberry Slush','Cups 24oz (count)',1),
('Taro Slush','Cups 24oz (count)',1),
('Coffee Milk Tea','Cups 24oz (count)',1),
('Jasmine Milk Tea','Cups 24oz (count)',1),
('Oolong Milk Tea','Cups 24oz (count)',1),
('Wintermelon Tea','Cups 24oz (count)',1),
('Passionfruit Green Tea','Cups 24oz (count)',1),
('Lychee Green Tea','Cups 24oz (count)',1),
('Chocolate Milk Tea','Cups 24oz (count)',1),
('Vanilla Milk Tea','Cups 24oz (count)',1),
('Ube Milk Tea','Cups 24oz (count)',1),
('Salted Cream Tea','Cups 24oz (count)',1)

) AS r(item_name, inventory_name, qty)
JOIN Menu_Items m ON m.item_name = r.item_name
JOIN Inventory i ON i.item_name = r.inventory_name;

-- ADD-ONS (inventory-aware)
-- Add-ons are Menu_Items with item_type='Addon'

INSERT INTO Menu_Items(item_name, base_price, item_type) VALUES
('Add Boba', 0.75, 'Addon'),
('Add Jelly', 0.60, 'Addon'),
('Add Popping Boba', 0.85, 'Addon'),
('Salted Cream Top', 0.90, 'Addon'),
('Extra Honey', 0.40, 'Addon'),
('Extra Brown Sugar', 0.40, 'Addon');

-- Recipes for add-ons (consume inventory)
INSERT INTO Recipes (menu_item_id, inventory_id, quantity_used)
SELECT m.menu_item_id, i.inventory_id, r.qty
FROM (
  VALUES
  ('Add Boba', 'Tapioca Pearls (kg)', 0.08),
  ('Add Jelly', 'Jelly Topping (kg)', 0.06),
  ('Add Popping Boba', 'Popping Boba (kg)', 0.06),
  ('Salted Cream Top', 'Half & Half (liters)', 0.08),
  ('Extra Honey', 'Honey Syrup (liters)', 0.03),
  ('Extra Brown Sugar', 'Brown Sugar Syrup (liters)', 0.03)
) AS r(item_name, inventory_name, qty)
JOIN Menu_Items m ON m.item_name = r.item_name
JOIN Inventory i ON i.item_name = r.inventory_name;
