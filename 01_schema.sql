-- 01_schema.sql
-- Drop in dependency order
DROP TABLE IF EXISTS Line_Item_Add_Ons CASCADE;   
DROP TABLE IF EXISTS Order_Line_Items CASCADE;
DROP TABLE IF EXISTS Orders CASCADE;
DROP TABLE IF EXISTS Recipes CASCADE;            
DROP TABLE IF EXISTS Menu_Items CASCADE;
DROP TABLE IF EXISTS Inventory CASCADE;
DROP TABLE IF EXISTS Employees CASCADE;
CREATE TABLE Employees (
  employee_id SERIAL PRIMARY KEY,
  first_name  VARCHAR(50) NOT NULL,
  last_name   VARCHAR(50) NOT NULL,
  role        VARCHAR(20) NOT NULL CHECK (role IN ('Cashier', 'Manager'))
);

CREATE TABLE Inventory (
  inventory_id      SERIAL PRIMARY KEY,
  item_name         VARCHAR(100) NOT NULL UNIQUE,
  quantity_in_stock NUMERIC(10,2) NOT NULL CHECK (quantity_in_stock >= 0),
  reorder_level     NUMERIC(10,2) NOT NULL CHECK (reorder_level >= 0)
);

CREATE TABLE Menu_Items (
  menu_item_id SERIAL PRIMARY KEY,
  item_name    VARCHAR(100) NOT NULL UNIQUE,
  base_price   NUMERIC(6,2) NOT NULL CHECK (base_price >= 0),
  item_type    VARCHAR(20) NOT NULL DEFAULT 'Drink'
    CHECK (item_type IN ('Drink','Addon'))
);


CREATE TABLE Recipes (
  menu_item_id    INTEGER NOT NULL REFERENCES Menu_Items(menu_item_id) ON DELETE CASCADE,
  inventory_id    INTEGER NOT NULL REFERENCES Inventory(inventory_id),
  quantity_used   NUMERIC(10,2) NOT NULL CHECK (quantity_used > 0),
  PRIMARY KEY (menu_item_id, inventory_id)
);

CREATE TABLE Orders (
  order_id        SERIAL PRIMARY KEY,
  employee_id     INTEGER NOT NULL REFERENCES Employees(employee_id),
  order_timestamp TIMESTAMP NOT NULL,
  total_amount    NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0)
);

CREATE TABLE Order_Line_Items (
  line_item_id SERIAL PRIMARY KEY,
  order_id     INTEGER NOT NULL REFERENCES Orders(order_id) ON DELETE CASCADE,
  menu_item_id INTEGER NOT NULL REFERENCES Menu_Items(menu_item_id),
  quantity     INTEGER NOT NULL CHECK (quantity > 0),
  sale_price   NUMERIC(6,2) NOT NULL
);

CREATE TABLE Line_Item_Add_Ons (
  line_item_id INTEGER NOT NULL REFERENCES Order_Line_Items(line_item_id) ON DELETE CASCADE,
  add_on_menu_item_id INTEGER NOT NULL REFERENCES Menu_Items(menu_item_id),
  quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
  PRIMARY KEY (line_item_id, add_on_menu_item_id)
);

-- Helpful indexes for reporting
CREATE INDEX idx_orders_timestamp ON Orders(order_timestamp);
CREATE INDEX idx_orders_employee  ON Orders(employee_id);
CREATE INDEX idx_oli_order        ON Order_Line_Items(order_id);
CREATE INDEX idx_oli_menu_item    ON Order_Line_Items(menu_item_id);
CREATE INDEX idx_recipes_menu_item ON Recipes(menu_item_id);
CREATE INDEX idx_lia_line_item ON Line_Item_Add_Ons(line_item_id);
CREATE INDEX idx_lia_addon     ON Line_Item_Add_Ons(add_on_menu_item_id);
