package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
@ConfigurationProperties(prefix = "app.external-calls")
public class RestTemplateConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);
    
    private boolean trustAllCertificates = false;
    
    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }
    
    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        if (trustAllCertificates) {
            logger.warn("SSL certificate validation is disabled. This should only be used in development/testing environments.");
            return createTrustAllRestTemplate(builder);
        } else {
            return builder.build();
        }
    }
    
    private RestTemplate createTrustAllRestTemplate(RestTemplateBuilder builder) {
        try {
            // Create a trust manager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            
            // Create SSL context with the trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            
            // Create SSL socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            
            // Create hostname verifier that accepts all hostnames
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            
            // Build RestTemplate with SSL configuration and timeouts
            return builder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .requestFactory(() -> {
                    try {
                        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
                        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                    } catch (Exception e) {
                        logger.warn("Could not configure SSL trust for RestTemplate", e);
                    }
                    return new org.springframework.http.client.SimpleClientHttpRequestFactory();
                })
                .build();
                
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.warn("Could not configure SSL trust, using default RestTemplate", e);
            return builder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .build();
        }
    }
}
