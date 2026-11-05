# Spring Boot Starter — template cho hệ thống quản lý

Template backend dùng lại cho các dự án khách hàng (management system): đã có sẵn xác thực JWT, phân quyền, response chuẩn, i18n, phân trang/lọc động, upload file, Docker, CI và một module CRUD mẫu để copy.

**Stack:** Java 21 · Spring Boot 3.5 · Spring Security + JWT (access + refresh, logout, blacklist) · Spring Data JPA · MySQL 8 · Flyway · MapStruct · Lombok · springdoc-openapi · JUnit 5 + Testcontainers

---

## Tạo dự án mới trong 5 phút

**Yêu cầu:** JDK 21, Docker (Docker Desktop hoặc Docker Engine + Compose v2).

**1. Lấy mã nguồn** (nút **Use this template** trên GitHub, hoặc clone rồi bỏ lịch sử git):

```bash
git clone https://github.com/ducpham04/springboot-starter.git inventory-api
```

```bash
cd inventory-api && rm -rf .git && git init
```

**2. Đổi tên groupId / artifactId / package** (tự di chuyển thư mục và đổi tên class `Application`):

```bash
./scripts/rename-project.sh com.acme inventory-api com.acme.inventory
```

**3. Tạo file `.env`** và đặt JWT secret:

```bash
cp .env.example .env
```

```bash
sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$(openssl rand -base64 48)|" .env
```

(Trên macOS dùng `sed -i ''`, hoặc mở `.env` và dán kết quả của `openssl rand -base64 48`.)

**4. Chạy bằng Docker Compose** (MySQL + app):

```bash
docker compose up -d --build
```

**5. Kiểm tra:**

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health
- Đăng nhập bằng admin khởi tạo trong `.env` (`APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`, mặc định `admin@example.com` / `Admin@123456`), copy `accessToken` vào nút **Authorize**.

