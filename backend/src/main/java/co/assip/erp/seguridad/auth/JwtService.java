package co.assip.erp.seguridad.auth;

import io.jsonwebtoken.Claims;                    // ← IMPORT CLAVE
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final String issuer;
    private final long expMinutes;

    public JwtService(
            @Value("${assip.auth.jwt.secret}") String secret,
            @Value("${assip.auth.jwt.issuer}") String issuer,
            @Value("${assip.auth.jwt.exp-minutes}") long expMinutes
    ) {
        // Permite secreto en base64 o texto plano; si no es base64 válido, usa bytes directos
        SecretKey key;
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            key = Keys.hmacShaKeyFor(decoded);
        } catch (Exception ignored) {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }
        this.secretKey = key;
        this.issuer = issuer;
        this.expMinutes = expMinutes;
    }

    public String generateToken(Long userId, String username, boolean superuserSeguridad, boolean debeCambiarPassword, boolean activo) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMinutes * 60);

        Map<String, Object> claims = Map.of(
                "uid", userId,
                "usr", username,
                "sup", superuserSeguridad,
                "dcp", debeCambiarPassword,
                "act", activo
        );

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(claims)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAndValidate(String token) {
        // Lanza excepción si es inválido/expirado
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(Claims claims) {
        Object uid = claims.get("uid");
        if (uid instanceof Integer i) return i.longValue();
        if (uid instanceof Long l) return l;
        if (uid instanceof String s) return Long.parseLong(s);
        return null;
    }

    public String getUsername(Claims claims) {
        Object usr = claims.get("usr");
        return usr != null ? usr.toString() : claims.getSubject();
    }
}
