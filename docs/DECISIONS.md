# Nhật ký quyết định (Decision Log)

Ghi lại các điểm chưa rõ trong yêu cầu và phương án đã chọn. Mỗi mục: **Bối cảnh → Quyết định → Lý do**.

---

## D01. Sản phẩm cần xây dựng
- **Bối cảnh:** Yêu cầu nêu quy trình làm việc và stack nhưng không kèm "phần đặc tả" sản phẩm.
- **Quyết định:** Xây **TiệmPro – hệ thống quản lý bán hàng & kho cho cửa hàng bán lẻ nhỏ** (tạp hoá, văn phòng phẩm, cửa hàng tiện lợi 1–3 điểm bán).
- **Lý do:** Repo là template "management system" đã có module Product, auth, phân quyền; bài toán bán hàng + kho tận dụng tối đa nền tảng đó, có nhu cầu thực tế lớn ở Việt Nam (hộ kinh doanh), dễ demo end-to-end và dễ định giá bán.

## D02. Vị trí mã nguồn backend / frontend
- **Quyết định:** Giữ backend Spring Boot ở thư mục gốc (như template), thêm frontend ở `frontend/`, e2e ở `e2e/`.
- **Lý do:** Không phải di chuyển toàn bộ template (Dockerfile, CI, script đổi tên vẫn chạy nguyên), giảm rủi ro hỏng build.

## D03. Nhánh và Pull Request
- **Quyết định:** Nhánh `feature/mvp` tách từ nhánh template (PR #1 chưa merge). Draft PR mở vào `main`.
- **Lý do:** `main` là nhánh gốc của repo; PR vào `main` chứa cả template + MVP nên merge một lần là đủ. Nếu PR #1 được merge trước, diff của PR MVP tự thu gọn còn phần MVP.

## D04. Đăng ký tài khoản công khai
- **Quyết định:** Thêm cờ `app.auth.registration-enabled`, **mặc định tắt**. Tài khoản nhân viên do Chủ cửa hàng (ADMIN) tạo.
- **Lý do:** Phần mềm nội bộ cửa hàng không nên cho người lạ tự đăng ký. Cờ vẫn giữ để template dùng cho dự án khác (test bật cờ này).

## D05. Vai trò
- **Quyết định:** 3 vai trò nghiệp vụ `ADMIN` (Chủ cửa hàng), `MANAGER` (Quản lý), `STAFF` (Nhân viên bán hàng) + giữ `USER` của template (chỉ xem sản phẩm).
- **Lý do:** Đủ phủ mô hình cửa hàng nhỏ: chủ xem báo cáo & quản lý người dùng, quản lý lo hàng hoá/kho, nhân viên bán hàng.

## D06. Tồn kho thay đổi qua chứng từ
- **Quyết định:** Tồn kho (`products.quantity`) chỉ được nhập lúc tạo sản phẩm; sau đó chỉ thay đổi qua **phiếu nhập**, **đơn bán**, **huỷ đơn**, **kiểm kho (điều chỉnh)**. Mọi thay đổi ghi vào `stock_movements`.
- **Lý do:** Đảm bảo truy vết được tồn kho (yêu cầu cốt lõi của quản lý kho); sửa sản phẩm không làm lệch tồn.

## D07. Chống bán vượt tồn khi nhiều người bán cùng lúc
- **Quyết định:** Khi tạo đơn, khoá các dòng sản phẩm bằng `SELECT ... FOR UPDATE` (PESSIMISTIC_WRITE) theo thứ tự id tăng dần.
- **Lý do:** Đơn giản, chắc chắn đúng trên MySQL InnoDB; khoá theo thứ tự cố định tránh deadlock. Lượng giao dịch của cửa hàng nhỏ không gây nghẽn.

## D08. Sinh mã chứng từ
- **Quyết định:** Mã dạng `DH260105-0001` (đơn hàng), `PN260105-0001` (phiếu nhập): tiền tố + ngày + số thứ tự trong ngày, dùng bảng `code_sequences` với `INSERT ... ON DUPLICATE KEY UPDATE value = LAST_INSERT_ID(value + 1)`.
- **Lý do:** Mã dễ đọc cho nhân viên, nguyên tử, không cần khoá bảng.

## D09. Không xoá cứng chứng từ
- **Quyết định:** Đơn hàng chỉ được **huỷ** (hoàn tồn kho), không xoá. Phiếu nhập không sửa/xoá sau khi lưu (MVP). Sản phẩm đã phát sinh giao dịch không xoá được → chuyển "Ngừng kinh doanh".
- **Lý do:** Chuẩn kế toán/kho cơ bản, giữ toàn vẹn báo cáo.

## D10. Nhà cung cấp
- **Quyết định:** MVP lưu tên nhà cung cấp dạng text trên phiếu nhập, chưa có danh mục nhà cung cấp.
- **Lý do:** Giảm phạm vi; danh mục NCC và công nợ để sau MVP.

## D11. Lưu token ở frontend
- **Quyết định:** Access + refresh token lưu trong `localStorage`, axios interceptor tự refresh (single-flight) khi gặp `TOKEN_EXPIRED`.
- **Lý do:** Đơn giản, phù hợp SPA gọi API cùng origin qua nginx. Rủi ro XSS được giảm bằng: access token sống 15 phút, refresh token xoay vòng + phát hiện dùng lại, CSP cơ bản ở nginx, React tự escape. Phương án cookie HttpOnly ghi vào đề xuất phiên bản sau.

## D12. Triển khai frontend
- **Quyết định:** Frontend build tĩnh, phục vụ bằng nginx; nginx proxy `/api` sang backend. `docker compose up` chạy 3 service: `mysql`, `app`, `web` (http://localhost:3000).
- **Lý do:** Một origin → không cần CORS, cấu hình production-like, một lệnh chạy toàn bộ.

## D13. Dữ liệu demo
- **Quyết định:** Dữ liệu demo tiếng Việt nằm trong Flyway location riêng `db/demo`, bật bằng `APP_DEMO_DATA=true` (mặc định bật trong `.env.example` và profile dev; tắt khi triển khai thật).
- **Lý do:** Demo được ngay sau `docker compose up`, nhưng không lẫn dữ liệu giả vào môi trường thật.

## D14. Biểu đồ
- **Quyết định:** Biểu đồ doanh thu vẽ bằng CSS/Tailwind thuần, không dùng thư viện chart.
- **Lý do:** Chỉ cần biểu đồ cột đơn giản; tránh tăng ~100KB bundle.

## D15. Tiền tệ
- **Quyết định:** Chỉ VND, lưu `DECIMAL(15,2)`, hiển thị không lẻ (`1.250.000 ₫`). Không quản lý thuế VAT trong MVP.
- **Lý do:** Đối tượng là hộ kinh doanh, phần lớn không xuất hoá đơn VAT.

## D16. Kiểm thử e2e
- **Quyết định:** Playwright chạy trên stack `docker compose` thật (http://localhost:3000), CI có job e2e riêng.
- **Lý do:** Kiểm thử đúng cấu hình triển khai (nginx proxy + backend + MySQL).
