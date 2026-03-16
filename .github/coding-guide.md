# 🚀 Hướng dẫn Chạy dự án & Quản lý Database

Tài liệu này hướng dẫn cách thiết lập môi trường, phát triển cấu trúc DB bằng Flyway và cách chia sẻ dữ liệu test giữa các thành viên.



## 🏃 1. Cách chạy dự án chính xác

Để đảm bảo dự án chạy không lỗi Schema, hãy tuân thủ các bước sau:

1. **Khởi động Database:**
   Chạy trực tiếp source code sau khi pull code về, Flyway sẽ trực tiếp quét file migrate database để dựng bản.


2. **Kiểm tra DB:** Đảm bảo container `cosmetics_db_container` đang chạy (Dùng lệnh `docker ps`).

3. **Kiểm tra cấu trúc DB:** Vào Datasource thêm một data source mới cho postgresql với thông tin:
- user: `db_admin`
- password: `db_password_123`
- database: `cosmetics_db`
- host: `localhost`
- port: `5432`


*Lưu ý: Flyway sẽ tự động quét thư mục `src/main/resources/db/migration` để dựng bảng.*

---

## 🔄 2. Cách tạo file Migrate với JPA Buddy

Khi bạn thay đổi code (thêm field vào Entity), hãy để **JPA Buddy** tự viết SQL cho bạn nhằm tránh sai sót.

1. **Sửa Entity:** Thêm thuộc tính hoặc tạo Entity mới trong Java.
2. **Mở tab JPA Structure:** (Thường ở góc dưới bên trái IntelliJ).
3. **Chuột phải vào "Migrations":** Chọn **"Diff Changelog"** hoặc **"Flyway Migration"**.
4. **Cấu hình Diff:**
* **Source:** Java Entities.
* **Target:** DB hiện tại (PostgreSQL đang chạy trên Docker).


5. **Generate:** JPA Buddy sẽ so sánh và tạo ra một file `.sql` mới (ví dụ: `V2__add_column_x.sql`).
6. **Kiểm tra & Lưu:** File sẽ tự động được đặt vào `src/main/resources/db/migration`. Lần tới khi khởi động App, Flyway sẽ tự chạy file này.

---

## 💾 3. Backup dữ liệu để người khác có thể Test

Thông thường, chúng ta không đẩy thư mục `pg_data` của Docker lên Git vì nó rất nặng và khó quản lý phiên bản. Thay vào đó, ta sẽ sử dụng các script SQL.

### Bước A: Xuất dữ liệu (Export)

Khi bạn đã nhập dữ liệu mẫu vào máy mình và muốn chia sẻ, hãy chạy lệnh:

```bash
# Xuất toàn bộ dữ liệu ra file seed_data.sql
docker exec -t cosmetics-db-container pg_dump -U db_admin -d cosmetics_db --data-only --inserts > src/main/resources/db/test_data/seed_data.sql

```

*Ghi chú: Tham số `--data-only` chỉ lấy dữ liệu, không lấy cấu trúc (vì cấu trúc đã có Flyway lo).*

### Bước B: Đẩy lên Git

Thêm file `seed_data.sql` vào Git và push lên.

### Bước C: Người khác lấy về dùng (Import)

Sau khi đồng nghiệp lấy code về và chạy `docker-compose up`, họ chỉ cần chạy lệnh sau để có dữ liệu test giống hệt bạn:

```bash
cat src/main/resources/db/test_data/seed_data.sql | docker exec -i cosmetics_db_container psql -U db_admin -d cosmetics_db

```

---

## 💾 4. Đẩy code lên Github đúng cách

- Mỗi người tạo một branch có tên như sau: `HoTen_MSSV`
- Khi commit code lên Github, cần tạo message theo đúng chuẩn như sau: 
### a. Cấu trúc chuẩn (1 dòng duy nhất)

```text
<type>[scope]: <mô tả ngắn gọn>

```

*Lưu ý: Bắt buộc phải có một khoảng trắng (dấu cách) sau dấu hai chấm `: `.*

---

### b. Các từ khóa `<type>` cốt lõi (Nên dùng 6 loại này)

Để đơn giản nhất, bạn chỉ cần yêu cầu nhóm nhớ 6 từ khóa sau:

* **`feat`**: Thêm tính năng mới hoặc giao diện mới.
* **`fix`**: Sửa một lỗi (bug).
* **`refactor`**: Cải thiện, sắp xếp lại code (không thêm tính năng, không sửa lỗi).
* **`style`**: Chỉnh sửa liên quan đến format code, UI/CSS (khoảng trắng, dấu phẩy, đổi màu...).
* **`docs`**: Cập nhật tài liệu (như file README.md).
* **`chore`**: Các công việc lặt vặt như cập nhật thư viện, cấu hình build, sửa file `.gitignore`...

Phần `[scope]` (phạm vi) nằm trong ngoặc đơn là **tùy chọn**, dùng để nói rõ file hoặc module nào đang được sửa (ví dụ: `auth`, `ui`, `components`, `api`).

---

### c. Ví dụ thực tế cho nhóm

* **Thêm tính năng:**
  `feat(ui): thêm màn hình danh sách thiết bị`
* **Sửa lỗi:**
  `fix(auth): sửa lỗi ứng dụng bị crash khi đăng nhập sai token`
* **Refactor code:**
  `refactor(components): tách logic xử lý API ra khỏi màn hình chính`
* **Cập nhật cấu hình (Chore):**
  `chore: cập nhật thư viện expo và react-native`
* **Sửa giao diện/Format:**
  `style(header): canh giữa lại tiêu đề và đổi màu nền`

---

### d. Ba quy tắc "bất di bất dịch"

Để nhìn danh sách commit gọn gàng, hãy dặn nhóm tuân thủ đúng 3 điều này:

1. **Viết thường toàn bộ phần đầu:** Các từ khóa như `feat`, `fix` và `scope` phải viết chữ thường.
2. **Mô tả bắt đầu bằng động từ:** (Ví dụ: *thêm*, *sửa*, *xóa*, *cập nhật*...). Viết chữ thường ở đầu câu và **không** để dấu chấm ở cuối câu.
3. **Không quá 72 ký tự:** Chỉ tóm tắt ý chính nhất của lần commit đó.
- Sau khi gộp xong, tạo pull request vào nhánh `dev`.
- Khi tạo pull request, hãy tóm tắt những việc mình đã làm trong đoạn code cần push, sau đó dùng AI Gen cùng với file `.github/pull-request-template.md` để tạo nội dung chi tiết cho pull request.
- Tuyệt đối không trực tiếp push vào nhánh `main`.
- Nếu cần sửa gấp liên quan đến `entity`, `repository`, ... (nói chung là base của dự án), cần liên hệ leader (có thể cần pull gấp code vào nhánh `main` hoặc `dev` để xử lý).

## ⚠️ Lưu ý quan trọng

* **Không bao giờ sửa file Migration cũ:** Nếu file `V1` đã được push lên Git và đồng nghiệp đã chạy, bạn sửa nội dung `V1` sẽ gây lỗi `Checksum mismatch`. Hãy luôn tạo file `V2`, `V3` mới.
* **Quy tắc đặt tên:** Luôn dùng 2 dấu gạch dưới (`__`) sau ký tự phiên bản (Ví dụ: `V2__description.sql`).
* **Gitignore:** Đảm bảo file `.gitignore` đã có thư mục dữ liệu Docker (ví dụ: `postgres_data/` hoặc `pg_data/`) để không đẩy file rác lên Git.

---