package com.unifun.smpp.service.messge;

import com.unifun.smpp.model.MessageInput;

import java.util.List;

public interface MessageService {
    void setMessage(String message);
    String getMessage();
    int httpStatus();
    void setMessageBdAndQueue(String message);
    List<MessageInput> getListBd();
    MessageInput getListMessage();
    String responsHttpMessage();
}
