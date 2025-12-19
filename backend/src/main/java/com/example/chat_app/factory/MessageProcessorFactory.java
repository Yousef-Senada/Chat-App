package com.example.chat_app.factory;

import com.example.chat_app.exceptions.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessorFactory {

    public MessageProcessor getProcessor(String type) {
        if (type == null) throw new AppException("Message type is null", HttpStatus.BAD_REQUEST);

        return switch (type.toUpperCase()) {
            case "TEXT" -> new TextMessageProcessor();
            case "IMAGE" -> new ImageMessageProcessor();
            case "VIDEO" -> new VideoMessageProcessor(); 
            case "VOICE" -> new AudioMessageProcessor();
            default -> throw new AppException("Unsupported message type: " + type, HttpStatus.BAD_REQUEST);
        };
    }

}
