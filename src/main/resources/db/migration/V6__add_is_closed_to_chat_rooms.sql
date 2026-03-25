-- Thêm cột is_closed vào bảng chat_rooms, mặc định là false (phòng chưa đóng)
ALTER TABLE chat_rooms
    ADD COLUMN is_closed BOOLEAN NOT NULL DEFAULT FALSE;