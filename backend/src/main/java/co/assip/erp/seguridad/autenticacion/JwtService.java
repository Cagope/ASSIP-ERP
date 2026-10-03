package co.assip.erp.seguridad.autenticacion;

import co.assip.erp.seguridad.usuarios.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de generar, validar y leer
 * los tokens JWT utilizados por el ERP.
 *
 * El JWT identifica al usuario y conserva el contexto
 * de agencias. Los roles y permisos se consultan desde
 * la base de datos cuando son necesarios.
 */
@Service
public class JwtService {

    private static final String SECRET_KEY =
            "ClaveSecretaSeguraAssipErpBackend1234567890";

    private static final long DURACION_TOKEN_MS =
            1000L * 60 * 60 * 10;

    /**
     * Construye la clave utilizada para firmar
     * y validar los tokens.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes()
        );
    }

    /**
     * Genera el token de autenticación.
     *
     * Incluye únicamente:
     * - identificador del usuario;
     * - agencias autorizadas;
     * - username como subject.
     *
     * No incluye roles ni permisos.
     */
    public String generarTokenConClaims(
            Usuario usuario,
            List<Integer> agencias
    ) {

        Map<String, Object> claims =
                new HashMap<>();

        claims.put(
                "idUsuario",
                usuario.getIdUsuario()
        );

        claims.put(
                "agencias",
                agencias
        );

        long ahora =
                System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(
                        usuario.getUsername()
                )
                .setIssuedAt(
                        new Date(ahora)
                )
                .setExpiration(
                        new Date(
                                ahora + DURACION_TOKEN_MS
                        )
                )
                .signWith(
                        getSigningKey(),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    /**
     * Valida firma, estructura y vigencia del token.
     */
    public boolean validarToken(
            String token
    ) {

        if (token == null
                || token.isBlank()) {
            return false;
        }

        try {

            Jwts.parserBuilder()
                    .setSigningKey(
                            getSigningKey()
                    )
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (JwtException
                 | IllegalArgumentException ex) {

            return false;
        }
    }

    /**
     * Obtiene el username almacenado
     * como subject del token.
     */
    public String extraerUsername(
            String token
    ) {

        return extraerTodo(token)
                .getSubject();
    }

    /**
     * Obtiene todos los claims del token.
     *
     * Esta operación también valida su firma
     * y fecha de expiración.
     */
    public Claims extraerTodo(
            String token
    ) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        getSigningKey()
                )
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}