package com.unifun.smpp.service.imp;



import com.unifun.smpp.service.ClientService;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Data
@RequiredArgsConstructor
public class ClientServiceImp implements ClientService {

    private static Logger logger = Logger.getLogger(ClientServiceImp.class);

    private final ClientProperties clientProperties;
    SMPPSession smppSession;

    @Override
    public void start() {

        smppSession = initSesionSmppClient();

        new Thread(() -> {
            while(true) {
                sendMessage("Hello Veaceslav!");


                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void stop() {

    }

    private SMPPSession initSesionSmppClient()
    {
        SMPPSession smppSession = new SMPPSession();
        smppSession.setPduProcessorDegree(1);

        MessageReceiverListenerImp messageReceiverListenerImp = new MessageReceiverListenerImp();

        try{
            String systemId = smppSession.connectAndBind(
                clientProperties.getHost(),
                    clientProperties.getPort(),
                    new BindParameter(
                            BindType.BIND_TRX,
                            clientProperties.getName(),
                            "",
                            "cp",
                            TypeOfNumber.UNKNOWN,
                            NumberingPlanIndicator.UNKNOWN,
                            null
                    )
            );
            smppSession.addSessionStateListener(new StateSessionListenerImp());


            logger.info("Connected with system id: " + systemId);
        } catch (IOException e){
            logger.info("I/O error occurred "+e);

        }
        return smppSession;
    }

    private void sendMessage(String message){
        try {
            // set RegisteredDelivery
            RegisteredDelivery registeredDelivery = new RegisteredDelivery();
            registeredDelivery.setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE);
            TimeFormatter timeFormatter = new RelativeTimeFormatter();

            String messageId = smppSession.submitShortMessage("CMT", TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN, "2216", TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN,
                    "858176504657", new ESMClass(), (byte)0, (byte)1, timeFormatter.format(new Date()), null,
                    registeredDelivery, (byte)0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1,
                            false), (byte)0, message.getBytes());

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
        }
    }

    private ScheduledExecutorService createMonitor() {
        return Executors.newScheduledThreadPool(10000, new ThreadFactory() {
            private AtomicInteger sequence = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("CHSmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
                return t;
            }
        });
    }
}
