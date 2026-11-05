#!/usr/bin/env python3
"""Generates src/main/resources/db/demo/V6_1__demo_data.sql (deterministic, Vietnamese sample data).

Dates are computed in SQL relative to the migration time, so the dashboard always has
"today" and "last 14 days" data. Run again after changing the lists below:
    python3 scripts/generate-demo-data.py
"""
import random
from decimal import Decimal
from pathlib import Path

random.seed(2026)
OUT = Path(__file__).resolve().parent.parent / "src/main/resources/db/demo/V6_1__demo_data.sql"

USERS = [  # id, email, name, phone, role, bcrypt(password)
    (9001, "admin@tiempro.vn", "Nguyễn Thị Lan", "0903123456", "ADMIN",
     "$2a$10$0HJk.Y/094Zb.fshRsEAwumkZTNk5r/T4MaNt5DznDzy4or0lJi/C"),   # Admin@123
    (9002, "quanly@tiempro.vn", "Trần Văn Minh", "0912345678", "MANAGER",
     "$2a$10$3ms0VSYBLrqU5CMKL9XuuuzhkBeGEr/66oqwBgtuwwbOHfI5qqDJe"),   # Manager@123
    (9003, "nhanvien@tiempro.vn", "Lê Thị Hoa", "0987654321", "STAFF",
     "$2a$10$Ik89vb0m8Nu0uVfOivZhcu0DMGLsin.Duj5Zhl5lsTeCojz8MmpWi"),   # Staff@123
    (9004, "nhanvien2@tiempro.vn", "Phạm Quốc Bảo", "0978111222", "STAFF",
     "$2a$10$Ik89vb0m8Nu0uVfOivZhcu0DMGLsin.Duj5Zhl5lsTeCojz8MmpWi"),   # Staff@123
]
SELLERS = [9002, 9003, 9003, 9004, 9004, 9001]

CATEGORIES = [
    (1, "Đồ uống", "Nước ngọt, nước suối, trà, cà phê"),
    (2, "Bánh kẹo & Snack", "Bánh quy, kẹo, snack"),
    (3, "Mì gói & Đồ khô", "Mì, phở, bún khô, gạo"),
    (4, "Gia vị", "Nước mắm, dầu ăn, đường, muối"),
    (5, "Sữa & Chế phẩm", "Sữa tươi, sữa chua, sữa đặc"),
    (6, "Hoá mỹ phẩm", "Dầu gội, sữa tắm, bột giặt"),
    (7, "Văn phòng phẩm", "Bút, vở, giấy in"),
    (8, "Đồ gia dụng", "Đồ dùng nhà bếp, vệ sinh"),
]

