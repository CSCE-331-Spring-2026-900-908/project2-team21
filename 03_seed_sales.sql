-- 03_seed_sales.sql
-- Generates ~52 weeks of Orders + Order_Line_Items and updates totals.

DO $$
DECLARE
  start_date date := (current_date - interval '364 days')::date;  -- 52 weeks
  end_date   date := current_date;

  d          date;
  peak1      date := (start_date + interval '30 days')::date;
  peak2      date := (start_date + interval '150 days')::date;
  peak3      date := (start_date + interval '250 days')::date;

  orders_today int;
  i int;

  new_order_id int;
  line_count int;
BEGIN
  d := start_date;

  WHILE d <= end_date LOOP
    -- Base daily volume (tuned to land near ~$1M depending on randomness)
    orders_today :=
      CASE
        WHEN d IN (peak1, peak2, peak3) THEN 450              -- peak days (φ=3)
        WHEN EXTRACT(DOW FROM d) IN (5,6) THEN 220            -- Fri/Sat
        WHEN EXTRACT(DOW FROM d) = 0 THEN 180                 -- Sun
        ELSE 140                                              -- Mon-Thu
      END;

    -- Create orders for that day
    FOR i IN 1..orders_today LOOP
      INSERT INTO Orders(employee_id, order_timestamp, total_amount)
      VALUES (
        (SELECT employee_id FROM Employees ORDER BY random() LIMIT 1),
        (d::timestamp
          + make_interval(hours => (10 + floor(random()*12))::int)      -- 10am-10pm-ish
          + make_interval(mins  => floor(random()*60)::int)
          + make_interval(secs  => floor(random()*60)::int)
        ),
        0
      )
      RETURNING order_id INTO new_order_id;

      -- 1 to 4 line items per order
      line_count := 1 + floor(random()*4)::int;

      INSERT INTO Order_Line_Items(order_id, menu_item_id, quantity)
      SELECT
        new_order_id,
        mi.menu_item_id,
        1 + floor(random()*3)::int  -- quantity 1..3
      FROM (
        SELECT menu_item_id
        FROM Menu_Items
        ORDER BY random()
        LIMIT line_count
      ) mi;
    END LOOP;

    d := d + 1;
  END LOOP;

  -- Update totals from line items (base_price * quantity)
  UPDATE Orders o
  SET total_amount = t.sum_amount
  FROM (
    SELECT oli.order_id, ROUND(SUM(mi.base_price * oli.quantity), 2) AS sum_amount
    FROM Order_Line_Items oli
    JOIN Menu_Items mi ON mi.menu_item_id = oli.menu_item_id
    GROUP BY oli.order_id
  ) t
  WHERE o.order_id = t.order_id;

END $$;
