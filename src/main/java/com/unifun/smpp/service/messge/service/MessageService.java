package com.unifun.smpp.service.messge.service;

import com.unifun.smpp.model.Message;

import java.util.List;

public interface MessageService {
    void setMessage(String message);
    String getMessage();
    int httpStatus();
    void setMessageBd(String message);
    List<Message> getListBd();


}
