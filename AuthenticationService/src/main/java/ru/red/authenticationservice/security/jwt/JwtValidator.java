package ru.red.authenticationservice.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

/**
 * JSON Web Token Validator
 */
@Log4j2
@Component
public class JwtValidator {
    private final JwtParser parser;

    /**
     * @param publicKey used to verify signature of the JWT
     */
    public JwtValidator(PublicKey publicKey) {
        this.parser = Jwts.parser().setSigningKey(publicKey);
        log.info("Initialized JWT Validator");
    }

    /**
     * JWT Predicate method
     *
     * @param jws JWT String
     * @return is JWT Valid
     */
    public boolean validateJwt(String jws) {
        try {
            parser.parseClaimsJws(jws);
        } catch (JwtException e) {
            log.info("Failed validating JWT {}", e.getMessage());
            return false;
        }
        return true;
    }
}