# sku, name, category, unit, price, opening stock, min stock
PRODUCTS = [
    ("NU001", "Nước suối Aquafina 500ml", 1, "chai", 5000, 240, 48),
    ("NU002", "Coca-Cola lon 330ml", 1, "lon", 10000, 192, 48),
    ("NU003", "Pepsi lon 330ml", 1, "lon", 10000, 144, 48),
    ("NU004", "Trà xanh Không Độ 455ml", 1, "chai", 10000, 120, 24),
    ("NU005", "Cà phê sữa G7 hộp 18 gói", 1, "hộp", 52000, 30, 10),
    ("NU006", "Nước tăng lực Red Bull 250ml", 1, "lon", 12000, 96, 24),
    ("NU007", "Trà Ô Long Tea+ 450ml", 1, "chai", 11000, 30, 24),
    ("BK001", "Bánh Chocopie hộp 12 cái", 2, "hộp", 58000, 40, 10),
    ("BK002", "Snack khoai tây Lay's 52g", 2, "gói", 12000, 120, 30),
    ("BK003", "Bánh quy Cosy Marie 432g", 2, "gói", 39000, 36, 10),
    ("BK004", "Kẹo Alpenliebe gói 120g", 2, "gói", 18000, 60, 12),
    ("BK005", "Bánh gạo One One 150g", 2, "gói", 25000, 22, 10),
    ("MG001", "Mì Hảo Hảo tôm chua cay", 3, "gói", 4500, 300, 60),
    ("MG002", "Mì Omachi sườn hầm", 3, "gói", 8000, 150, 40),
    ("MG003", "Phở Vifon bò gói 65g", 3, "gói", 9000, 100, 30),
    ("MG004", "Gạo ST25 túi 5kg", 3, "túi", 185000, 25, 5),
    ("MG005", "Bún khô Safoco 500g", 3, "gói", 22000, 18, 10),
    ("GV001", "Nước mắm Nam Ngư 500ml", 4, "chai", 38000, 48, 12),
    ("GV002", "Dầu ăn Neptune 1L", 4, "chai", 62000, 36, 10),
    ("GV003", "Đường Biên Hoà 1kg", 4, "gói", 28000, 40, 10),
    ("GV004", "Hạt nêm Knorr 400g", 4, "gói", 42000, 30, 8),
    ("GV005", "Tương ớt Chinsu 250g", 4, "chai", 16000, 12, 10),
    ("SU001", "Sữa tươi Vinamilk 180ml (lốc 4)", 5, "lốc", 32000, 80, 20),
    ("SU002", "Sữa chua Vinamilk có đường (lốc 4)", 5, "lốc", 28000, 60, 15),
    ("SU003", "Sữa đặc Ông Thọ 380g", 5, "lon", 26000, 48, 12),
    ("SU004", "Sữa TH True Milk 1L", 5, "hộp", 38000, 14, 8),
    ("HM001", "Dầu gội Clear 340g", 6, "chai", 115000, 20, 5),
    ("HM002", "Sữa tắm Lifebuoy 850g", 6, "chai", 145000, 15, 5),
    ("HM003", "Bột giặt OMO 3kg", 6, "túi", 175000, 18, 5),
    ("HM004", "Nước rửa chén Sunlight 750g", 6, "chai", 32000, 40, 10),
    ("HM005", "Kem đánh răng P/S 230g", 6, "tuýp", 35000, 9, 6),
    ("VP001", "Bút bi Thiên Long TL-027", 7, "cây", 5000, 200, 40),
    ("VP002", "Vở Campus 200 trang", 7, "quyển", 22000, 80, 20),
    ("VP003", "Giấy in Double A A4 70gsm", 7, "ram", 85000, 20, 5),
    ("VP004", "Băng keo trong 5cm", 7, "cuộn", 15000, 30, 10),
    ("GD001", "Khăn giấy Pulppy 2 lớp (gói 10)", 8, "gói", 45000, 30, 8),
    ("GD002", "Túi rác đen tự huỷ (cuộn)", 8, "cuộn", 25000, 40, 10),
    ("GD003", "Bật lửa Clipper", 8, "cái", 15000, 50, 10),
    ("GD004", "Pin AA Panasonic (vỉ 2)", 8, "vỉ", 18000, 7, 6),
    ("GD005", "Hộp nhựa Lock&Lock 1L", 8, "cái", 89000, 12, 3),
]
INACTIVE = {"BK005"}  # one discontinued product for the demo

CUSTOMERS = [
    ("Nguyễn Văn An", "0901234567", "an.nguyen@gmail.com", "12 Lê Lợi, Q.1, TP.HCM"),
    ("Trần Thị Bích", "0912223344", None, "45 Nguyễn Trãi, Q.5, TP.HCM"),
    ("Lê Hoàng Cường", "0933445566", "cuong.le@yahoo.com", "88 Cách Mạng Tháng 8, Q.3, TP.HCM"),
    ("Phạm Thu Dung", "0944556677", None, "23 Võ Văn Tần, Q.3, TP.HCM"),
    ("Hoàng Minh Đức", "0955667788", "duc.hoang@gmail.com", "156 Điện Biên Phủ, Bình Thạnh"),
    ("Vũ Thị Hạnh", "0966778899", None, "9 Phan Xích Long, Phú Nhuận"),
    ("Đặng Quốc Huy", "0977889900", None, "301 Lý Thường Kiệt, Q.10"),
    ("Bùi Thị Kim", "0988990011", "kim.bui@gmail.com", "17 Hoàng Văn Thụ, Tân Bình"),
    ("Đỗ Văn Long", "0399112233", None, "64 Quang Trung, Gò Vấp"),
    ("Ngô Thị Mai", "0388223344", None, "5 Nguyễn Thị Minh Khai, Q.1"),
    ("Dương Văn Nam", "0377334455", "nam.duong@outlook.com", "112 Trường Chinh, Tân Phú"),
    ("Lý Thị Oanh", "0366445566", None, "27 Âu Cơ, Tân Bình"),
    ("Trịnh Công Phúc", "0355556677", None, "78 Lạc Long Quân, Q.11"),
    ("Mai Thị Quỳnh", "0344667788", "quynh.mai@gmail.com", "3 Hai Bà Trưng, Q.1"),
    ("Công ty TNHH Ánh Dương", "0283822334", "ketoan@anhduong.vn", "200 Pasteur, Q.3 (khách sỉ VPP)"),
]


