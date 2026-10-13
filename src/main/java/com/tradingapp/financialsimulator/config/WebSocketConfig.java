package com.tradingapp.financialsimulator.config;

import com.tradingapp.financialsimulator.model.OrderType;
import com.tradingapp.financialsimulator.security.websocket.JwtAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE+99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    //Inject Custom interceptor
    private final JwtAuthChannelInterceptor jwtAuthChannelInterceptor;

    // register of STOMP endpoint
     public void registerStompEndpoints(StompEndpointRegistry registry){

         registry.addEndpoint("/ws").withSockJS();
     }


     // register of configuring the message broker
     public void configureMessageBroker(MessageBrokerRegistry registry){

         registry.setApplicationDestinationPrefixes("/app");


         registry.enableSimpleBroker("/topic","/queue");
     }

     public void configureClientInboundChannel(ChannelRegistration registration){
         // the interceptor will be called for every message coming from a client.
         registration.interceptors(jwtAuthChannelInterceptor);
     }

}
