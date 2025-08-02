package com.banklab.security.oauth2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@PropertySource({"classpath:/application.properties"})
public class OAuth2ClientConfig {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.authorization-uri}")
    private String authorizationUri;

    @Value("${kakao.token-uri}")
    private String tokenUri;

    @Value("${kakao.user-info-uri}")
    private String userInfoUri;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {

        ClientRegistration kakaoRegistration = ClientRegistration.withRegistrationId("kakao")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userInfoUri(userInfoUri)
                .userNameAttributeName("id")
                .clientName("Kakao")
                .scope("account_email", "name", "gender", "birthday", "birthyear", "phone_number")
                .redirectUri(redirectUri)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();

        return new InMemoryClientRegistrationRepository(kakaoRegistration);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }
}
