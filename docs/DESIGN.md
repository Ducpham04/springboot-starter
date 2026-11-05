# DESIGN – TiệmPro

> UI/UX Designer · Mobile-first · Tiếng Việt · Tailwind CSS

## 1. Nguyên tắc thiết kế
1. **Nhanh tại quầy:** thao tác chính (bán hàng) tối đa 1 màn hình, nút lớn (≥ 44px), tìm kiếm luôn sẵn sàng.
2. **Rõ ràng hơn đẹp:** số tiền to, đậm, căn phải; trạng thái bằng màu + chữ (không chỉ màu).
3. **Mobile-first:** thiết kế cho 360px trước, mở rộng lên tablet (≥ 768px) và desktop (≥ 1024px).
4. **Nhất quán:** một bộ component dùng chung cho mọi màn hình.
5. **Tiếp cận được:** tương phản ≥ 4.5:1, focus ring rõ, label cho mọi input, hỗ trợ bàn phím.

## 2. Design system

### 2.1 Màu sắc (Tailwind tokens)
| Vai trò | Token | Hex | Dùng cho |
|---|---|---|---|
| Primary | `brand-600` / hover `brand-700` | `#2563EB` / `#1D4ED8` | Nút chính, link, menu active |
| Primary nhạt | `brand-50` / `brand-100` | `#EFF6FF` / `#DBEAFE` | Nền menu active, highlight |
| Thành công | `emerald-600` / nền `emerald-50` | `#059669` | Hoàn thành, nhập kho (+) |
| Cảnh báo | `amber-600` / nền `amber-50` | `#D97706` | Sắp hết hàng |
| Nguy hiểm | `rose-600` / nền `rose-50` | `#E11D48` | Huỷ, xoá, hết hàng, xuất (−) |
| Chữ chính | `slate-900` | `#0F172A` | Tiêu đề, nội dung |
| Chữ phụ | `slate-500` | `#64748B` | Mô tả, nhãn |
| Viền | `slate-200` | `#E2E8F0` | Card, input, bảng |
| Nền trang | `slate-50` | `#F8FAFC` | Background |
| Bề mặt | `white` | `#FFFFFF` | Card, modal |

### 2.2 Typography
- Font: **Inter** (Google Fonts, hỗ trợ tiếng Việt đầy đủ), fallback `system-ui`.
- Số tiền dùng `tabular-nums` để thẳng cột.

| Cấp | Class | Size / weight |
|---|---|---|
| H1 tiêu đề trang | `text-xl md:text-2xl font-semibold` | 20/24px · 600 |
| H2 tiêu đề card | `text-base font-semibold` | 16px · 600 |
| Body | `text-sm` | 14px · 400 |
| Caption / nhãn | `text-xs text-slate-500` | 12px |
| Số liệu lớn (KPI) | `text-2xl font-bold tabular-nums` | 24px · 700 |

### 2.3 Spacing, bo góc, đổ bóng
- Thang spacing 4px của Tailwind; padding trang `p-4 md:p-6`; khoảng cách giữa các khối `space-y-4 md:space-y-6`.
- Bo góc: input/nút `rounded-lg` (8px), card/modal `rounded-xl` (12px), badge `rounded-full`.
- Đổ bóng: card `shadow-sm`, modal/drawer `shadow-xl`.
- Breakpoint: `sm 640` · `md 768` · `lg 1024` · `xl 1280`.

### 2.4 Component
| Component | Biến thể / ghi chú |
|---|---|
| **Button** | `primary`, `secondary` (viền), `danger`, `ghost`; size `sm`/`md`; trạng thái `loading` (spinner + disabled) |
| **Input / Textarea / Select** | Label phía trên, thông báo lỗi đỏ phía dưới (`aria-invalid`), hint xám |
| **MoneyInput** | Nhập số, hiển thị phân cách nghìn |
| **Card** | Nền trắng, viền `slate-200`, `rounded-xl`, header tuỳ chọn |
| **StatCard** | Icon tròn màu + nhãn + số lớn + mô tả phụ |
| **Badge** | `green` Hoàn thành/Đang bán · `red` Đã huỷ/Hết hàng · `amber` Sắp hết · `slate` Ngừng KD |
| **DataTable** | Desktop: bảng; mobile (< md): mỗi dòng thành card xếp dọc. Có trạng thái loading (skeleton), rỗng (EmptyState), lỗi |
| **Pagination** | "Trước / Sau" + "Trang x / y", tổng số bản ghi |
| **Modal** | Mobile: bottom-sheet full chiều rộng; desktop: giữa màn hình, max-w-lg. Đóng bằng Esc / nút X |
| **ConfirmDialog** | Cho thao tác nguy hiểm (xoá, huỷ đơn), nút đỏ |
| **Toast** | Góc trên phải (desktop) / trên giữa (mobile), tự ẩn 3s |
| **EmptyState** | Icon + câu mô tả + nút hành động |
| **SearchBox** | Icon kính lúp, debounce 300ms |
| **PageHeader** | Tiêu đề + mô tả + vùng nút hành động bên phải (mobile xuống dòng) |

