package com.unifun.smpp.service.messge.impl;

import com.unifun.smpp.model.MessageInput;
import com.unifun.smpp.repo.MessageRepository;
import com.unifun.smpp.service.messge.MessageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class MessageServiceImp implements MessageService {

    private static final int MAX_MESSAGE_RECEIVER = 3;
    private final MessageRepository messageRepository;
    private static Logger logger = Logger.getLogger(MessageServiceImp.class);

    Queue<MessageInput> queue = new ConcurrentLinkedQueue();
    private String message;

    @Override
    public int httpStatus() {
        if (message.isEmpty()) return HttpStatus.BAD_REQUEST.value();
        else {
            return HttpStatus.OK.value();
        }
    }

    /**
     * @param message message is message
     */
    @Override
    public void setMessageBdAndQueue(String message) {
        setMessage(message);
        if (!message.equals("")) {
            try {
                MessageInput messageInput = new MessageInput();
                messageInput.setMessage(message);
                messageRepository.save(messageInput);
                queue.add(messageInput);
                logger.info("Insertion was with successful");
            } catch (Exception e) {
                logger.info("Error exception ", e);
            }
        }
    }

    public List<MessageInput> getListBd(){

        return messageRepository.findAll();

    }

    public ArrayList<String> getListMessage() {

        ArrayList<String> listMessage = new ArrayList<>();
        int count = 0;
        for(MessageInput message : queue){
            listMessage.add(message.getMessage());
            message.setStatusIO(1);
            messageRepository.save(message);
            queue.remove();
            count++;
            if(count >= MAX_MESSAGE_RECEIVER) break;
        }
        return listMessage;
    }

    public String responsHttpMessage(){
        if (message.isEmpty()) return "message is emty";
        else {
            return "Insertion was with successful";
        }
    }

}