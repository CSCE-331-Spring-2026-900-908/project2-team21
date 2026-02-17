SELECT SUM(total_amount) FROM Orders; --Shows sales are >1M 
SELECT MAX(order_timestamp) - MIN(order_timestamp) AS interval_difference FROM Orders;  --Shows that we have 52 weeks of orders
SELECT DATE(order_timestamp), SUM(total_amount) FROM Orders GROUP BY DATE(order_timestamp) ORDER BY SUM(total_amount) DESC LIMIT 4;  --Shows that we have precisely 3 peak days with the next being a good amount in profit away
SELECT COUNT(*) AS count_menu_items FROM menu_items; --Enumerates all menu items
SELECT COUNT(*) AS count_menu_items FROM menu_items WHERE item_type = 'Drink'; --Enumerates all menu items EXLCUDING ADDONS
SELECT COUNT(*) AS count_employees FROM employees; --Enumerates count of employees
SELECT * FROM inventory WHERE quantity_in_stock < reorder_level; --Shows all items in inventory that need to be reordered
SELECT COUNT(CASE WHEN total_amount < 10 THEN 1 END) AS "<$10", COUNT(CASE WHEN total_amount BETWEEN 10 AND 20 THEN 1 END) AS "$10-$20", COUNT(CASE WHEN total_amount > 20 THEN 1 END) AS "$20+" FROM orders; --Shows the orders in different categories of sales amount