package com.example.remotecamera.config;

import com.example.remotecamera.signal.SignalingHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import java.util.Arrays;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final SignalingHandler handler;
    @Value("${app.allowed-origins:}") private String allowedOrigins;
    public WebSocketConfig(SignalingHandler handler){this.handler=handler;}
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        String[] origins=Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(s->!s.isEmpty()).toArray(String[]::new);
        var registration=registry.addHandler(handler,"/ws/signaling");
        if(origins.length==0) registration.setAllowedOriginPatterns("http://localhost:*","https://localhost:*");
        else registration.setAllowedOrigins(origins);
    }
}
