SELECT SUM(total_amount) FROM Orders; --Shows sales are >1M 
SELECT MAX(order_timestamp) - MIN(order_timestamp) AS interval_difference FROM Orders;  --Shows that we have 52 weeks of orders
SELECT DATE(order_timestamp), SUM(total_amount) FROM Orders GROUP BY DATE(order_timestamp) ORDER BY SUM(total_amount) DESC LIMIT 4;  --Shows that we have precisely 3 peak days with the next being a good amount in profit away
SELECT COUNT(*) AS count_menu_items FROM menu_items; --Enumerates all menu items
SELECT COUNT(*) AS count_menu_items FROM menu_items WHERE item_type = 'Drink'; --Enumerates all menu items EXLCUDING ADDONS
SELECT COUNT(*) AS count_employees FROM employees; --Enumerates count of employees
SELECT * FROM inventory WHERE quantity_in_stock < reorder_level; --Shows all items in inventory that need to be reordered
SELECT COUNT(CASE WHEN total_amount < 10 THEN 1 END) AS "<$10", COUNT(CASE WHEN total_amount BETWEEN 10 AND 20 THEN 1 END) AS "$10-$20", COUNT(CASE WHEN total_amount > 20 THEN 1 END) AS "$20+" FROM orders; --Shows the count of orders in different categories of sales amount
SELECT m.item_name, SUM(oli.quantity) AS total_sold FROM order_line_items oli JOIN menu_items m ON m.menu_item_id = oli.menu_item_id WHERE m.item_type = 'Drink' GROUP BY m.item_name ORDER BY total_sold DESC LIMIT 10; --Top 10 best-selling drinks by quantity
SELECT ma.item_name AS addon_name, SUM(lia.quantity) AS addon_units FROM line_item_add_ons lia JOIN menu_items ma ON ma.menu_item_id = lia.add_on_menu_item_id GROUP BY ma.item_name ORDER BY addon_units DESC LIMIT 10; --Top 10 most-used add-ons
SELECT EXTRACT(HOUR FROM order_timestamp) AS hour_of_day, COUNT(*) AS order_count FROM orders GROUP BY hour_of_day ORDER BY order_count DESC LIMIT 1; --Busiest hour by number of orders
SELECT ROUND(AVG(total_amount), 2) AS avg_order_total FROM orders; --Average order total (sanity check)
SELECT DATE_TRUNC('week', order_timestamp) AS week_start, SUM(total_amount) AS weekly_sales FROM orders GROUP BY week_start ORDER BY weekly_sales DESC LIMIT 5; --Top 5 highest-sales weeks
SELECT m.item_name AS menu_item, COUNT(*) AS times_sold, ROUND(SUM(oli.sale_price * oli.quantity), 2) AS revenue FROM order_line_items oli JOIN menu_items m ON m.menu_item_id = oli.menu_item_id GROUP BY m.item_name ORDER BY revenue DESC LIMIT 10; --Top 10 menu items by revenue
SELECT TO_CHAR(order_timestamp, 'Day') AS day_of_week, SUM(total_amount) AS total_revenue FROM orders GROUP BY day_of_week, EXTRACT(DOW FROM order_timestamp) ORDER BY EXTRACT(DOW FROM order_timestamp); --Total revenue grouped by day of the week
SELECT order_id, employee_id, order_timestamp, total_amount FROM orders ORDER BY total_amount DESC LIMIT 1; --Finds the single largest transaction by revenue to identify your biggest sales
SELECT ROUND(AVG(item_count), 2) AS avg_drinks_per_order FROM (SELECT order_id, SUM(quantity) AS item_count FROM order_line_items GROUP BY order_id) AS order_totals; --Calculates the average number of drinks purchased per order
SELECT (SELECT SUM(quantity) FROM line_item_add_ons) AS total_addons_sold, (SELECT SUM(quantity) FROM order_line_items) AS total_drinks_sold; --Compares total add-ons sold against total drinks sold
SELECT e.first_name, e.last_name, COUNT(o.order_id) AS total_orders_handled FROM employees e JOIN orders o ON e.employee_id = o.employee_id GROUP BY e.first_name, e.last_name ORDER BY total_orders_handled DESC; --Ranks employees by total number of orders