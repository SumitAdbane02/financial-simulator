package com.tradingapp.financialsimulator.config;

import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

public class WebsocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    protected void configureInbound(MessageSecurityMetadataSourceRegistry message){
        message
                // Our JwtAuthChannelInterceptor handles this authentication
                .simpTypeMatchers(SimpMessageType.CONNECT).authenticated()
                // Subscribing to user-specific queue requires authentication
                .simpDestMatchers("/user/**").authenticated()

                // sending message to our @MessageMapping controller require authetication
                .simpDestMatchers("/app/**").authenticated()
                 // for all subscriptions
                .simpTypeMatchers(SimpMessageType.SUBSCRIBE,SimpMessageType.MESSAGE).authenticated()
                // Any other message type is denied by default for security
                .anyMessage().denyAll();

    }

    protected boolean sameOriginDisabled(){
        return true; 
    }
}
