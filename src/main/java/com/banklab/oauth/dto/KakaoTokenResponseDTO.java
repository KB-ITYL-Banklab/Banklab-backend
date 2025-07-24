package com.banklab.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoTokenResponseDTO {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private int refreshTokenExpiresIn;
}
