package com.unifun.smpp.controller;

import com.unifun.smpp.model.Message;
import com.unifun.smpp.service.messge.service.MessageService;
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
    List<Message> getMessage(@RequestParam(defaultValue = "") String message, HttpServletResponse response) throws IOException {


        messageService.setMessageBd(message);
        response.setStatus(messageService.httpStatus());
//        response.getWriter().write("Http message id "+messageService.httpStatus());
        return messageService.getListBd();

    }
}

