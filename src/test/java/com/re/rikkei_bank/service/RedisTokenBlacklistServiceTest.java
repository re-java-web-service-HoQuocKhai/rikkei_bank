package com.re.rikkei_bank.service;

import com.re.rikkei_bank.service.impl.RedisTokenBlacklistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisTokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisTokenBlacklistServiceImpl redisTokenBlacklistService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void saveTokenToBlacklist_Success() {
        String token = "test-token";
        long expirationMs = 3600000;
        String expectedKey = "jwt_blacklist:" + token;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisTokenBlacklistService.saveTokenToBlacklist(token, expirationMs);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(expectedKey, "blacklisted", expirationMs, TimeUnit.MILLISECONDS);
    }

    @Test
    void saveTokenToBlacklist_NegativeExpiration() {
        String token = "test-token";
        long expirationMs = -1000;

        redisTokenBlacklistService.saveTokenToBlacklist(token, expirationMs);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isTokenBlacklisted_True() {
        String token = "test-token";
        String expectedKey = "jwt_blacklist:" + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

        boolean result = redisTokenBlacklistService.isTokenBlacklisted(token);

        assertTrue(result);
        verify(redisTemplate).hasKey(expectedKey);
    }

    @Test
    void isTokenBlacklisted_False() {
        String token = "test-token";
        String expectedKey = "jwt_blacklist:" + token;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

        boolean result = redisTokenBlacklistService.isTokenBlacklisted(token);

        assertFalse(result);
        verify(redisTemplate).hasKey(expectedKey);
    }
}
