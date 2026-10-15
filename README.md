# CropTalk

React (Vite) frontend in `frontend/data` and a Java Spring Boot backend in `backend/` (migrated from the original Node.js/Express backend).

## Backend setup

Requirements: Java 17+, Maven, a MongoDB database, and a Clerk secret key.

1. Copy `backend/.env.example` to `backend/.env` and fill in the values (`CONN_STRING`, `CLERK_SECRET_KEY`, ...).
2. Start the backend from the `backend/` directory (`mvn spring-boot:run`). The `.env` file is read from the working directory.

| Variable | Purpose | Default |
|---|---|---|
| `CONN_STRING` | MongoDB connection string | required |
| `PORT` | REST API port | `5000` |
| `CLERK_SECRET_KEY` | Clerk Backend API key (`/api/users/login/:uname`) | empty |
| `SOCKET_PORT` | Socket.IO port (Node shared the HTTP port) | `9092` |
| `SOCKET_CORS_ORIGIN` | Origin allowed for Socket.IO | `http://localhost:5173` |

The frontend calls the API at `http://localhost:3000`, so run the backend with `PORT=3000`. The frontend connects to Socket.IO at `VITE_SOCKET_URL` (default `http://localhost:9092`).

## Frontend

`cd frontend/data && npm install && npm run dev`
