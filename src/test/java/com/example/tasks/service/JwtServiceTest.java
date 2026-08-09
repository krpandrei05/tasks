package com.example.tasks.service;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.AesKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String TEST_SECRET = "TXlTZWNyZXRLZXkyMDI2Rm9ySnd0U2lnbmluZ1Rhc2tzQXBwISE=";
    private static final long TEST_EXPIRATION_MS = 600000L; // 10 minute, diferit de config-ul real (60 min)

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", TEST_EXPIRATION_MS);
    }

    private JwtClaims decodeToken(String token) throws Exception {
        JwtConsumer consumer = new JwtConsumerBuilder()
                .setVerificationKey(new AesKey(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA256)
                .setSkipDefaultAudienceValidation()
                .setRequireExpirationTime()
                .build();
        return consumer.processToClaims(token);
    }

    // Cazul 1 - Continutul token-ului
    @Test
    void generateToken_includesCorrectSubjectAndRoleIdClaim() throws Exception {
        String token = jwtService.generateToken("andrei.krp@gmail.com", 2L);

        JwtClaims decodedClaims = decodeToken(token);

        assertEquals("andrei.krp@gmail.com", decodedClaims.getSubject());
        assertEquals(2L, decodedClaims.getClaimValue("roleId", Long.class));
    }

    // Cazul 2 - Calculul de expirare
    @Test
    void generateToken_setsExpirationBasedOnConfiguredValue() throws Exception {
        String token = jwtService.generateToken("andrei.krp@gmail.com", 1L);

        JwtClaims decodedClaims = decodeToken(token);
        NumericDate issuedAt = decodedClaims.getIssuedAt();
        NumericDate expiration = decodedClaims.getExpirationTime();

        long actualDiffSeconds = expiration.getValue() - issuedAt.getValue();
        long expectedDiffSeconds = TEST_EXPIRATION_MS / 1000;

        assertEquals(expectedDiffSeconds, actualDiffSeconds);
    }

}