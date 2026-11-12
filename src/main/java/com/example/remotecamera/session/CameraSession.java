package com.example.remotecamera.session;

import java.time.Instant;

public final class CameraSession {
    private final String id, participantToken, viewerToken;
    private final Instant expiresAt;
    CameraSession(String id, String participantToken, String viewerToken, Instant expiresAt) {
        this.id=id; this.participantToken=participantToken; this.viewerToken=viewerToken; this.expiresAt=expiresAt;
    }
    public String id(){return id;} public String participantToken(){return participantToken;}
    public String viewerToken(){return viewerToken;} public Instant expiresAt(){return expiresAt;}
    public boolean expired(Instant now){return !now.isBefore(expiresAt);}
}
