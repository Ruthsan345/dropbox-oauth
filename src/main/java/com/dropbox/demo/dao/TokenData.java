package com.dropbox.demo.dao;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenData {
    String accessToken;
    String refreshToken;
    long expiryTime;

   public boolean isExpired() {
        return System.currentTimeMillis() >= (expiryTime - 60000);
    }
}