Icon: **lucide-react**, nét 1.75, size 18–20px.

### 2.5 Định dạng
- Tiền: `1.250.000 ₫` (`Intl.NumberFormat('vi-VN', {style:'currency', currency:'VND'})`).
- Ngày giờ: `05/11/2026 14:30`; ngày: `05/11/2026`.
- Số lượng: `1.250`.

## 3. Bố cục (App shell)
- **Desktop (≥ lg):** sidebar trái cố định 256px (logo, menu theo nhóm, thông tin người dùng + đăng xuất ở đáy); nội dung bên phải.
- **Mobile/tablet:** top bar (nút menu ☰, tên trang, avatar) + sidebar dạng drawer trượt từ trái, có lớp phủ mờ.
- Menu chỉ hiện các mục người dùng có quyền.

## 4. Sitemap

```
/login                       Đăng nhập
/forgot-password             Quên mật khẩu
/reset-password?token=       Đặt lại mật khẩu
/                            Tổng quan            (ADMIN, MANAGER)
/pos                         Bán hàng             (mọi vai trò)
/orders                      Đơn hàng
/orders/:id                  Chi tiết đơn
/products                    Sản phẩm
/products/new                Thêm sản phẩm        (ADMIN, MANAGER)
/products/:id/edit           Sửa sản phẩm         (ADMIN, MANAGER)
/categories                  Nhóm hàng            (ADMIN, MANAGER)
/customers                   Khách hàng
/customers/:id               Chi tiết khách hàng
/inventory/receipts          Phiếu nhập           (ADMIN, MANAGER)
/inventory/receipts/new      Tạo phiếu nhập       (ADMIN, MANAGER)
/inventory/receipts/:id      Chi tiết phiếu nhập  (ADMIN, MANAGER)
/inventory/movements         Lịch sử kho + kiểm kho (ADMIN, MANAGER)
/users                       Người dùng           (ADMIN)
/profile                     Tài khoản & đổi mật khẩu
*                            404
```

Nhóm menu: **Tổng quan** · **Bán hàng** (Bán hàng, Đơn hàng, Khách hàng) · **Hàng hoá** (Sản phẩm, Nhóm hàng) · **Kho** (Phiếu nhập, Lịch sử kho) · **Hệ thống** (Người dùng).

## 5. Mô tả màn hình

### 5.1 Đăng nhập
- Bố cục 2 cột trên desktop: trái là panel thương hiệu nền gradient xanh (logo, slogan "Bán hàng nhanh – Kho luôn đúng", 3 gạch đầu dòng lợi ích); phải là form. Mobile: chỉ form, logo trên cùng.
- Form: Email, Mật khẩu (nút hiện/ẩn), nút "Đăng nhập" full width, link "Quên mật khẩu?".
- Hộp "Tài khoản demo" bấm để tự điền (chỉ hiện khi bật dữ liệu demo).
- Lỗi hiển thị dạng alert đỏ phía trên form.

### 5.2 Quên mật khẩu / Đặt lại mật khẩu
- Một input + nút; sau khi gửi hiện thông báo trung tính. Trang đặt lại: mật khẩu mới + nhập lại, kiểm tra khớp.

### 5.3 Tổng quan
- Hàng 4–5 **StatCard** (mobile 2 cột, desktop 5 cột): Doanh thu hôm nay, Đơn hôm nay, Doanh thu tháng, Lợi nhuận gộp tháng, Sắp hết hàng.
- **Biểu đồ cột doanh thu 14 ngày** (cột xanh, tooltip khi hover/chạm hiển thị ngày + số tiền; trục ngày dạng `05/11`).
- 2 cột (desktop) / xếp dọc (mobile): **Top 5 bán chạy** (tên, SL bán, doanh thu, thanh tiến độ) · **Sắp hết hàng** (tên, tồn/ngưỡng, badge amber, link "Nhập hàng").
- **Đơn gần đây**: 5 đơn, mã, khách, tổng, trạng thái, thời gian.

### 5.4 Bán hàng (POS) – màn hình quan trọng nhất
- **Desktop:** 2 cột. Trái (60%): ô tìm kiếm lớn + chip lọc nhóm hàng + lưới thẻ sản phẩm (tên, SKU, giá, tồn; thẻ hết hàng mờ và không bấm được). Phải (40%): **Giỏ hàng** dính (sticky) – chọn khách (ô tìm SĐT + nút "+ Khách mới"), danh sách dòng (tên, đơn giá, nút −/+ và ô số lượng, thành tiền, nút xoá), tóm tắt (Tạm tính, Giảm giá, **Khách cần trả** to đậm), phương thức thanh toán (3 nút toggle), "Tiền khách đưa" + "Tiền thừa" (khi tiền mặt), ghi chú, nút **"Thanh toán (F9)"** lớn màu xanh.
- **Mobile:** lưới sản phẩm full màn hình; thanh dưới cùng cố định "🛒 3 món · 125.000 ₫ – Xem giỏ" mở giỏ hàng dạng bottom-sheet toàn màn hình.
- Sau thanh toán: modal thành công hiển thị mã đơn, tổng tiền, tiền thừa, nút "Đơn mới" (focus sẵn) và "Xem đơn".