Xong. Tiếp theo: đổi mật khẩu admin, xoá module `product` nếu không cần (hoặc copy nó làm module đầu tiên, xem [Thêm module mới](#thêm-module-mới-copy-theo-product)).

---

## Chạy khi phát triển

| Cách | Lệnh | Ghi chú |
|---|---|---|
| IDE + MySQL trong Docker | `docker compose up -d mysql` rồi chạy `StarterApplication` | Profile `dev` mặc định, đã có JWT secret dev và admin `admin@example.com` / `Admin@123` |
| Không cần compose | `./mvnw spring-boot:test-run` | Dùng `TestStarterApplication`, tự bật một MySQL tạm bằng Testcontainers |
| Toàn bộ trong Docker | `docker compose up -d --build` | Profile `prod`, đọc `.env` |

Build và test:

```bash
./mvnw verify
```

`./mvnw test` chỉ chạy unit test (`*Test`); `./mvnw verify` chạy thêm integration test (`*IT`, cần Docker).

---

## Cấu trúc thư mục

Package chia theo **feature**; mỗi feature tự chứa entity / repository / dto / mapper / service / controller.

```
src/main/java/com/example/starter
├── StarterApplication.java
├── common/                 # dùng chung cho mọi module
│   ├── entity/BaseEntity          id, version, createdAt, updatedAt, createdBy, updatedBy
│   ├── response/ApiResponse       envelope chuẩn + PageResponse
│   ├── exception/                 ErrorCode, AppException, GlobalExceptionHandler
│   ├── paging/SpecificationBuilder lọc động null-safe
│   └── util/                      MessageHelper (i18n), TokenGenerator
├── config/                 # AppProperties (app.*), i18n, OpenAPI, JPA auditing, AdminSeeder, Clock
├── security/               # SecurityConfig, JWT filter/service, blacklist, UserPrincipal
├── auth/                   # đăng ký, đăng nhập, refresh, logout, đổi/quên mật khẩu
├── user/                   # User, Role, Permission, API /users
├── product/                # ★ module CRUD mẫu để copy
├── file/                   # upload/download, StorageService (local → S3)
└── mail/                   # MailService (log giả / SMTP)

src/main/resources
├── application.yml / application-dev.yml / application-prod.yml
├── db/migration/           # Flyway: V1 schema auth, V2 seed role/permission, V3 product
└── i18n/messages*.properties   # thông báo tiếng Anh (mặc định) và tiếng Việt
```

---

## Xác thực (JWT)

| Method | Endpoint | Auth | Mô tả |
|---|---|---|---|
| POST | `/api/v1/auth/register` | – | Đăng ký (role `USER`), trả về cặp token |
| POST | `/api/v1/auth/login` | – | Đăng nhập |
| POST | `/api/v1/auth/refresh` | – | Đổi refresh token lấy cặp token mới |
| POST | `/api/v1/auth/logout` | Bearer | Blacklist access token + thu hồi refresh token (body `{"refreshToken": "..."}` là tuỳ chọn) |
| POST | `/api/v1/auth/change-password` | Bearer | Đổi mật khẩu, đăng xuất mọi thiết bị |
| POST | `/api/v1/auth/forgot-password` | – | Gửi link đặt lại mật khẩu qua mail (luôn trả 200 để không lộ email nào tồn tại) |
| POST | `/api/v1/auth/reset-password` | – | Đặt lại mật khẩu bằng token trong mail (dùng 1 lần) |
| GET | `/api/v1/users/me` | Bearer | Thông tin người dùng hiện tại |

Cơ chế:

- **Access token**: JWT HMAC, sống ngắn (`JWT_ACCESS_TOKEN_TTL`, mặc định 15 phút), có claim `jti` và `ver`.
- **Refresh token**: chuỗi ngẫu nhiên, DB chỉ lưu SHA-256. **Đổi mới sau mỗi lần refresh**; nếu một token đã bị thay thế được dùng lại, hệ thống coi là bị đánh cắp và thu hồi mọi refresh token của user đó.
- **Logout**: `jti` của access token vào bảng `revoked_access_tokens` (blacklist), một job hằng giờ xoá các bản ghi đã hết hạn. Muốn dùng Redis thì viết bean khác implement `TokenBlacklistService` và đánh dấu `@Primary`.
- **Đổi/đặt lại mật khẩu**: tăng `users.token_version`, nên mọi access token cũ bị từ chối ngay, đồng thời thu hồi toàn bộ refresh token.
- Mỗi request nạp lại user từ DB, nên khoá tài khoản (`enabled = false`) hoặc đổi quyền có hiệu lực ngay.

---

## Phân quyền: Role + Permission

- Bảng `roles`, `permissions`, `role_permissions`, `user_roles`.
- Authority của user = `ROLE_<role.code>` + mọi `permission.code` thuộc các role của user.
- Seed sẵn: `ADMIN` (mọi quyền), `USER` (`PRODUCT_READ`).

Ví dụ trong controller:

```java
@PreAuthorize("hasAuthority('PRODUCT_READ')")                          // theo permission (nên dùng)
@PreAuthorize("hasRole('ADMIN')")                                       // theo role
@PreAuthorize("hasRole('ADMIN') and hasAuthority('PRODUCT_DELETE')")    // kết hợp
```

Thêm quyền mới: tạo migration Flyway mới chèn vào `permissions` và gán cho role trong `role_permissions` (xem `V3__product.sql`).

Endpoint công khai được khai báo trong `SecurityConfig.PUBLIC_ENDPOINTS`.

---

## Response chuẩn & xử lý lỗi

Mọi response đều có dạng:

```json
{
  "success": false,
  "code": "VALIDATION_FAILED",
  "message": "Dữ liệu không hợp lệ",
  "errors": [{ "field": "email", "message": "email không đúng định dạng" }],
  "timestamp": "2026-01-01T00:00:00Z"
}
```

Khi thành công: `success: true`, `code: "SUCCESS"`, dữ liệu nằm trong `data`.

- Lỗi nghiệp vụ: `throw new AppException(ErrorCode.PRODUCT_NOT_FOUND, id)`. Thêm mã lỗi mới vào enum `ErrorCode` (gồm HTTP status và message key), rồi thêm message vào cả hai file `messages.properties` và `messages_vi.properties`.
- `GlobalExceptionHandler` chuyển các lỗi validation, JSON sai, sai tham số sort, 401/403, vi phạm unique, xung đột optimistic lock, file quá lớn... về đúng format trên. Lỗi 401/403 phát sinh trong filter chain được `RestAuthenticationEntryPoint` trả về cùng format.

### Đa ngôn ngữ (vi / en)

- Ngôn ngữ lấy từ header `Accept-Language: vi` hoặc `Accept-Language: en`; không có header thì dùng `APP_DEFAULT_LOCALE` (mặc định `vi`).
- Message validation của Bean Validation (`@NotBlank`, `@Email`, `@Size`...) đã được dịch sẵn trong `i18n/messages_vi.properties`.
- Message tuỳ chỉnh: `@Pattern(regexp = "...", message = "{validation.product.sku.pattern}")`.

---

## Phân trang & lọc động

Mọi API danh sách dùng tham số chuẩn của Spring: `page` (bắt đầu từ 0), `size` (tối đa 100), `sort=field,asc|desc` (có thể lặp lại). Kết quả được bọc trong `PageResponse`.

```
GET /api/v1/products?keyword=phone&status=ACTIVE&category=a&category=b&minPrice=10&maxPrice=100&page=0&size=20&sort=price,desc
```

Bộ lọc dùng `SpecificationBuilder`: mọi giá trị `null` hoặc rỗng đều được bỏ qua, nên không cần viết `if`:

```java
Specification<Product> spec = SpecificationBuilder.<Product>create()
        .likeAny(filter.keyword(), "name", "sku", "description")  // OR, không phân biệt hoa thường
        .equal("status", filter.status())
        .in("category", filter.category())
        .between("price", filter.minPrice(), filter.maxPrice())
        .equal("roles.code", "ADMIN")                              // thuộc tính lồng nhau → LEFT JOIN
        .build();
repository.findAll(spec, pageable);
```

Repository chỉ cần `extends JpaSpecificationExecutor<Entity>`.

---

## Upload file

| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/v1/files` (multipart, field `file`) | Upload, trả về `key` và `url` |
| GET | `/api/v1/files?key=...` | Tải file |
| DELETE | `/api/v1/files?key=...` | Xoá (cần quyền `FILE_DELETE`) |

- Giới hạn dung lượng: `STORAGE_MAX_FILE_SIZE`. Danh sách content-type cho phép: `app.storage.allowed-content-types`.
- `LocalStorageService` lưu vào `STORAGE_LOCAL_DIR/yyyy/MM/uuid.ext` và chặn path traversal.
- **Chuyển sang S3/MinIO**: tạo `S3StorageService implements StorageService` gắn `@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")`, rồi đặt `STORAGE_PROVIDER=s3`.

## Gửi mail

`MailService` là interface; chọn implementation bằng `MAIL_PROVIDER`:

- `log` (mặc định): `LoggingMailService` in nội dung mail ra log, tiện cho dev và test.
- `smtp`: `SmtpMailService` gửi bất đồng bộ qua `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

---

## Thêm module mới (copy theo Product)

Ví dụ thêm module `Customer`:

1. Copy thư mục `product/` thành `customer/`, rồi đổi tên class và package.
2. **Entity** `extends BaseEntity` (tự có id, version và các cột audit).
3. **Migration** `V4__customer.sql`: tạo bảng có đủ các cột `id, version, created_at, updated_at, created_by, updated_by`; thêm permission `CUSTOMER_*` và gán cho role.
4. **DTO**: `CustomerRequest` (kèm annotation validation), `CustomerResponse`, `CustomerFilter`.
5. **Mapper** MapStruct `@Mapper` (componentModel spring đã cấu hình trong `pom.xml`).
6. **Repository** `extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer>`.
7. **Service**: dùng `SpecificationBuilder` để lọc, ném `AppException` khi lỗi; thêm `ErrorCode` và message vi/en.
8. **Controller**: trả `ApiResponse`, gắn `@PreAuthorize("hasAuthority('CUSTOMER_READ')")`.
9. **Test**: copy `ProductServiceTest` (unit) và `ProductIT` (integration).

---

## Test

| Loại | Vị trí | Công cụ |
|---|---|---|
| Unit | `*Test.java` | JUnit 5, Mockito, AssertJ: `ProductServiceTest`, `JwtServiceTest`, `LocalStorageServiceTest`, `SpecificationBuilderTest` |
| Integration | `*IT.java` | `@SpringBootTest` + MockMvc + MySQL thật (Testcontainers): `AuthIT`, `ProductIT`, `FileIT` |

Integration test kế thừa `AbstractIntegrationTest`. Container MySQL được dùng chung cho mọi test class nhờ cơ chế cache context của Spring. `CapturingMailService` giữ lại các mail đã gửi để test đọc link đặt lại mật khẩu.

---

## Cấu hình

Mọi cấu hình tuỳ biến nằm dưới `app.*` (được kiểm tra khi khởi động qua `AppProperties`) và đọc từ biến môi trường. Danh sách đầy đủ có trong [`.env.example`](.env.example).

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (`prod` trong Docker) | `dev`: secret và admin mặc định, log DEBUG. `prod`: mọi secret bắt buộc phải có, Swagger tắt trừ khi `SWAGGER_ENABLED=true` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | `localhost`, `3306`, `starter`... | Kết nối MySQL |
| `JWT_SECRET` | – (bắt buộc ở `prod`) | Khoá HMAC dạng Base64, tối thiểu 32 byte |
| `JWT_ACCESS_TOKEN_TTL`, `JWT_REFRESH_TOKEN_TTL` | `15m`, `7d` | Thời hạn token |
| `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` | trống (dev: `admin@example.com` / `Admin@123`) | Admin được tạo khi khởi động nếu chưa tồn tại |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Danh sách origin, phân tách bằng dấu phẩy |
| `MAIL_PROVIDER` | `log` | `log` hoặc `smtp` |
| `STORAGE_PROVIDER`, `STORAGE_LOCAL_DIR` | `local`, `./uploads` | Nơi lưu file |

## Docker & CI

- `Dockerfile`: multi-stage (JDK build → JRE runtime), tách layer jar để cache, chạy bằng user không phải root, có `HEALTHCHECK` gọi `/actuator/health`, file upload nằm ở volume `/app/uploads`.
- `docker-compose.yml`: `mysql` (có healthcheck) + `app`; dữ liệu lưu trong volume `mysql-data` và `uploads`.
- `.github/workflows/ci.yml`: `./mvnw verify` (unit + Testcontainers), lưu báo cáo test, rồi build Docker image. Muốn push image lên registry thì thêm `docker/login-action` và đặt `push: true`.
