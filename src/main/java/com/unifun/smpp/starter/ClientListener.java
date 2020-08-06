package com.unifun.smpp.starter;

import com.unifun.smpp.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientListener {

    private final ClientService clientService;

    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        clientService.start();
    }
    @EventListener(ContextClosedEvent.class)
    public void stop(){
        clientService.stop();
    }
}
