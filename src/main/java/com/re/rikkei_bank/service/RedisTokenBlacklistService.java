package com.re.rikkei_bank.service;

public interface RedisTokenBlacklistService {
    void saveTokenToBlacklist(String token, long expirationMs);
    boolean isTokenBlacklisted(String token);
}
