package com.example.chat_app.service;

import com.example.chat_app.model.dto.AuthenticationResponse;
import com.example.chat_app.model.dto.LoginRequest;
import com.example.chat_app.model.dto.RegisterRequest;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.UserRepository;
import com.example.chat_app.strategies.AuthenticationStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private UserRepository userRepo;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private List<AuthenticationStrategy> strategies;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder,
                       JwtService jwtService, List<AuthenticationStrategy> strategies) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.strategies = strategies;
    }

    public AuthenticationResponse register(RegisterRequest request){
        if(userRepo.existsByUsername(request.username())){
            throw new RuntimeException("Username already taken!");
        }

        User user = new User();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepo.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse Login(LoginRequest request){
        return strategies.stream()
                .filter(strategy -> strategy.supports("JWT"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Authentication method not supported"))
                .authenticate(request);
    }
}
