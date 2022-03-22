package ru.red.authenticationservice.controller;

import liquibase.util.MD5Util;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.authenticationservice.dto.JwksDto;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/.well-known")
public class WellKnownController {
    private final JwksDto jwks;

    /**
     * @param publicKey to expose as JWK to outside world
     */
    public WellKnownController(PublicKey publicKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        this.jwks = new JwksDto();
        jwks.addKey(generateJwk(rsaPublicKey));
    }

    /**
     * JWK exposing endpoint
     *
     * @return <code>Map<String, Object></code> JWK
     */
    @GetMapping("jwks.json")
    public Mono<JwksDto> publicKey() {
        return Mono.just(jwks);
    }

    private Map<String, Object> generateJwk(RSAPublicKey rsaPublicKey) {
        Map<String, Object> values = new HashMap<>();
        values.put("kty", rsaPublicKey.getAlgorithm());
        values.put("kid", MD5Util.computeMD5(rsaPublicKey.getPublicExponent().toString())); // TODO : Implement JWK Rotation
        values.put("n", Base64.getUrlEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray()));
        values.put("e", Base64.getUrlEncoder().encodeToString(rsaPublicKey.getPublicExponent().toByteArray()));
        values.put("alg", "RS256");
        values.put("use", "sig");
        return values;
    }
}
