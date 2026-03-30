require("dotenv").config();
const express = require("express");
const path = require("path");
const { Pool } = require("pg");

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, "public")));

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.PGSSL === "true" ? { rejectUnauthorized: false } : false
});

// ---------- ROUTES (Portal + interfaces) ----------
app.get("/", (_, res) => res.sendFile(path.join(__dirname, "public", "portal.html")));
app.get("/cashier", (_, res) => res.sendFile(path.join(__dirname, "public", "cashier.html")));
app.get("/manager", (_, res) => res.sendFile(path.join(__dirname, "public", "manager.html")));
app.get("/kiosk", (_, res) => res.sendFile(path.join(__dirname, "public", "kiosk.html")));
app.get("/menuboard", (_, res) => res.sendFile(path.join(__dirname, "public", "menuboard.html")));

// ---------- MVP API: create a simple order ----------
app.post("/api/orders", async (req, res) => {
  // Keep it simple for MVP: insert one order + 1 line item
  const { employeeId, menuItemId, quantity } = req.body;

  try {
    const client = await pool.connect();
    try {
      await client.query("BEGIN");

      const order = await client.query(
        `INSERT INTO Orders(employee_id, order_timestamp, total_amount)
         VALUES ($1, NOW(), 0) RETURNING order_id`,
        [employeeId]
      );

      // Use current menu base_price as sale_price for MVP
      const priceRow = await client.query(
        `SELECT base_price FROM Menu_Items WHERE menu_item_id=$1`,
        [menuItemId]
      );

      const salePrice = priceRow.rows[0]?.base_price ?? 0;

      await client.query(
        `INSERT INTO Order_Line_Items(order_id, menu_item_id, quantity, sale_price)
         VALUES ($1,$2,$3,$4)`,
        [order.rows[0].order_id, menuItemId, quantity, salePrice]
      );

      // update total
      await client.query(
        `UPDATE Orders
         SET total_amount = (
           SELECT SUM(quantity * sale_price) FROM Order_Line_Items WHERE order_id=$1
         )
         WHERE order_id=$1`,
        [order.rows[0].order_id]
      );

      await client.query("COMMIT");
      res.json({ ok: true, orderId: order.rows[0].order_id });
    } catch (e) {
      await client.query("ROLLBACK");
      res.status(500).json({ ok: false, error: e.message });
    } finally {
      client.release();
    }
  } catch (e) {
    res.status(500).json({ ok: false, error: e.message });
  }
});

// ---------- MVP API: show latest orders (demo evidence) ----------
app.get("/api/orders/recent", async (_, res) => {
  try {
    const result = await pool.query(
      `SELECT order_id, order_timestamp, total_amount
       FROM Orders
       ORDER BY order_timestamp DESC
       LIMIT 10`
    );
    res.json(result.rows);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Server running on port ${port}`));