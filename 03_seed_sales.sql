-- 03_seed_sales.sql
-- Generates ~52 weeks of Orders + Order_Line_Items (+ Add-ons) and updates totals.
-- Assumes:
--   - Menu_Items has item_type in ('Drink','Addon')
--   - Line_Item_Add_Ons table exists
--   - Add-ons are stored in Menu_Items with item_type='Addon'

DO $$
DECLARE
  start_date date := (current_date - interval '364 days')::date;  -- ~52 weeks
  end_date   date := current_date;

  d          date;
  peak1      date := (start_date + interval '30 days')::date;
  peak2      date := (start_date + interval '150 days')::date;
  peak3      date := (start_date + interval '250 days')::date;

  orders_today  int;
  i             int;

  new_order_id  int;
  line_count    int;
  j             int;

  drink_id      int;
  new_line_id   int;

  addon_count   int;
  k             int;
  addon_id      int;
  addon_qty     int;
BEGIN
  d := start_date;

  WHILE d <= end_date LOOP
    -- Daily volume pattern
    orders_today :=
      CASE
        WHEN d IN (peak1, peak2, peak3) THEN 450
        WHEN EXTRACT(DOW FROM d) IN (5,6) THEN 220
        WHEN EXTRACT(DOW FROM d) = 0 THEN 180
        ELSE 140
      END;

    FOR i IN 1..orders_today LOOP
      INSERT INTO Orders(employee_id, order_timestamp, total_amount)
      VALUES (
        (SELECT employee_id FROM Employees ORDER BY random() LIMIT 1),
        (d::timestamp
          + make_interval(hours => (10 + floor(random()*12))::int)  -- 10am-10pm-ish
          + make_interval(mins  => floor(random()*60)::int)
          + make_interval(secs  => floor(random()*60)::int)
        ),
        0
      )
      RETURNING order_id INTO new_order_id;

      -- 1 to 4 line items per order
      line_count := 1 + floor(random()*4)::int;

      FOR j IN 1..line_count LOOP
        -- Pick a random DRINK (not an add-on)
        SELECT menu_item_id
        INTO drink_id
        FROM Menu_Items
        WHERE item_type = 'Drink'
        ORDER BY random()
        LIMIT 1;

        INSERT INTO Order_Line_Items(order_id, menu_item_id, quantity)
        VALUES (
          new_order_id,
          drink_id,
          1 + floor(random()*3)::int  -- quantity 1..3
        )
        RETURNING line_item_id INTO new_line_id;

        -- Add-ons: ~45% chance a drink gets add-ons
        IF random() < 0.45 THEN
          -- 1 or 2 add-ons (weighted toward 1)
          addon_count := CASE WHEN random() < 0.75 THEN 1 ELSE 2 END;

          FOR k IN 1..addon_count LOOP
            -- Pick a random ADDON
            SELECT menu_item_id
            INTO addon_id
            FROM Menu_Items
            WHERE item_type = 'Addon'
            ORDER BY random()
            LIMIT 1;

            -- Most add-ons qty=1, sometimes qty=2
            addon_qty := CASE WHEN random() < 0.85 THEN 1 ELSE 2 END;

            -- Avoid duplicates per line item: if already exists, bump quantity instead
            INSERT INTO Line_Item_Add_Ons(line_item_id, add_on_menu_item_id, quantity)
            VALUES (new_line_id, addon_id, addon_qty)
            ON CONFLICT (line_item_id, add_on_menu_item_id)
            DO UPDATE SET quantity = Line_Item_Add_Ons.quantity + EXCLUDED.quantity;
          END LOOP;
        END IF;

      END LOOP;

    END LOOP;

    d := d + 1;
  END LOOP;

  -- Update totals from line items + add-ons
  -- total = SUM( drink_price * drink_qty + SUM(addon_price * addon_qty_per_drink * drink_qty) )
  UPDATE Orders o
  SET total_amount = t.sum_amount
  FROM (
    SELECT
      oli.order_id,
      ROUND(
        SUM(mi.base_price * oli.quantity)
        + COALESCE(SUM(ma.base_price * lia.quantity * oli.quantity), 0),
        2
      ) AS sum_amount
    FROM Order_Line_Items oli
    JOIN Menu_Items mi
      ON mi.menu_item_id = oli.menu_item_id
    LEFT JOIN Line_Item_Add_Ons lia
      ON lia.line_item_id = oli.line_item_id
    LEFT JOIN Menu_Items ma
      ON ma.menu_item_id = lia.add_on_menu_item_id
    GROUP BY oli.order_id
  ) t
  WHERE o.order_id = t.order_id;

END $$;
