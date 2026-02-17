-- weekly_sales.sql (weekly sales history)
SELECT 
  DATE_TRUNC('week', order_timestamp) AS week_start, 
  COUNT(*) AS order_count 
FROM Orders 
GROUP BY week_start 
ORDER BY week_start;