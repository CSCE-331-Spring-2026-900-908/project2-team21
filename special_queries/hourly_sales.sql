-- hourly_sales.sql (Special Query #3: "Realistic Sales History")
SELECT 
  DATE_TRUNC('hour', order_timestamp) AS hour_start, 
  COUNT(*) AS order_count, 
  SUM(total_amount) AS total_sales 
FROM Orders 
GROUP BY hour_start 
ORDER BY hour_start;