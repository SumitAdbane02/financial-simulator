package com.tradingapp.financialsimulator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // register of STOMP endpoint
     public void registerStompEndpoints(StompEndpointRegistry registry){

         registry.addEndpoint("/ws").withSockJS();
     }


     // register of configuring the message broker
     public void configureMessageBroker(MessageBrokerRegistry registry){

         registry.setApplicationDestinationPrefixes("/app");


         registry.enableSimpleBroker("/topic","/queue");
     }


}
