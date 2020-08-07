package com.unifun.smpp.service.messge.service.impl;

import com.unifun.smpp.model.Message;
import com.unifun.smpp.repo.MessageRepository;
import com.unifun.smpp.service.messge.service.MessageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class MessageServiceImp implements MessageService {

    private final MessageRepository messageRepository;
    private static Logger logger = Logger.getLogger(MessageServiceImp.class);

    Queue queue = new ConcurrentLinkedQueue();

    private String message;

    @Override
    public int httpStatus() {
        if (message.isEmpty()) return HttpStatus.BAD_REQUEST.value();
        else {
            return HttpStatus.OK.value();
        }
    }

    /**
     * @param message
     */
    @Override
    public void setMessageBd(String message) {
        setMessage(message);
        if (!message.equals("")) {
            try {
                Message message1 = new Message();
                message1.setMessage(message);
                messageRepository.save(message1);
                queue.add(message1);
                logger.info("Insertion was with successful");
            } catch (Exception e) {
                logger.info("Error exception ", e);
            }
        }
    }

    public List<Message> getListBd(){

        return messageRepository.findAll();
    }



    public void getMessageBd() throws IOException {
//
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode jsonNode = mapper.readTree((JsonParser) messageRepository.findAll());
//
//        jsonNode.
    }
}