def q(s):
    return "NULL" if s is None else "'" + s.replace("\\", "\\\\").replace("'", "''") + "'"


def money(v):
    return f"{Decimal(v):.2f}"


out = []
w = out.append
w("-- GENERATED by scripts/generate-demo-data.py - do not edit by hand.")
w("-- Vietnamese demo data for TiemPro. Installed only when app.demo-data=true, on an EMPTY database.")
w("-- Demo accounts: admin@tiempro.vn / Admin@123, quanly@tiempro.vn / Manager@123,")
w("--                nhanvien@tiempro.vn / Staff@123, nhanvien2@tiempro.vn / Staff@123")
w("")
w("SET @today = DATE(UTC_TIMESTAMP() + INTERVAL 7 HOUR); -- business day in Vietnam time")
w("SET @now = UTC_TIMESTAMP(6);")
w("")

# Users
w("INSERT INTO users (id, email, password, full_name, phone, enabled, token_version, created_at, updated_at, created_by, updated_by) VALUES")
w(",\n".join(f"({u[0]}, {q(u[1])}, {q(u[5])}, {q(u[2])}, {q(u[3])}, 1, 0, @now - INTERVAL 60 DAY, @now - INTERVAL 60 DAY, 'system', 'system')" for u in USERS) + ";")
for u in USERS:
    w(f"INSERT INTO user_roles (user_id, role_id) SELECT {u[0]}, id FROM roles WHERE code = '{u[4]}';")
w("")

# Categories
w("INSERT INTO categories (id, name, description, created_at, updated_at, created_by, updated_by) VALUES")
w(",\n".join(f"({c[0]}, {q(c[1])}, {q(c[2])}, @now - INTERVAL 60 DAY, @now - INTERVAL 60 DAY, 'quanly@tiempro.vn', 'quanly@tiempro.vn')" for c in CATEGORIES) + ";")
w("")

# Products: cost ~ 72-85% of price
products = []
for i, (sku, name, cat, unit, price, opening, min_stock) in enumerate(PRODUCTS, start=1):
    cost = (Decimal(price) * Decimal(random.randint(72, 85)) / 100).quantize(Decimal("100"))
    products.append(dict(id=i, sku=sku, name=name, cat=cat, unit=unit, price=Decimal(price), cost=cost,
                         stock=opening, opening=opening, min=min_stock, active=sku not in INACTIVE))

# Customers
w("INSERT INTO customers (id, name, phone, email, address, created_at, updated_at, created_by, updated_by) VALUES")
w(",\n".join(f"({i}, {q(c[0])}, {q(c[1])}, {q(c[2])}, {q(c[3])}, @now - INTERVAL {random.randint(31, 59)} DAY, @now - INTERVAL 30 DAY, 'nhanvien@tiempro.vn', 'nhanvien@tiempro.vn')"
             for i, c in enumerate(CUSTOMERS, start=1)) + ";")
w("")

PRODUCTS_PLACEHOLDER = len(out)
w("-- products placeholder")
w("")

# Opening stock receipt 32 days ago
movements = []  # (product_id, type, change, balance, ref, note, created_expr, by)
receipt_items = []
total = Decimal(0)
for p in products:
    line = p["cost"] * p["opening"]
    total += line
    receipt_items.append((p["id"], p["opening"], p["cost"], line))
