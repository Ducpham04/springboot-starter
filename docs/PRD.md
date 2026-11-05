# PRD – TiệmPro: Quản lý bán hàng & kho cho cửa hàng nhỏ

> Phiên bản: MVP 1.0 · Người viết: Product Owner / BA · Ngày: 2026-11-05

## 1. Bối cảnh & vấn đề

Phần lớn hộ kinh doanh bán lẻ ở Việt Nam (tạp hoá, văn phòng phẩm, cửa hàng tiện lợi, shop mỹ phẩm…) vẫn ghi chép bằng sổ tay hoặc Excel:

- Không biết chính xác **tồn kho**, hàng hết mới biết, hàng tồn lâu không ai để ý.
- Không biết **doanh thu / lãi** theo ngày, theo mặt hàng.
- Nhân viên bán hàng tính tiền thủ công, dễ sai; chủ khó kiểm soát khi vắng mặt.
- Các phần mềm lớn (KiotViet, Sapo…) đầy đủ nhưng phí thuê bao hằng tháng, nhiều tính năng thừa.

## 2. Mục tiêu sản phẩm

| # | Mục tiêu | Chỉ số đo (sau 3 tháng dùng) |
|---|---|---|
| G1 | Bán hàng nhanh, chính xác tại quầy | Tạo 1 đơn ≤ 30 giây; 0 lỗi tính tiền |
| G2 | Tồn kho luôn đúng, có truy vết | Chênh lệch kiểm kho < 1%; 100% thay đổi tồn có chứng từ |
| G3 | Chủ cửa hàng nắm tình hình mọi lúc, kể cả trên điện thoại | Xem doanh thu hôm nay trong ≤ 2 thao tác |
| G4 | Phân quyền rõ ràng | Nhân viên không xem được giá vốn/báo cáo, không huỷ được đơn |

## 3. Persona

| Persona | Mô tả | Nhu cầu chính | Thiết bị |
|---|---|---|---|
| **Chị Lan – Chủ cửa hàng (ADMIN)** | 38 tuổi, chủ 1 tạp hoá lớn, không rành công nghệ | Xem doanh thu, lãi, hàng sắp hết; quản lý tài khoản nhân viên | Điện thoại là chính |
| **Anh Minh – Quản lý (MANAGER)** | 30 tuổi, phụ trách nhập hàng, sắp xếp kho | Nhập hàng nhanh, cập nhật giá, kiểm kho, huỷ đơn sai | Laptop + điện thoại |
| **Hoa – Nhân viên bán hàng (STAFF)** | 22 tuổi, đứng quầy | Tìm hàng nhanh, tính tiền đúng, lưu khách quen | Máy tính quầy / tablet |

## 4. Phạm vi

### 4.1 MVP (bản này)
1. Đăng nhập, đăng xuất, quên/đặt lại mật khẩu, đổi mật khẩu.
2. Quản lý người dùng & phân quyền theo vai trò (ADMIN).
3. Danh mục hàng hoá (nhóm hàng).
4. Sản phẩm: mã SKU, tên, nhóm, đơn vị tính, giá bán, giá vốn, tồn kho, ngưỡng cảnh báo, ảnh, trạng thái.
5. Khách hàng: tên, SĐT, email, địa chỉ, ghi chú; lịch sử mua hàng & tổng chi tiêu.
6. Bán hàng (POS): tìm sản phẩm, giỏ hàng, chọn khách, giảm giá, phương thức thanh toán, tạo đơn → trừ tồn.
7. Đơn hàng: danh sách lọc theo ngày/trạng thái/khách, chi tiết, huỷ đơn (hoàn tồn).
8. Kho: phiếu nhập hàng (cộng tồn, cập nhật giá vốn), kiểm kho (điều chỉnh tồn), lịch sử xuất nhập tồn.
9. Tổng quan (dashboard): doanh thu & lợi nhuận hôm nay/tháng này, số đơn, biểu đồ doanh thu 14 ngày, top sản phẩm bán chạy, hàng sắp hết, đơn gần đây.
10. Giao diện tiếng Việt, mobile-first.
11. Dữ liệu demo + tài khoản demo cho từng vai trò.

### 4.2 Sau MVP
- Danh mục nhà cung cấp, công nợ NCC & khách hàng.
- In hoá đơn / phiếu (máy in nhiệt 58/80mm), quét mã vạch bằng camera.
- Nhiều chi nhánh / chuyển kho.
- Khuyến mãi theo chương trình, tích điểm khách hàng.
- Báo cáo nâng cao, xuất Excel.
- Đơn online, tích hợp thanh toán QR (VietQR), hoá đơn điện tử.
- Ứng dụng mobile / PWA offline.

## 5. User stories & Acceptance criteria

