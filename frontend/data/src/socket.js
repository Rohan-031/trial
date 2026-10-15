// src/socket.js
import { io } from "socket.io-client";

// The Spring Boot backend serves Socket.IO on its own port (SOCKET_PORT, default 9092).
const socket = io(import.meta.env.VITE_SOCKET_URL || "http://localhost:9092");
export default socket;
