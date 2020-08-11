package com.unifun.smpp.service.imp;

import com.unifun.smpp.repo.ServerConfigRepository;
import com.unifun.smpp.service.ClientService;
import com.unifun.smpp.service.messge.MessageService;
import com.unifun.smpp.starter.ClientProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.log4j.Logger;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.RelativeTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
@Service
@Data
@RequiredArgsConstructor
public class ClientServiceImp implements ClientService {

    private static Logger logger = Logger.getLogger(ClientServiceImp.class);

    private final ServerConfigRepository serverConfigRepository;
    private SMPPSession smppSession;

    private final MessageService messageService;
    private final int TIME_FOR_SEND_ONE_MESSAGE = 1000;
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
//                ArrayList<String> queueMessage = messageService.getListMessage();
                String message = messageService.getListMessage();
                if(!message.isEmpty()) {
                    new Thread(() -> {
                        sendMessage(message);
                    }).start();
                }
            } catch (Exception e) {
                logger.info("Error ", e);
            }
        }
    };

    TimerTask timerTaskInitSesion = new TimerTask() {
        @Override
        public void run() {
            smppSession = initSesionSmppClient(serverConfigRepository.getOne((long) 1).getPort(),
                    serverConfigRepository.getOne((long) 1).getName(), serverConfigRepository.getOne((long) 1).getHost());
        }
    };
    @Override
    public void start() {

        System.out.println("Start");

        timer.scheduleAtFixedRate(timerTaskInitSesion,0,1000);
        timer.scheduleAtFixedRate(timerTask, 1000, TIME_FOR_SEND_ONE_MESSAGE/serverConfigRepository.getOne((long) 1).getTpc());

    }

    @Override
    public void stop() {

    }

    private SMPPSession initSesionSmppClient(int port, String name, String host) {
        SMPPSession smppSession = new SMPPSession();
        smppSession.setPduProcessorDegree(1);

        MessageReceiverListenerImp messageReceiverListenerImp = new MessageReceiverListenerImp();
        smppSession.setMessageReceiverListener(messageReceiverListenerImp);
        try {
            String systemId = smppSession.connectAndBind(
                    host,
                    port,
                    new BindParameter(
                            BindType.BIND_TRX,
                            name,
                            "",
                            "cp",
                            TypeOfNumber.UNKNOWN,
                            NumberingPlanIndicator.UNKNOWN,
                            null
                    )
            );
            smppSession.addSessionStateListener(new StateSessionListenerImp());


            logger.info("Connected with system id: " + systemId);
        } catch (IOException e) {
            logger.info("I/O error occurred " + e);

        }
        return smppSession;
    }

    private void sendMessage(String message) {
        try {
            // set RegisteredDelivery
            RegisteredDelivery registeredDelivery = new RegisteredDelivery();
            registeredDelivery.setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE);
            TimeFormatter timeFormatter = new RelativeTimeFormatter();

            String messageId = smppSession.submitShortMessage("CMT",
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    "2216",
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    "858176504657",
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    timeFormatter.format(new Date()),
                    null,
                    registeredDelivery,
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT,
                            MessageClass.CLASS1,
                            false),
                    (byte) 0,
                    message.getBytes());

            System.out.println("Message submitted, message_id is " + messageId);

        } catch (PDUException e) {
            // Invalid PDU parameter
            System.err.println("Invalid PDU parameter");
            e.printStackTrace();
        } catch (ResponseTimeoutException e) {
            // Response timeout
            System.err.println("Response timeout");
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            // Invalid response
            System.err.println("Receive invalid respose");
            e.printStackTrace();
        } catch (NegativeResponseException e) {
            // Receiving negative response (non-zero command_status)
            System.err.println("Receive negative response");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error occur");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error " + e);
            e.printStackTrace();
        }
    }
}
