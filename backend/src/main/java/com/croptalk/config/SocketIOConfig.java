package com.croptalk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.corundumstudio.socketio.SocketIOServer;

/** Port of `new Server(httpServer, { cors: { origin: "http://localhost:5173", methods: ["GET","POST"] } })`. */
@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer(@Value("${socketio.port}") int port,
                                         @Value("${socketio.origin}") String origin) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setPort(port);
        config.setOrigin(origin);
        return new SocketIOServer(config);
    }
}
