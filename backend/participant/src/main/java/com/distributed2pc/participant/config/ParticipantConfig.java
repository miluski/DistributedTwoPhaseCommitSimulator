package com.distributed2pc.participant.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.distributed2pc.common.enums.FaultType;
import com.distributed2pc.participant.feature.fault.domain.ParticipantFaultStrategy;
import com.distributed2pc.participant.feature.fault.infrastructure.CrashFaultStrategy;
import com.distributed2pc.participant.feature.fault.infrastructure.ForceAbortVoteFaultStrategy;
import com.distributed2pc.participant.feature.fault.infrastructure.IntermittentFaultStrategy;
import com.distributed2pc.participant.feature.fault.infrastructure.NetworkDelayFaultStrategy;
import com.distributed2pc.participant.feature.fault.infrastructure.TransientFaultStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import reactor.netty.http.client.HttpClient;

/**
 * Spring configuration for the participant service.
 *
 * <p>
 * Registers the fault strategy map used by {@code FaultInjectionService}.
 * Adding a new fault type requires only a new {@link ParticipantFaultStrategy}
 * implementation and a single entry in this map — no other class changes.
 */
@Configuration
public class ParticipantConfig {

    /**
     * Builds the fault strategy map keyed by {@link FaultType}.
     *
     * @param crash          strategy for the CRASH fault.
     * @param networkDelay   strategy for the NETWORK_DELAY fault.
     * @param forceAbortVote strategy for the FORCE_ABORT_VOTE fault.
     * @return immutable map of fault type to strategy.
     */
    /**
     * Builds the fault strategy map keyed by {@link FaultType}.
     *
     * @param crash          strategy for the CRASH fault.
     * @param networkDelay   strategy for the NETWORK_DELAY fault.
     * @param forceAbortVote strategy for the FORCE_ABORT_VOTE fault.
     * @param transient_     strategy for the TRANSIENT (auto-recovering) fault.
     * @param intermittent   strategy for the INTERMITTENT (probabilistic) fault.
     * @return immutable map of fault type to strategy.
     */
    @Bean
    public Map<FaultType, ParticipantFaultStrategy> faultStrategies(
            CrashFaultStrategy crash,
            NetworkDelayFaultStrategy networkDelay,
            ForceAbortVoteFaultStrategy forceAbortVote,
            TransientFaultStrategy transientFault,
            IntermittentFaultStrategy intermittent) {
        return Map.of(
                FaultType.CRASH, crash,
                FaultType.NETWORK_DELAY, networkDelay,
                FaultType.FORCE_ABORT_VOTE, forceAbortVote,
                FaultType.TRANSIENT, transientFault,
                FaultType.INTERMITTENT, intermittent);
    }

    /**
     * Shared {@link WebClient} builder configured with the shared trust store so
     * that peer-to-peer and coordinator HTTPS calls succeed inside Docker.
     * Hostname verification is disabled to allow intra-container routing by
     * service name rather than by certificate CN/SAN.
     *
     * @return a pre-configured builder instance.
     * @throws IllegalStateException if the SSL context cannot be initialised.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        try {
            HttpClient httpClient = buildSslHttpClient();
            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient));
        } catch (SSLException e) {
            throw new IllegalStateException("Failed to configure participant WebClient SSL context", e);
        }
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

    /**
     * Jackson {@link ObjectMapper} with Java 8 date/time support.
     *
     * @return configured mapper instance.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