### 5.5 Đơn hàng
- Bộ lọc: từ ngày – đến ngày (mặc định 30 ngày), trạng thái, ô tìm kiếm.
- Bảng: Mã đơn, Thời gian, Khách hàng, Người bán, Thanh toán, Tổng tiền (căn phải), Trạng thái. Mobile: card.
- **Chi tiết đơn:** header mã đơn + badge trạng thái + nút "Huỷ đơn" (đỏ, theo quyền); thông tin khách, người bán, thời gian, phương thức; bảng dòng hàng; tóm tắt tiền; nếu đã huỷ hiển thị lý do, người huỷ, thời điểm. Dialog huỷ yêu cầu nhập lý do.

### 5.6 Sản phẩm
- PageHeader + nút "Thêm sản phẩm".
- Bộ lọc: tìm kiếm, nhóm hàng, trạng thái, checkbox "Sắp hết hàng".
- Bảng: Ảnh thu nhỏ, SKU, Tên, Nhóm, Giá bán, Giá vốn (ẩn với STAFF), Tồn (badge amber nếu sắp hết, đỏ nếu = 0), Trạng thái, thao tác (Sửa, Xoá).
- **Form sản phẩm** (trang riêng, 2 cột desktop): Thông tin chung (SKU, Tên, Nhóm, Đơn vị tính, Mô tả), Giá & kho (Giá bán, Giá vốn, Tồn ban đầu – chỉ khi tạo mới, Ngưỡng cảnh báo), Ảnh (upload + xem trước), Trạng thái (toggle). Nút Lưu / Huỷ dính ở đáy trên mobile.

### 5.7 Nhóm hàng
- Bảng đơn giản: Tên, Mô tả, Số sản phẩm, thao tác. Thêm/sửa bằng modal.

### 5.8 Khách hàng
- Danh sách: tìm kiếm tên/SĐT; bảng Tên, SĐT, Email, Số đơn, Tổng chi tiêu. Thêm/sửa bằng modal.
- **Chi tiết:** card thông tin + 3 chỉ số (Số đơn, Tổng chi tiêu, Lần mua gần nhất) + bảng đơn gần đây.

### 5.9 Phiếu nhập
- Danh sách: Mã phiếu, Thời gian, Nhà cung cấp, Số dòng, Tổng tiền, Người tạo.
- **Tạo phiếu:** Nhà cung cấp, Ghi chú; ô tìm sản phẩm để thêm dòng; bảng dòng (Sản phẩm, Tồn hiện tại, SL nhập, Giá nhập (mặc định = giá vốn hiện tại), Thành tiền, xoá); tổng tiền; nút "Lưu phiếu nhập" (xác nhận vì không sửa được sau khi lưu).
- **Chi tiết:** thông tin + bảng dòng.

### 5.10 Lịch sử kho & kiểm kho
- Bộ lọc: sản phẩm, loại biến động. Bảng: Thời gian, Sản phẩm, Loại (badge), Thay đổi (+ xanh / − đỏ), Tồn sau, Chứng từ (link), Người thực hiện, Ghi chú.
- Nút "Kiểm kho" mở modal: chọn sản phẩm (hiện tồn hệ thống), nhập tồn thực tế, lý do → hiển thị chênh lệch trước khi xác nhận.

### 5.11 Người dùng (ADMIN)
- Bảng: Họ tên, Email, SĐT, Vai trò (badge), Trạng thái, Ngày tạo, thao tác Sửa / Khoá-Mở khoá.
- Modal tạo: Họ tên, Email, SĐT, Mật khẩu ban đầu, Vai trò (radio). Modal sửa: không có email/mật khẩu.

### 5.12 Tài khoản
- Thông tin cá nhân (chỉ đọc) + form đổi mật khẩu (mật khẩu hiện tại, mới, nhập lại). Đổi xong → đăng xuất về trang đăng nhập với thông báo.

## 6. Trạng thái & phản hồi
- Loading: skeleton cho bảng/card, spinner trong nút.
- Rỗng: EmptyState có hành động ("Chưa có sản phẩm – Thêm sản phẩm").
- Lỗi mạng/500: thẻ lỗi với nút "Thử lại".
- Lỗi validation từ API (`errors[]`) được map vào đúng field của form.
- Thành công: toast xanh. 403: trang "Bạn không có quyền truy cập".
