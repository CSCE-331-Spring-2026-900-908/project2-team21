-- peak_days.sql (Special Query #2: "Peak Sales Day")
SELECT 
  DATE_TRUNC('day', order_timestamp) AS sale_day, 
  SUM(total_amount) AS daily_total 
FROM Orders 
GROUP BY sale_day 
ORDER BY daily_total DESC 
LIMIT 10;