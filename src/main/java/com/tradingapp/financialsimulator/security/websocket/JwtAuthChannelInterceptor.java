package com.tradingapp.financialsimulator.security.websocket;

import com.tradingapp.financialsimulator.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;


    public Message<?> preSend(Message<?> message, MessageChannel messageChannel) {
        // StompHeaderAccessor allows us to access the STOMP command and header
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // we are only interested in the CONNECT Command
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            //The client should send the JWT in an Authorization" Header

            List<String> authorization = accessor.getNativeHeader("Authorization");


            if (authorization != null && !authorization.isEmpty()) {
                String authHeader = authorization.get(0);
                String jwt = null;

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwt = authHeader.substring(7);

                }
                if (jwt != null && jwtService.validateToken(jwt)) {
                    //If the token is valid,extract the username
                    String username = jwtService.extractUsername(jwt);

                    //Load the user details from the database
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    //Create a standard Spring security authentication token
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null,  // credentials are null for JWT-based auth
                            userDetails.getAuthorities()
                    );

                    accessor.setUser(authenticationToken);
                }

            }
            // Extract the token from the Bearer <token> format

        }
        return message;

    }
}