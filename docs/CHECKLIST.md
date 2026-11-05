# CHECKLIST – TiệmPro MVP

> Project Manager · Thứ tự = thứ tự ưu tiên. Tick `[x]` khi xong (đã có test/kiểm tra chạy được).
> Nguyên tắc: luồng chính chạy end-to-end trước, rồi mới mở rộng.

## Giai đoạn 0 – Tài liệu
- [x] PRD (`docs/PRD.md`)
- [x] Design system & màn hình (`docs/DESIGN.md`)
- [x] Kiến trúc, ERD, API (`docs/ARCHITECTURE.md`)
- [x] Checklist (file này) & nhật ký quyết định (`docs/DECISIONS.md`)
- [x] Commit, push `feature/mvp`, mở Draft PR

## Giai đoạn 1 – Backend nền tảng & danh mục
- [ ] Migration: vai trò MANAGER/STAFF, permission mới, cờ tắt đăng ký công khai
- [ ] `/users/me` trả permissions; quản lý người dùng (tạo, sửa, khoá/mở, danh sách vai trò)
- [ ] Nhóm hàng (CRUD + số sản phẩm)
- [ ] Mở rộng sản phẩm: nhóm, đơn vị, giá vốn, ngưỡng cảnh báo; lọc sắp hết; ẩn giá vốn; không đổi tồn khi sửa; chặn xoá khi có giao dịch
- [ ] Khách hàng (CRUD, tìm kiếm, thống kê chi tiêu)

## Giai đoạn 2 – Backend nghiệp vụ chính
- [ ] Sinh mã chứng từ (`code_sequences`)
- [ ] Bán hàng: tạo đơn (khoá tồn, kiểm tra tồn, giảm giá), danh sách/chi tiết (STAFF chỉ thấy đơn của mình), huỷ đơn hoàn tồn
- [ ] Kho: phiếu nhập (giá vốn bình quân), kiểm kho, lịch sử biến động
- [ ] Tổng quan (dashboard)
- [ ] Dữ liệu demo tiếng Việt + tài khoản demo 3 vai trò (`db/demo`)
- [ ] Test backend: unit (Order, Inventory, Dashboard) + integration (luồng bán hàng, kho, phân quyền)

## Giai đoạn 3 – Frontend luồng chính
- [ ] Khởi tạo Vite + React + TS + Tailwind + React Query + Router + RHF + Zod
- [ ] Bộ UI component dùng chung + App shell (sidebar/drawer, menu theo quyền)
- [ ] Đăng nhập (tài khoản demo), refresh token tự động, đăng xuất, guard route theo quyền
- [ ] Bán hàng (POS) – desktop 2 cột, mobile bottom-sheet
- [ ] Đơn hàng: danh sách, chi tiết, huỷ đơn
- [ ] Docker: `frontend/Dockerfile` + nginx proxy; `docker compose up` chạy cả 3 service

## Giai đoạn 4 – Frontend mở rộng
- [ ] Sản phẩm: danh sách + lọc, form thêm/sửa, upload ảnh
- [ ] Nhóm hàng
- [ ] Khách hàng: danh sách, modal, chi tiết
- [ ] Kho: phiếu nhập (danh sách, tạo, chi tiết), lịch sử kho, kiểm kho
- [ ] Tổng quan: KPI, biểu đồ 14 ngày, top bán chạy, sắp hết, đơn gần đây
- [ ] Người dùng (ADMIN)
- [ ] Tài khoản: đổi mật khẩu; quên/đặt lại mật khẩu; trang 404/403

## Giai đoạn 5 – QA
- [ ] `docs/TEST_PLAN.md`
- [ ] Unit test frontend (Vitest)
- [ ] E2E Playwright luồng chính (đăng nhập → bán hàng → đơn hàng; nhập kho; phân quyền)
- [ ] CI: job backend, frontend, e2e
- [ ] Tất cả test pass

## Giai đoạn 6 – Review & bàn giao
- [ ] Review bảo mật (auth, validation, injection, secret, header) + hiệu năng (N+1, index) và sửa lỗi
- [ ] README hoàn chỉnh (giới thiệu, tính năng, ảnh chụp, cách chạy, tài khoản demo, deploy)
- [ ] `docs/FINAL_REPORT.md`
- [ ] PR chuyển Ready for review
