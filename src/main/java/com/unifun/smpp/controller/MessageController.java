package com.unifun.smpp.controller;

import com.unifun.smpp.model.MessageInput;
import com.unifun.smpp.service.messge.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping(path = "/")
public class MessageController {

    @Autowired
    MessageService messageService;


    @GetMapping("/message")
    public @ResponseBody
    String getMessage(@RequestParam(defaultValue = "") String message, HttpServletResponse response) throws IOException {

        messageService.setMessageBdAndQueue(message);
        response.setStatus(messageService.httpStatus());
        return messageService.responsHttpMessage();

    }


    @GetMapping("/list")
    public @ResponseBody
    List<MessageInput> getList(){

        return messageService.getListBd();
    }
}