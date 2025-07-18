package com.banklab.security.util;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
@PropertySource({"classpath:/application.properties"})
public class JwtKeyManager {

    @Value("${jwt.secret-key:default-fallback-key-for-development}")
    private String secretKeyString;

    @Getter
    private Key key;

    @PostConstruct
    public void initKey() {
        if (secretKeyString.length() < 32) {
            throw new IllegalArgumentException("JWT Secret Key는 최소 32자 이상이어야 합니다.");
        }

        this.key = Keys.hmacShaKeyFor(
                secretKeyString.getBytes(StandardCharsets.UTF_8)
        );
    }

}
