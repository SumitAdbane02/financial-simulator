package com.tradingapp.financialsimulator.controller;

import com.tradingapp.financialsimulator.dto.Greeting;
import com.tradingapp.financialsimulator.dto.HelloMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.tags.HtmlEscapeTag;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {

    @MessageMapping("/hello") // listens for message on the destination
    @SendTo("/topic/greetings")  // Broadcaste the return value to all subscribution
    public Greeting greeting(HelloMessage message) throws InterruptedException{

        Thread.sleep(1000);
        // senitize user inputs before echoing it back to prevent xss attacks
        String sanitizeName= HtmlUtils.htmlEscape(message.getName());
        return new Greeting("Hello ,"+sanitizeName+" !");

    }
}
