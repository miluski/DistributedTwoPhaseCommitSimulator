package com.distributed2pc.coordinator.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import reactor.netty.http.client.HttpClient;

/**
 * Application-level bean configuration for WebClient and Jackson.
 */
@Configuration
public class AppConfig {

    /**
     * Provides a {@link WebClient.Builder} configured with a custom SSL context
     * that trusts the shared participant certificate while disabling hostname
     * verification for intra-Docker communication.
     *
     * @return pre-configured WebClient.Builder
     * @throws IllegalStateException if the SSL context cannot be initialised
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        try {
            HttpClient httpClient = buildSslHttpClient();
            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient));
        } catch (SSLException e) {
            throw new IllegalStateException("Failed to configure WebClient SSL context", e);
        }
    }

    /**
     * Jackson ObjectMapper with JSR-310 (Java 8 date/time) support.
     *
     * @return configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private HttpClient buildSslHttpClient() throws SSLException {
        String trustStorePath = System.getProperty("javax.net.ssl.trustStore");
        String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword", "");
        TrustManagerFactory tmf = loadTrustManagerFactory(trustStorePath, trustStorePassword);
        SslContext sslContext = SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK)
                .trustManager(tmf)
                .build();
        return HttpClient.create()
                .secure(spec -> spec
                        .sslContext(sslContext)
                        .handlerConfigurator(this::disableHostnameVerification));
    }

    private TrustManagerFactory loadTrustManagerFactory(String path, String password) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (path != null && !path.isBlank()) {
                KeyStore ts = KeyStore.getInstance("PKCS12");
                try (InputStream in = new FileInputStream(path)) {
                    ts.load(in, password.toCharArray());
                }
                tmf.init(ts);
            } else {
                tmf.init((KeyStore) null);
            }
            return tmf;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load trust store from: " + path, e);
        }
    }

    private void disableHostnameVerification(SslHandler sslHandler) {
        SSLEngine engine = sslHandler.engine();
        SSLParameters params = engine.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("");
        engine.setSSLParameters(params);
    }
}
