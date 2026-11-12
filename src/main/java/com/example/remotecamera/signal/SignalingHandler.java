package com.example.remotecamera.signal;

import com.example.remotecamera.session.SessionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {
    private static final int MAX_MESSAGE=65_536;
    private final SessionService sessions; private final ObjectMapper mapper=new ObjectMapper();
    public SignalingHandler(SessionService sessions){this.sessions=sessions;}
    @Override public void afterConnectionEstablished(WebSocketSession socket)throws Exception{
        Map<String,String> q=query(socket.getUri()); String id=q.get("session"), token=q.get("token");
        var role=sessions.role(id,token);
        if(role.isEmpty()){socket.close(CloseStatus.POLICY_VIOLATION);return;}
        String key=id+":"+role.get();
        WebSocketSession previous=connectedSockets.get(key);
        if(previous!=null&&previous.isOpen()) previous.close(CloseStatus.NORMAL);
        connectedSockets.put(key,socket);
        socket.getAttributes().put("peerKey",key); socket.getAttributes().put("session",id); socket.getAttributes().put("role",role.get());
        socket.getAttributes().put("socket",socket);
        socket.sendMessage(new TextMessage("{\"type\":\"ready\"}"));
        String otherKey=id+":"+(role.get().equals("participant")?"viewer":"participant");
        WebSocketSession other=connectedSockets.get(otherKey);
        if(other!=null&&other.isOpen()){
            socket.sendMessage(new TextMessage("{\"type\":\"peer-ready\"}"));
            other.sendMessage(new TextMessage("{\"type\":\"peer-ready\"}"));
        }
    }
    @Override protected void handleTextMessage(WebSocketSession sender,TextMessage message)throws Exception{
        if(message.getPayloadLength()>MAX_MESSAGE){sender.close(CloseStatus.TOO_BIG_TO_PROCESS);return;}
        String id=(String)sender.getAttributes().get("session"), role=(String)sender.getAttributes().get("role");
        if(!sessions.find(id).isPresent()){sender.close(CloseStatus.POLICY_VIOLATION);return;}
        JsonNode json=mapper.readTree(message.getPayload()); String type=json.path("type").asText();
        if(!type.equals("offer")&&!type.equals("answer")&&!type.equals("ice")){sender.close(CloseStatus.POLICY_VIOLATION);return;}
        if((role.equals("participant")&&!type.equals("answer")&&!type.equals("ice"))||(role.equals("viewer")&&!type.equals("offer")&&!type.equals("ice"))){sender.close(CloseStatus.POLICY_VIOLATION);return;}
        String otherKey=id+":"+(role.equals("participant")?"viewer":"participant");
        WebSocketSession target=connectedSockets.get(otherKey);
        if(target!=null&&target.isOpen()) target.sendMessage(new TextMessage(message.getPayload()));
    }
    private final Map<String,WebSocketSession> connectedSockets=new ConcurrentHashMap<>();
    @Override public void afterConnectionClosed(WebSocketSession socket,CloseStatus status){Object key=socket.getAttributes().get("peerKey");if(key!=null)connectedSockets.remove((String)key,socket);}
    private Map<String,String> query(URI uri){Map<String,String> map=new java.util.HashMap<>();if(uri==null||uri.getRawQuery()==null)return map;for(String item:uri.getRawQuery().split("&")){String[] p=item.split("=",2);if(p.length==2)map.put(java.net.URLDecoder.decode(p[0],java.nio.charset.StandardCharsets.UTF_8),java.net.URLDecoder.decode(p[1],java.nio.charset.StandardCharsets.UTF_8));}return map;}
}
