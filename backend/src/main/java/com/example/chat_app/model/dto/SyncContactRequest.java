package com.example.chat_app.model.dto;

import java.util.List;

public record SyncContactRequest(
        List<String>  phoneNumbers
) {}
