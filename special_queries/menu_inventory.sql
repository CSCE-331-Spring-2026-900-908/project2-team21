-- menu_inventory.sql (Special Query #4: "Menu Item Inventory")
SELECT 
  m.item_name AS menu_item, 
  COUNT(r.inventory_id) AS ingredient_count 
FROM Menu_Items m 
JOIN Recipes r ON m.menu_item_id = r.menu_item_id 
GROUP BY m.item_name 
ORDER BY ingredient_count DESC;