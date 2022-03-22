package ru.red.authenticationservice.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.red.authenticationservice.jooq.tables.records.UsersRecord;

import java.security.Key;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.util.Date.from;

/**
 * JWT Token Provider
 * Hardcoded to be working with RSA Encryption algorithm
 */
@Log4j2
@Component
public class JwtProvider {
    private final Key privateKey;
    private final Long jwtMinutes;

    /**
     * @param privateKey PKCS#8 Private key
     * @param jwtMinutes Number of minutes till the key will be available
     */
    @Autowired
    public JwtProvider(PrivateKey privateKey,
                       @Value("${jwt.claims.exp.minutes}") Long jwtMinutes) {
        this.privateKey = privateKey;
        this.jwtMinutes = jwtMinutes;
        log.info("Initialized JWT Provider");
    }

    /**
     * Factory method for creating JWTs<br>
     * <b>Claims</b>
     * <table>
     * <th>Field</th>
     * <th>Description</th>
     * <tr>
     * <td>sub</td>
     * <td>User id</td>
     * </tr>
     * <tr>
     * <td>iss</td>
     * <td>JWT Creation time</td>
     * </tr>
     * <tr>
     * <td>exp</td>
     * <td>JWT Creation time plus jwtMinutes property from config</td>
     * </tr>
     * </table>
     *
     * @param usersRecord containing user credentials
     * @return {@link String} with JSON Web Token
     */
    public String createJwt(UsersRecord usersRecord) {
        var id = usersRecord.getId();
        var username = usersRecord.getEmail();
        log.info("Creating JWT for [{}] {}", id, username);
        LocalDateTime now = LocalDateTime.now();
        return Jwts.builder()
                .setIssuer("auth.arch.homework")
                .setSubject(username)
                .setIssuedAt(from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(from(
                                now.plusMinutes(jwtMinutes)
                                        .toInstant(ZoneOffset.UTC)
                        )
                )
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }
}
