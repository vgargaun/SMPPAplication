package com.unifun.smpp.service.imp;

import com.unifun.smpp.model.MessageInput;
import com.unifun.smpp.repo.MessageRepository;
import com.unifun.smpp.repo.ServerConfigRepository;
import com.unifun.smpp.service.ClientService;
import com.unifun.smpp.service.messge.MessageService;
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
import org.slf4j.LoggerFactory;
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
    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClientServiceImp.class);

    private final ServerConfigRepository serverConfigRepository;
    private final MessageRepository messageRepository;
    private SMPPSession smppSession;
    private boolean checkSesionStatus = false;
    private final MessageService messageService;
    private final int TIME_FOR_SEND_ONE_MESSAGE = 1000;
    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            MessageInput messageInput = messageService.getListMessage();
            try {
                if(!messageInput.getMessage().isEmpty()&&checkSesionStatus) {
                    new Thread(() -> {
                        sendMessage(messageInput.getMessage());
                        messageInput.setSetSendStatus("SEND");
                        messageRepository.save(messageInput);
                    }).start();
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
            if(smppSession.getSessionState().isBound()) {
                checkSesionStatus = true;
            } else {
                checkSesionStatus = false;
                smppSession = initSesionSmppClient();
            }

        }
    };
    @Override
    public void start() {

        System.out.println("Start");
        smppSession = initSesionSmppClient();
        timer.scheduleAtFixedRate(timerTaskCheckSesion,0,3000);
        timer.scheduleAtFixedRate(timerTask, 1000, TIME_FOR_SEND_ONE_MESSAGE/serverConfigRepository.getOne((long) 1).getTpc());

    }

    @Override
    public void stop() {

    }

    private SMPPSession initSesionSmppClient() {
        SMPPSession smppSession = new SMPPSession();
        smppSession.setPduProcessorDegree(1);

        MessageReceiverListenerImp messageReceiverListenerImp = new MessageReceiverListenerImp();
        smppSession.setMessageReceiverListener(messageReceiverListenerImp);
        try {
            String systemId = smppSession.connectAndBind(
                    serverConfigRepository.getOne((long) 1).getHost(),
                    serverConfigRepository.getOne((long) 1).getPort(),
                    new BindParameter(
                            BindType.BIND_TRX,
                            serverConfigRepository.getOne((long) 1).getName(),
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
            LOGGER.info("Bind failed on socket {}:{}. Try again... {}", serverConfigRepository.getOne((long) 1).getHost(), serverConfigRepository.getOne((long) 1).getPort(), e.getMessage());
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
