package com.example.remotecamera.session;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component public class ExpiryJob { private final SessionService sessions; public ExpiryJob(SessionService sessions){this.sessions=sessions;} @Scheduled(fixedDelay=60000) public void cleanup(){sessions.removeExpired();} }
