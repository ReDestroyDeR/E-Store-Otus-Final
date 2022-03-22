package ru.red.authenticationservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(@Value("${auth.security.bcrypt.strength}") Integer strength) {
        return new BCryptPasswordEncoder(strength);
    }

    /*
     * So... The flow below is quite sophisticated
     * We expect a key to be in PKCS#8 Format
     * It has been encoded in Base64 beforehand, so it can be passed as an environment variable
     * And the key itself is Base64 encoded (specified in format)
     *
     * We're creating string from utf-8 bytes we get while decoding our "env variable"
     * Then we're getting rid of useless header and footer
     * After that we're removing all whitespaces
     *
     * Finally, we get desired KEY bytes by calling decoder once more
     */

    @Bean
    public PublicKey publicKey(@Value("${jwt.keys.public}") String publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] publicBytes = decoder.decode(
                new String(decoder.decode(publicKey.getBytes(StandardCharsets.UTF_8)))
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s+", "")
        );

        // X.509 is used for public key. It is generated from .pem and, it works?
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicBytes);
        return keyFactory.generatePublic(publicSpec);
    }

    @Bean
    public PrivateKey privateKey(@Value("${jwt.keys.private}") String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        Base64.Decoder decoder = Base64.getDecoder();

        byte[] privateBytes = decoder.decode(
                new String(decoder.decode(privateKey.getBytes(StandardCharsets.UTF_8)))
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "")
        );

        // Using PKCS8 For private key (As standard states)
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateBytes);
        return keyFactory.generatePrivate(privateSpec);
    }
}
