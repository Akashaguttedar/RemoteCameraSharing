package com.example.remotecamera.session;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService service;
    public SessionController(SessionService service){this.service=service;}
    public record CreateResponse(String sessionId,String participantUrl,String viewerUrl,Instant expiresAt){}
    @PostMapping public CreateResponse create(HttpServletRequest request){
        CameraSession s=service.create(); String base=request.getScheme()+"://"+request.getServerName()+((request.getServerPort()==80||request.getServerPort()==443)?"":":"+request.getServerPort());
        return new CreateResponse(s.id(),base+"/participant.html?session="+s.id()+"&token="+s.participantToken(),base+"/viewer.html?session="+s.id()+"&token="+s.viewerToken(),s.expiresAt());
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,@RequestParam String token){
        CameraSession s=service.find(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(!service.authenticate(id,token).isPresent() || !java.security.MessageDigest.isEqual(s.viewerToken().getBytes(),token.getBytes())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        service.remove(id);
    }
}
