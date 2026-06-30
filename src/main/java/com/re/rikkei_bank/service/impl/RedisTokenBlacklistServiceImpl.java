package com.re.rikkei_bank.service.impl;

import com.re.rikkei_bank.service.RedisTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistServiceImpl implements RedisTokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt_blacklist:";

    @Override
    public void saveTokenToBlacklist(String token, long expirationMs) {
        if (expirationMs > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", expirationMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
