# FE Chat Integration Sheet

Base URL REST: `/api/v1/chat`
WebSocket endpoint: `/ws-chat`
Topic realtime: `/topic/chat/rooms/{roomId}`

## 1) Message Types
- `TEXT`
- `IMAGE`
- `PRODUCT_LINK`

## 2) Common Headers
- `X-User-Id: <currentUserId>`
- `Authorization: Bearer <token>` (n?u FE dang g?i)

## 3) Customer Flow

### B1. L?y ho?c t?o phòng active c?a customer
`GET /customer/rooms/active`

Response `200`:
```json
{
  "id": "room-123",
  "customerId": "user-1",
  "staffId": null,
  "closed": false,
  "createdAt": "2026-05-11T12:00:00",
  "updatedAt": "2026-05-11T12:00:00"
}
```

Alternative create (n?u mu?n explicit):
`POST /customer/rooms` -> `201` cùng schema trên.

### B2. Sub realtime
- Connect STOMP t?i `/ws-chat`
- Subscribe: `/topic/chat/rooms/{roomId}`

### B3. Customer g?i tin nh?n
`POST /customer/messages`

Request:
```json
{
  "roomId": "room-123",
  "type": "TEXT",
  "content": "Xin chao shop"
}
```

Response `201` (`MessageDTO`):
```json
{
  "id": "msg-1",
  "roomId": "room-123",
  "senderId": "user-1",
  "type": "TEXT",
  "content": "Xin chao shop",
  "imageUrl": null,
  "linkUrl": null,
  "productId": null,
  "variantId": null,
  "productName": null,
  "productImageUrl": null,
  "productPrice": null,
  "sentAt": "2026-05-11T12:01:00"
}
```

### B4. Load l?ch s? chat
`GET /rooms/{roomId}/messages`

Response `200`: `MessageDTO[]` (oldest -> newest).

## 4) Staff/Admin Chat Flow

### B1. L?y danh sách phòng active
`GET /staff/rooms/active`

Response `200`: `ChatRoomDTO[]`.

### B2. Nh?n phòng
`PATCH /staff/rooms/{roomId}/assign`
Header: `X-User-Id=<staffId>`

Response `200`: `ChatRoomDTO` (có `staffId` dã set).

### B3. Staff reply
`POST /staff/rooms/{roomId}/messages`
Header: `X-User-Id=<staffId>`

Request m?u TEXT:
```json
{
  "type": "TEXT",
  "content": "Shop da nhan duoc yeu cau"
}
```

Response `201`: `MessageDTO`.

## 5) Generic send endpoint (fallback)
`POST /rooms/{roomId}/messages`
- Dùng khi FE mu?n endpoint chung.
- V?n ki?m tra quy?n sender theo room.

## 6) Payload rules theo type

### `TEXT`
B?t bu?c:
- `content` khác r?ng

### `IMAGE`
B?t bu?c:
- `imageUrl`

### `PRODUCT_LINK`
B?t bu?c:
- `linkUrl`
- `productName`

Khuy?n ngh? g?i thêm:
- `productId`, `variantId`, `productImageUrl`, `productPrice`

## 7) Error mapping cho FE

### `400` invalid payload
```json
{ "error": "Noi dung tin nhan khong duoc de trong." }
```

### `403` forbidden/inactive/room closed
```json
{ "error": "Nguoi dung ... khong co quyen thao tac voi phong chat: ..." }
```
ho?c
```json
{ "error": "Phong chat da ket thuc: ..." }
```

### `404` room not found
```json
{ "error": "Khong tim thay phong chat voi ID: ..." }
```

## 8) Realtime behavior
- M?i l?n g?i thành công, BE publish event lên:
  - `/topic/chat/rooms/{roomId}`
- Payload realtime chính là `MessageDTO`.

## 9) FE integration checklist
- Vào chat: g?i `GET /customer/rooms/active` (ho?c staff active rooms).
- Subscribe topic theo `roomId` tru?c khi g?i.
- G?i REST (`/customer/messages` ho?c `/staff/rooms/{roomId}/messages`).
- Khi nh?n event realtime: append ngay vào UI.
- Khi reload trang: g?i `GET /rooms/{roomId}/messages` d? hydrate l?ch s?.