Ký hiệu: **US-xx** · vai trò · mức ưu tiên (P0 = bắt buộc MVP, P1 = nên có trong MVP).

### Xác thực
**US-01 (P0) – Đăng nhập** · Mọi vai trò
*Là người dùng, tôi muốn đăng nhập bằng email + mật khẩu để dùng hệ thống.*
- AC1: Đúng email/mật khẩu → vào trang chủ phù hợp vai trò (STAFF → Bán hàng, còn lại → Tổng quan).
- AC2: Sai → thông báo "Email hoặc mật khẩu không đúng", không tiết lộ email có tồn tại hay không.
- AC3: Tài khoản bị khoá → thông báo "Tài khoản đã bị khóa".
- AC4: Phiên tự gia hạn (refresh token) không bắt đăng nhập lại trong 7 ngày nếu còn dùng.

**US-02 (P0) – Đăng xuất** · Mọi vai trò
- AC1: Sau đăng xuất, token cũ không dùng được nữa (gọi API trả 401).

**US-03 (P1) – Quên mật khẩu** · Mọi vai trò
- AC1: Nhập email → luôn hiện "Nếu email tồn tại, liên kết đã được gửi".
- AC2: Link trong email dùng được 1 lần, hết hạn sau 30 phút.

**US-04 (P1) – Đổi mật khẩu** · Mọi vai trò
- AC1: Phải nhập đúng mật khẩu cũ; mật khẩu mới ≥ 8 ký tự, có chữ và số.
- AC2: Đổi xong → mọi phiên đăng nhập khác bị đăng xuất.

### Người dùng & phân quyền
**US-05 (P0) – Quản lý nhân viên** · ADMIN
*Là chủ cửa hàng, tôi muốn tạo tài khoản cho nhân viên và gán vai trò.*
- AC1: Tạo tài khoản với họ tên, email, SĐT, mật khẩu ban đầu, vai trò (ADMIN/MANAGER/STAFF).
- AC2: Sửa họ tên, SĐT, vai trò; khoá/mở khoá tài khoản. Tài khoản bị khoá không đăng nhập được, phiên đang dùng bị chặn ngay.
- AC3: Không thể tự khoá hoặc tự bỏ quyền ADMIN của chính mình.
- AC4: Email trùng → báo lỗi.

**US-06 (P0) – Phân quyền theo vai trò** · Hệ thống
- AC1: Ma trận quyền (mục 6) được áp dụng ở **cả API và giao diện** (menu/nút ẩn khi không có quyền; gọi API trái phép → 403).

### Hàng hoá
**US-07 (P0) – Nhóm hàng** · ADMIN, MANAGER
- AC1: Thêm/sửa/xoá nhóm hàng; tên không trùng.
- AC2: Không xoá được nhóm đang có sản phẩm.

**US-08 (P0) – Sản phẩm** · ADMIN, MANAGER (STAFF chỉ xem, không thấy giá vốn)
- AC1: Thêm sản phẩm: SKU (duy nhất), tên, nhóm, đơn vị tính, giá bán, giá vốn, tồn ban đầu, ngưỡng cảnh báo, ảnh, trạng thái.
- AC2: Danh sách có tìm kiếm (tên/SKU), lọc theo nhóm, trạng thái, "sắp hết hàng"; phân trang, sắp xếp.
- AC3: Sửa sản phẩm không thay đổi tồn kho.
- AC4: Sản phẩm đã có giao dịch không xoá được → gợi ý "Ngừng kinh doanh".
- AC5: Sản phẩm có tồn ≤ ngưỡng được đánh dấu "Sắp hết".

### Khách hàng
**US-09 (P0) – Khách hàng** · ADMIN, MANAGER, STAFF
- AC1: Thêm/sửa khách: tên, SĐT (duy nhất, đúng định dạng VN), email, địa chỉ, ghi chú.
- AC2: Tìm theo tên/SĐT.
- AC3: Xem chi tiết: tổng số đơn, tổng chi tiêu, danh sách đơn gần đây.
- AC4: Chỉ ADMIN/MANAGER được xoá khách chưa có đơn.

### Bán hàng
**US-10 (P0) – Tạo đơn tại quầy (POS)** · ADMIN, MANAGER, STAFF
*Là nhân viên, tôi muốn chọn hàng vào giỏ và tính tiền nhanh.*
- AC1: Tìm sản phẩm theo tên/SKU, chỉ hiện sản phẩm đang kinh doanh; thêm vào giỏ bằng 1 chạm.
- AC2: Sửa số lượng, xoá dòng; tạm tính, giảm giá (số tiền), tổng thanh toán cập nhật tức thì.
- AC3: Chọn khách (tìm theo SĐT) hoặc để "Khách lẻ"; chọn thanh toán Tiền mặt / Chuyển khoản / Thẻ; nhập tiền khách đưa → hiện tiền thừa (tiền mặt).
- AC4: Không bán vượt tồn: nếu số lượng > tồn → báo lỗi rõ tên sản phẩm, không tạo đơn.
- AC5: Tạo đơn thành công → tồn kho giảm, có mã đơn dạng `DH260105-0001`, giỏ được làm trống.
- AC6: Giảm giá không vượt tạm tính.

