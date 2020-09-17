package com.unifun.smpp.service.imp;


import org.apache.log4j.Logger;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.RelativeTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Configuration {

    private static Logger logger = Logger.getLogger(ClientServiceImp.class);
    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ClientServiceImp.class);


    public SMPPSession initSesionSmppClient(String name, String host, int port,String systemId,String password,String bindType,String ton,String npi) {
        SMPPSession smppSession = new SMPPSession();
        smppSession.setPduProcessorDegree(1);

        MessageReceiverListenerImp messageReceiverListenerImp = new MessageReceiverListenerImp();
        smppSession.setMessageReceiverListener(messageReceiverListenerImp);
        try {
            smppSession.connectAndBind(
                    host,
                    port,
                    BindType.valueOf(bindType),
                    systemId,
                    password,
                    "sns",
                    TypeOfNumber.valueOf(ton),
                    NumberingPlanIndicator.valueOf(npi),
                    "",
                    2000
                    ) ;
            smppSession.addSessionStateListener(new StateSessionListenerImp());


            logger.info("Connected with system id: " );
        } catch (IOException e) {
            LOGGER.info("Bind failed on socket {}:{}. Try again... {}", host, port, e.getMessage());
        }
        return smppSession;
    }

    public void sendMessage(String message, SMPPSession smppSession) {
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