receipt_time = "TIMESTAMP(@today - INTERVAL 32 DAY, '08:30:00') - INTERVAL 7 HOUR"
w("INSERT INTO stock_receipts (id, code, supplier_name, note, total_amount, created_at, updated_at, created_by, updated_by) VALUES")
w(f"(1, 'PN-DEMO-0001', 'Nhà phân phối Thành Công', 'Nhập hàng đầu kỳ', {money(total)}, {receipt_time}, {receipt_time}, 'quanly@tiempro.vn', 'quanly@tiempro.vn');")
w("INSERT INTO stock_receipt_items (receipt_id, product_id, quantity, unit_cost, line_total, created_at, updated_at, created_by, updated_by) VALUES")
w(",\n".join(f"(1, {pid}, {qty}, {money(cost)}, {money(line)}, {receipt_time}, {receipt_time}, 'quanly@tiempro.vn', 'quanly@tiempro.vn')"
             for pid, qty, cost, line in receipt_items) + ";")
for p in products:
    movements.append((p["id"], "RECEIPT", p["opening"], p["opening"], "PN-DEMO-0001", "Nhà phân phối Thành Công", receipt_time, "quanly@tiempro.vn"))
w("")

# Orders over the last 30 days (Vietnam business hours), plus a few "today" orders before now
orders = []
order_id = 0
weights = [p["opening"] for p in products]
for days_ago in range(30, -1, -1):
    n = random.randint(6, 14) if days_ago > 0 else random.randint(4, 7)
    times = sorted(random.sample(range(7 * 60, 21 * 60), n)) if days_ago > 0 else sorted(random.sample(range(5, 400), n), reverse=True)
    for t in times:
        if days_ago > 0:
            created = f"TIMESTAMP(@today - INTERVAL {days_ago} DAY, '{t // 60:02d}:{t % 60:02d}:{random.randint(0, 59):02d}') - INTERVAL 7 HOUR"
        else:
            created = f"@now - INTERVAL {t} MINUTE"
        lines = {}
        for _ in range(random.choice([1, 1, 2, 2, 3, 4])):
            p = random.choices(products, weights=weights)[0]
            if not p["active"] and days_ago < 20:
                continue
            qty = random.choice([1, 1, 1, 2, 2, 3])
            if p["stock"] - lines.get(p["id"], 0) >= qty + 2:
                lines[p["id"]] = lines.get(p["id"], 0) + qty
        if not lines:
            continue
        order_id += 1
        subtotal = sum(products[pid - 1]["price"] * qty for pid, qty in lines.items())
        cost = sum(products[pid - 1]["cost"] * qty for pid, qty in lines.items())
        discount = Decimal(0)
        if subtotal >= 200000 and random.random() < 0.3:
            discount = Decimal(random.choice([5000, 10000, 20000]))
        payment = random.choices(["CASH", "TRANSFER", "CARD"], weights=[6, 3, 1])[0]
        total_amount = subtotal - discount
        cash = None
        if payment == "CASH":
            cash = ((total_amount // 50000) + 1) * 50000 if random.random() < 0.6 else total_amount
        customer = random.randint(1, len(CUSTOMERS)) if random.random() < 0.45 else None
        seller = random.choice(SELLERS)
        seller_email = next(u[1] for u in USERS if u[0] == seller)
        cancelled = days_ago > 0 and random.random() < 0.03
        for pid, qty in lines.items():
            p = products[pid - 1]
            p["stock"] -= qty
            movements.append((pid, "SALE", -qty, p["stock"], f"@ORDER{order_id}", None, created, seller_email))
        if cancelled:
            for pid, qty in lines.items():
                p = products[pid - 1]
                p["stock"] += qty
                movements.append((pid, "SALE_CANCEL", qty, p["stock"], f"@ORDER{order_id}", "Khách đổi ý", created + " + INTERVAL 10 MINUTE", "quanly@tiempro.vn"))
        orders.append(dict(id=order_id, created=created, customer=customer, seller=seller, seller_email=seller_email,
                           payment=payment, subtotal=subtotal, discount=discount, total=total_amount, cost=cost,
                           cash=cash, cancelled=cancelled, lines=lines))

w("-- Orders get a temporary code, renamed below to DHyyMMdd-nnnn by business day")
w("INSERT INTO orders (id, code, customer_id, seller_id, status, payment_method, subtotal, discount, total, total_cost, cash_received, note, cancelled_at, cancelled_by, cancel_reason, created_at, updated_at, created_by, updated_by) VALUES")
rows = []
for o in orders:
    status = "CANCELLED" if o["cancelled"] else "COMPLETED"
    cancel_at = f"{o['created']} + INTERVAL 10 MINUTE" if o["cancelled"] else "NULL"
    cancel_by = "'quanly@tiempro.vn'" if o["cancelled"] else "NULL"
    cancel_reason = "'Khách đổi ý'" if o["cancelled"] else "NULL"
    rows.append(f"({o['id']}, 'TMP{o['id']}', {o['customer'] or 'NULL'}, {o['seller']}, '{status}', '{o['payment']}', "
                f"{money(o['subtotal'])}, {money(o['discount'])}, {money(o['total'])}, {money(o['cost'])}, "
                f"{money(o['cash']) if o['cash'] is not None else 'NULL'}, NULL, {cancel_at}, {cancel_by}, {cancel_reason}, "
                f"{o['created']}, {o['created']}, {q(o['seller_email'])}, {q(o['seller_email'])})")
w(",\n".join(rows) + ";")
w("")
w("INSERT INTO order_items (order_id, product_id, product_name, sku, unit, unit_price, unit_cost, quantity, line_total, created_at, updated_at, created_by, updated_by) VALUES")
rows = []
for o in orders:
    for pid, qty in o["lines"].items():
        p = products[pid - 1]
        rows.append(f"({o['id']}, {pid}, {q(p['name'])}, {q(p['sku'])}, {q(p['unit'])}, {money(p['price'])}, {money(p['cost'])}, "
                    f"{qty}, {money(p['price'] * qty)}, {o['created']}, {o['created']}, {q(o['seller_email'])}, {q(o['seller_email'])})")
w(",\n".join(rows) + ";")
w("")

# Products with final stock (written before receipts/orders because of foreign keys)
product_sql = ("INSERT INTO products (id, sku, name, description, category_id, unit, price, cost_price, quantity, min_stock, image_url, status, created_at, updated_at, created_by, updated_by) VALUES\n"
               + ",\n".join(f"({p['id']}, {q(p['sku'])}, {q(p['name'])}, NULL, {p['cat']}, {q(p['unit'])}, {money(p['price'])}, {money(p['cost'])}, "
             f"{p['stock']}, {p['min']}, NULL, '{'ACTIVE' if p['active'] else 'INACTIVE'}', @now - INTERVAL 40 DAY, @now - INTERVAL 40 DAY, 'quanly@tiempro.vn', 'quanly@tiempro.vn')"
             for p in products) + ";")
out[PRODUCTS_PLACEHOLDER] = product_sql

w("INSERT INTO stock_movements (product_id, type, quantity_change, balance_after, reference_code, note, created_at, updated_at, created_by, updated_by) VALUES")
rows = []
for pid, typ, change, bal, ref, note, created, by in movements:
    ref_sql = q(ref) if not ref.startswith("@ORDER") else f"'TMP{ref[6:]}'"
    rows.append(f"({pid}, '{typ}', {change}, {bal}, {ref_sql}, {q(note)}, {created}, {created}, {q(by)}, {q(by)})")
w(",\n".join(rows) + ";")
w("")

w("""-- Real document codes: DH + yyMMdd (Vietnam day) + daily sequence
CREATE TEMPORARY TABLE tmp_order_codes AS
SELECT id,
       CONCAT('DH', DATE_FORMAT(created_at + INTERVAL 7 HOUR, '%y%m%d'), '-',
              LPAD(ROW_NUMBER() OVER (PARTITION BY DATE(created_at + INTERVAL 7 HOUR) ORDER BY created_at, id), 4, '0')) AS code
FROM orders;
UPDATE stock_movements m JOIN tmp_order_codes t ON m.reference_code = CONCAT('TMP', t.id) SET m.reference_code = t.code;
UPDATE orders o JOIN tmp_order_codes t ON o.id = t.id SET o.code = t.code;
DROP TEMPORARY TABLE tmp_order_codes;

INSERT INTO code_sequences (seq_key, value)
SELECT SUBSTRING(code, 1, 8), MAX(CAST(SUBSTRING(code, 10) AS UNSIGNED)) FROM orders GROUP BY SUBSTRING(code, 1, 8);
""")
OUT.write_text("\n".join(out) + "\n", encoding="utf-8")
low = [p["sku"] for p in products if p["stock"] <= p["min"]]
print(f"wrote {OUT} - {len(orders)} orders, {len(movements)} movements, low stock: {low}")
