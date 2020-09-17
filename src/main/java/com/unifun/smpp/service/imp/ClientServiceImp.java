package com.unifun.smpp.service.imp;

import com.unifun.smpp.model.MessageInput;
import com.unifun.smpp.model.ServerConfig;
import com.unifun.smpp.repo.MessageRepository;
import com.unifun.smpp.repo.ServerConfigRepository;
import com.unifun.smpp.service.ClientService;
import com.unifun.smpp.service.messge.MessageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.jsmpp.session.SMPPSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
@Data
@RequiredArgsConstructor
public class ClientServiceImp implements ClientService {

    private static Logger logger = Logger.getLogger(ClientServiceImp.class);
    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClientServiceImp.class);

    private final ServerConfigRepository serverConfigRepository;
    private final MessageRepository messageRepository;
    private SMPPSession smppSession, smppSession2;
    private List<SMPPSession> smppSessionsList = new ArrayList<>();
    private boolean checkSesionStatus = false, checkSesionStatus2 = false;
    private final MessageService messageService;
    private final int TIME_FOR_SEND_ONE_MESSAGE = 2000;
    private final Configuration configuration;
    Timer timer = new Timer();
    String text = "hello";
    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            MessageInput messageInput = messageService.getListMessage();
            try {

//                if(!messageInput.getMessage().isEmpty()) {
//                    smppSessionsList.forEach((smppSession)->
//                        configuration.sendMessage(messageInput.getMessage(), smppSession));

//                }


//                smppSession = smppSessionsList.get(0);
//                    if(!smppSession.getSessionState().isBound()){
//                        List<ServerConfig> serverConfig = serverConfigRepository.findAll();
//                        serverConfig.forEach((config) -> smppSessionsList.add(getPrototypeBean().initSesionSmppClient(config.getName(), config.getHost(), config.getPort(),
//                                config.getSystemId(),config.getPassword(),config.getBindType(),config.getTON(),config.getNPI())));
//                    }
                if (smppSession.getSessionState().isBound()) {
//                    configuration.sendMessage(text, smppSession);
                    configuration.sendMessage(text, smppSession);
                } else {

                    logger.warn("SMPP sesion is inbound");
//                    smppSession = getPrototypeBean().initSesionSmppClient(serverConfigRepository.getOne(0L).getName(),
//                            serverConfigRepository.getOne(0L).getHost(),
//                            serverConfigRepository.getOne(0L).getPort(),
//                            serverConfigRepository.getOne(0L).getSystemId(),
//                            serverConfigRepository.getOne(0L).getPassword(),
//                            serverConfigRepository.getOne(0L).getBindType(),
//                            serverConfigRepository.getOne(0L).getTON(),
//                            serverConfigRepository.getOne(0L).getNPI());
                    List<ServerConfig> serverConfig = serverConfigRepository.findAll();
                    serverConfig.forEach((config) -> smppSession = getPrototypeBean().initSesionSmppClient(
                            config.getName(),
                            config.getHost(),
                            config.getPort(),
                            config.getSystemId(),
                            config.getPassword(),
                            config.getBindType(),
                            config.getTON(),
                            config.getNPI()));
                    Thread.sleep(2000);
                }

            } catch (Exception e) {
                messageInput.setSetSendStatus("REJECTED");
                messageRepository.save(messageInput);
                logger.info("Error ", e);
            }
        }
    };

    TimerTask timerTaskCheckSesion = new TimerTask() {
        @Override
        public void run() {
//            SMPPSession smppSession = null;
//            List<ServerConfig> serverConfig = serverConfigRepository.findAll();
//            for (int i = 0; i < smppSessionsList.size(); i++) {
//                if (!smppSessionsList.get(i).getSessionState().isBound()) {
//                    checkSesionStatus = false;
//                    for (ServerConfig config : serverConfig) {
//                        smppSession = getPrototypeBean().initSesionSmppClient(config.getName(), config.getHost(), config.getPort(),
//                                config.getSystemId(), config.getPassword(), config.getBindType(), config.getTON(), config.getNPI());
//                    }
//                    smppSessionsList.add(i, smppSession);
//                } else checkSesionStatus = true;
//
//
//            }

        }
    };
    TimerTask timerTaskCheckSesion2 = new TimerTask() {
        @Override
        public void run() {
//            if(smppSession2.getSessionState().isBound()) {
//                checkSesionStatus2 = true;
//            } else {
//                checkSesionStatus2 = false;
//                smppSession2 = initSesionSmppClient(serverConfigRepository.getOne((long) 2).getName(),
//                        serverConfigRepository.getOne((long) 2).getHost(),
//                        serverConfigRepository.getOne((long) 2).getPort());
//            }
        }
    };

    @Override
    public void start() {

        System.out.println("Start");
        List<ServerConfig> serverConfig = serverConfigRepository.findAll();
//        serverConfig.forEach((config) -> smppSessionsList.add(getPrototypeBean().initSesionSmppClient(config.getName(), config.getHost(), config.getPort(),
//                config.getSystemId(), config.getPassword(), config.getBindType(), config.getTON(), config.getNPI())));

        serverConfig.forEach((config) -> smppSession = getPrototypeBean().initSesionSmppClient(config.getName(), config.getHost(), config.getPort(),
                config.getSystemId(), config.getPassword(), config.getBindType(), config.getTON(), config.getNPI()));


//        smppSession = initSesionSmppClient(serverConfigRepository.getOne((long) 1).getName(),
//                serverConfigRepository.getOne((long) 1).getHost(),
//                serverConfigRepository.getOne((long) 1).getPort());
//
        timer.scheduleAtFixedRate(timerTaskCheckSesion, 3000, 3000);
//        timer.scheduleAtFixedRate(timerTaskCheckSesion2,0,3000);
        timer.scheduleAtFixedRate(timerTask, 1000, TIME_FOR_SEND_ONE_MESSAGE);

    }

    @Override
    public void stop() {

    }


    @Lookup
    public Configuration getPrototypeBean() {
        return null;
    }
}
