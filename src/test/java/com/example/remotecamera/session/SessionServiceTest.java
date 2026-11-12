package com.example.remotecamera.session;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {
    @Test void createsDistinctRoleTokensAndAuthenticatesThem(){
        SessionService service=new SessionService(); CameraSession s=service.create();
        assertNotEquals(s.participantToken(),s.viewerToken());
        assertEquals("participant",service.role(s.id(),s.participantToken()).orElseThrow());
        assertEquals("viewer",service.role(s.id(),s.viewerToken()).orElseThrow());
        assertTrue(service.role(s.id(),"wrong").isEmpty());
    }
    @Test void removesSession(){SessionService service=new SessionService();CameraSession s=service.create();service.remove(s.id());assertTrue(service.find(s.id()).isEmpty());}
}