**US-11 (P0) – Danh sách & chi tiết đơn** · ADMIN, MANAGER (tất cả đơn), STAFF (đơn do mình tạo)
- AC1: Lọc theo khoảng ngày, trạng thái, từ khoá (mã đơn / tên / SĐT khách).
- AC2: Chi tiết hiển thị dòng hàng, giá tại thời điểm bán, người bán, phương thức thanh toán.

**US-12 (P0) – Huỷ đơn** · ADMIN, MANAGER
- AC1: Bắt buộc nhập lý do; đơn chuyển "Đã huỷ", tồn kho được hoàn lại, ghi lịch sử kho.
- AC2: Không huỷ được đơn đã huỷ.

### Kho
**US-13 (P0) – Nhập hàng** · ADMIN, MANAGER
- AC1: Tạo phiếu nhập: nhà cung cấp (text), ghi chú, nhiều dòng (sản phẩm, số lượng > 0, giá nhập ≥ 0).
- AC2: Lưu phiếu → tồn tăng, giá vốn sản phẩm cập nhật theo **bình quân gia quyền**, mã phiếu `PN260105-0001`.
- AC3: Danh sách phiếu nhập, xem chi tiết.

**US-14 (P1) – Kiểm kho** · ADMIN, MANAGER
- AC1: Nhập số lượng thực tế + lý do → hệ thống ghi chênh lệch và cập nhật tồn.

**US-15 (P1) – Lịch sử xuất nhập tồn** · ADMIN, MANAGER
- AC1: Liệt kê mọi biến động tồn: thời gian, sản phẩm, loại (Nhập / Bán / Huỷ đơn / Kiểm kho), +/- số lượng, tồn sau, chứng từ, người thực hiện; lọc theo sản phẩm, loại.

### Báo cáo
**US-16 (P0) – Tổng quan** · ADMIN, MANAGER
- AC1: Thẻ chỉ số: doanh thu hôm nay, số đơn hôm nay, doanh thu tháng này, lợi nhuận gộp tháng này, số sản phẩm sắp hết.
- AC2: Biểu đồ doanh thu 14 ngày gần nhất.
- AC3: Top 5 sản phẩm bán chạy tháng này; danh sách sắp hết hàng; 5 đơn gần đây.
- AC4: Chỉ tính đơn "Hoàn thành" (không tính đơn đã huỷ).

## 6. Ma trận phân quyền

| Chức năng | ADMIN | MANAGER | STAFF |
|---|:-:|:-:|:-:|
| Tổng quan / báo cáo | ✔ | ✔ | – |
| Bán hàng (tạo đơn) | ✔ | ✔ | ✔ |
| Xem đơn | tất cả | tất cả | của mình |
| Huỷ đơn | ✔ | ✔ | – |
| Xem sản phẩm | ✔ | ✔ | ✔ (ẩn giá vốn) |
| Thêm/sửa/xoá sản phẩm, nhóm hàng | ✔ | ✔ | – |
| Khách hàng: xem/thêm/sửa | ✔ | ✔ | ✔ |
| Khách hàng: xoá | ✔ | ✔ | – |
| Nhập hàng, kiểm kho, lịch sử kho | ✔ | ✔ | – |
| Quản lý người dùng | ✔ | – | – |

## 7. Yêu cầu phi chức năng
- **Bảo mật:** JWT access 15 phút + refresh 7 ngày xoay vòng; mật khẩu BCrypt; phân quyền ở API; validate mọi input; không lộ stack trace.
- **Hiệu năng:** API danh sách < 300 ms với 10.000 sản phẩm/100.000 đơn (có index); tạo đơn < 500 ms.
- **Toàn vẹn dữ liệu:** không bán âm kho khi nhiều quầy bán đồng thời; mọi thay đổi tồn có lịch sử.
- **Khả dụng:** chạy bằng 1 lệnh `docker compose up`; healthcheck.
- **UX:** tiếng Việt có dấu, định dạng tiền `1.250.000 ₫`, ngày `05/11/2026`; dùng tốt trên màn hình 360px.
- **Đa ngôn ngữ API:** thông báo lỗi tiếng Việt (mặc định) / tiếng Anh.

## 8. Ngoài phạm vi MVP (rõ ràng)
Thuế VAT, hoá đơn điện tử, nhiều chi nhánh, công nợ, in ấn, khuyến mãi tự động, offline.
