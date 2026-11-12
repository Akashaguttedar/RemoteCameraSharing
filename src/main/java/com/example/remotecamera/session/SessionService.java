package com.example.remotecamera.session;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private static final Duration TTL = Duration.ofHours(1);
    private final Map<String, CameraSession> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    public CameraSession create() {
        String id=UUID.randomUUID().toString();
        CameraSession s=new CameraSession(id, token(), token(), Instant.now().plus(TTL));
        sessions.put(id,s); return s;
    }
    private String token(){byte[] bytes=new byte[32];random.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
    public Optional<CameraSession> find(String id){
        if(id==null || id.length()>36) return Optional.empty();
        CameraSession s=sessions.get(id);
        if(s!=null && s.expired(Instant.now())){sessions.remove(id,s);return Optional.empty();}
        return Optional.ofNullable(s);
    }
    public Optional<CameraSession> authenticate(String id,String token){return find(id).filter(s -> token!=null && (constantTime(s.participantToken(),token)||constantTime(s.viewerToken(),token)));}
    public Optional<String> role(String id,String token){return authenticate(id,token).map(s -> constantTime(s.participantToken(),token)?"participant":"viewer");}
    private boolean constantTime(String a,String b){return java.security.MessageDigest.isEqual(a.getBytes(java.nio.charset.StandardCharsets.UTF_8),b.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    public void remove(String id){sessions.remove(id);}
    public void removeExpired(){Instant now=Instant.now();sessions.entrySet().removeIf(e->e.getValue().expired(now));}
    public int size(){return sessions.size();}
}
