package com.unifun.smpp.service.imp;



import com.unifun.smpp.model.Message;
import com.unifun.smpp.service.ClientService;
import com.unifun.smpp.service.messge.service.MessageService;
import com.unifun.smpp.service.messge.service.impl.MessageServiceImp;
import com.unifun.smpp.starter.ClientProperties;
import com.unifun.smpp.repo.MessageRepository;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
//
    private final ClientProperties clientProperties;
    private final MessageRepository messageRepository;
    private SMPPSession smppSession;

    private final MessageService messageService;


    @Override
    public void start() {

        smppSession = initSesionSmppClient();

//        MessageServiceImp messageService = new MessageServiceImp();
        RowMapper<Message> rowMapper = new RowMapper<Message>() {
            @Override
            public Message mapRow(ResultSet resultSet, int row) throws SQLException {
                long id = resultSet.getLong("id");
                String message = resultSet.getString("message");
                return new Message(id, message);
            }
        };
//        String aux = null;
//        List<Messages> listMessages = null;
        new Thread(() -> {
            while(true) {

                try {


//                    System.out.println( (jdbcTemplate.query("SELECT message FROM messages WHERE id = 1", rowMapper)));
//                   sendMessage((jdbcTemplate.query("SELECT message FROM messages WHERE id = 1", rowMapper)).toString());
//                    System.out.println(aux);
                } catch (Exception e)
                {
                    logger.info("ERROR ", e);
                }

                try {


//                    sendMessage(listMessages.toString());
//                    System.out.println(listMessages.toString());
                } catch (Exception e){
                    logger.info("Send Exception ", e);
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.info("error " ,e);
                } catch (Exception a){
                    a.printStackTrace();
                    logger.info("error2 ", a);
                }
            }
//            sendMessage(messageService.getMessage());
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


        smppSession.setMessageReceiverListener(messageReceiverListenerImp);
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

            String messageId = smppSession.submitShortMessage("CMT",
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    "2216",
                    TypeOfNumber.INTERNATIONAL,
                    NumberingPlanIndicator.UNKNOWN,
                    "858176504657",
                    new ESMClass(),
                    (byte)0,
                    (byte)1,
                    timeFormatter.format(new Date()),
                    null,
                    registeredDelivery,
                    (byte)0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT,
                            MessageClass.CLASS1,
                            false),
                    (byte)0,
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
        }  catch (Exception e){
            System.err.println("IO error "+e);
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
