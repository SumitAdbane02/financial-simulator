package com.tradingapp.financialsimulator.service;

import com.tradingapp.financialsimulator.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
/**
 * Service class that implements Spring Security's UserDetailsService interface.
 * This class is responsible for loading user-specific data from the database.
 * It acts as the bridge between our User data model and Spring Security's authentication mechanism.
 */
@Service // Marks this class as a Spring service bean, making it eligible for component scanning and dependency injection.
@RequiredArgsConstructor // A Lombok annotation that generates a constructor with all final fields, enabling clean constructor injection.
public class UserDetailsServiceImpl implements UserDetailsService {

    // We inject the UserRepository to access user data from the database.
    // Using 'final' makes it a required dependency that is initialized in the constructor.
    private final UserRepository userRepository;

    /**
     * Loads a user by their username from the database.
     * This method is called by Spring Security's AuthenticationManager during the authentication process.
     *
     * @param username The username of the user to load.
     * @return A UserDetails object that Spring Security can use for authentication and validation.
     * @throws UsernameNotFoundException if no user is found with the given username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // We use the UserRepository to find a user by their username.
        // The findByUsername method, which we defined in our repository, returns an Optional<User>.
        // This is a safe way to handle a potential absence of a user.
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // The .orElseThrow() method is called on the Optional. If the Optional is empty (user not found),
                // it throws the exception provided by the lambda expression.
                // This is a standard exception that Spring Security expects in this scenario.
                 }
}
