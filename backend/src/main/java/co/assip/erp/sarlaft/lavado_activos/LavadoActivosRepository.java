package co.assip.erp.sarlaft.lavado_activos;

import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosPersonaDTO;
import java.util.List;

import co.assip.erp.sarlaft.lavado_activos.dto.LavadoActivosFormatoDTO;

@Repository
@RequiredArgsConstructor
public class LavadoActivosRepository {

    private final JdbcTemplate jdbc;

    public BigDecimal obtenerMontoControlLavado(
            Integer idAgencia
    ) {
        String sql = """
            SELECT valor_parametro
            FROM general.parametros
            WHERE id_agencia = ?
              AND codigo_parametro = 900
            LIMIT 1
        """;

        BigDecimal valor = jdbc.queryForObject(
                sql,
                BigDecimal.class,
                idAgencia
        );

        if (valor == null) {
            throw new RuntimeException(
                    "No existe el parámetro 900 - MONTO FORMATO LAVADO DE ACTIVOS para la agencia."
            );
        }

        return valor;
    }

    public boolean existeFormatoOrigen(
            String modulo,
            String proceso,
            Long idOrigen
    ) {
        if (idOrigen == null) {
            return false;
        }

        String sql = """
            SELECT COUNT(1)
            FROM sarlaft.formatos_lavado_activos
            WHERE modulo = ?
              AND proceso = ?
              AND id_origen = ?
        """;

        Integer count = jdbc.queryForObject(
                sql,
                Integer.class,
                modulo,
                proceso,
                idOrigen
        );

        return count != null && count > 0;
    }

    public Long insertar(
            LavadoActivosRequestDTO request,
            Integer idUsuario
    ) {
        String sql = """
            INSERT INTO sarlaft.formatos_lavado_activos (
                modulo,
                proceso,
                id_origen,

                fecha_transaccion,
                fecha_contable,

                tipo_transaccion,
                valor_transaccion,

                id_agencia,
                codigo_agencia,
                nombre_agencia,

                id_datos_personal,
                tipo_documento,
                documento,
                nombre_completo,

                codigo_producto,
                descripcion_producto,

                numero_producto,
                numero_comprobante,

                actividad_economica,

                tipo_documento_realiza,
                documento_realiza,

                primer_apellido_realiza,
                segundo_apellido_realiza,

                primer_nombre_realiza,
                segundo_nombre_realiza,

                direccion_realiza,
                telefono_realiza,

                departamento_realiza,
                ciudad_realiza,

                nombre_beneficiario,
                direccion_beneficiario,
                telefono_beneficiario,

                requiere_firma,
                firmado,

                fk_seguridad_creacion,
                fecha_creacion,
                fk_seguridad_edicion,
                fecha_edicion
            )
            VALUES (
                ?, ?, ?,
                ?, ?,
                ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?,
                ?, ?,
                ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?, ?,
                true,
                false,
                ?,
                CURRENT_TIMESTAMP,
                ?,
                CURRENT_TIMESTAMP
            )
            RETURNING id_formato_lavado_activos
        """;

        return jdbc.queryForObject(
                sql,
                Long.class,

                request.getModulo(),
                request.getProceso(),
                request.getIdOrigen(),

                request.getFechaTransaccion(),
                request.getFechaContable(),

                request.getTipoTransaccion(),
                request.getValorTransaccion(),

                request.getIdAgencia(),
                request.getCodigoAgencia(),
                request.getNombreAgencia(),

                request.getIdDatosPersonal(),
                request.getTipoDocumento(),
                request.getDocumento(),
                request.getNombreCompleto(),

                request.getCodigoProducto(),
                request.getDescripcionProducto(),

                request.getNumeroProducto(),
                request.getNumeroComprobante(),

                request.getActividadEconomica(),

                request.getTipoDocumentoRealiza(),
                request.getDocumentoRealiza(),

                request.getPrimerApellidoRealiza(),
                request.getSegundoApellidoRealiza(),

                request.getPrimerNombreRealiza(),
                request.getSegundoNombreRealiza(),

                request.getDireccionRealiza(),
                request.getTelefonoRealiza(),

                request.getDepartamentoRealiza(),
                request.getCiudadRealiza(),

                request.getNombreBeneficiario(),
                request.getDireccionBeneficiario(),
                request.getTelefonoBeneficiario(),

                idUsuario,
                idUsuario
        );
    }

    public void marcarImpreso(
            Long idLavadoActivos,
            Integer idUsuario
    ) {
        String sql = """
            UPDATE sarlaft.formatos_lavado_activos
            SET veces_impreso = COALESCE(veces_impreso, 0) + 1,
                fecha_ultima_impresion = CURRENT_TIMESTAMP,
                fk_seguridad_edicion = ?,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_formato_lavado_activos = ?
        """;

        jdbc.update(
                sql,
                idUsuario,
                idLavadoActivos
        );
    }

    public void marcarFirmado(
            Long idLavadoActivos,
            String observacion,
            Integer idUsuario
    ) {
        String sql = """
            UPDATE sarlaft.formatos_lavado_activos
            SET firmado = true,
                fecha_firma = CURRENT_TIMESTAMP,
                observacion_firma = ?,
                fk_seguridad_edicion = ?,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_formato_lavado_activos = ?
        """;

        jdbc.update(
                sql,
                observacion,
                idUsuario,
                idLavadoActivos
        );
    }

    public LavadoActivosPersonaDTO buscarPersonaPorDocumento(
            String documento
    ) {
        String sql = """
        SELECT
            dp.id_datos_personal,
            dp.tipo_documento,
            dp.documento,
            dp.id_departamento_expedicion,
            dp.id_ciudad_expedicion,
            dp.primer_apellido,
            dp.segundo_apellido,
            dp.nombres,

            ub.direccion,
            COALESCE(
                NULLIF(TRIM(ub.celular_uno), ''),
                NULLIF(TRIM(ub.telefono), ''),
                NULLIF(TRIM(ub.celular_dos), '')
            ) AS telefono

        FROM hoja_vida.datos_personales dp

        LEFT JOIN LATERAL (
            SELECT
                u.direccion,
                u.telefono,
                u.celular_uno,
                u.celular_dos
            FROM hoja_vida.ubicaciones u
            WHERE u.id_datos_personal = dp.id_datos_personal
            ORDER BY u.id_ubicacion ASC
            LIMIT 1
        ) ub ON true

        WHERE TRIM(dp.documento) = TRIM(?)
        LIMIT 1
    """;

        List<LavadoActivosPersonaDTO> datos = jdbc.query(
                sql,
                (rs, rowNum) -> {

                    String nombres = rs.getString("nombres");

                    String[] partes = nombres == null
                            ? new String[0]
                            : nombres.trim().split("\\s+", 2);

                    String primerNombre =
                            partes.length > 0 ? partes[0] : "";

                    String segundoNombre =
                            partes.length > 1 ? partes[1] : "";

                    String primerApellido =
                            rs.getString("primer_apellido");

                    String segundoApellido =
                            rs.getString("segundo_apellido");

                    String nombreCompleto =
                            (
                                    nvl(primerApellido) + " " +
                                            nvl(segundoApellido) + " " +
                                            nvl(nombres)
                            ).trim();

                    return LavadoActivosPersonaDTO.builder()
                            .idDatosPersonal(rs.getLong("id_datos_personal"))
                            .tipoDocumento(rs.getString("tipo_documento"))
                            .documento(rs.getString("documento"))
                            .idDepartamentoExpedicion(
                                    rs.getObject(
                                            "id_departamento_expedicion",
                                            Integer.class
                                    )
                            )
                            .idCiudadExpedicion(
                                    rs.getObject(
                                            "id_ciudad_expedicion",
                                            Integer.class
                                    )
                            )
                            .primerApellido(primerApellido)
                            .segundoApellido(segundoApellido)
                            .primerNombre(primerNombre)
                            .segundoNombre(segundoNombre)
                            .nombreCompleto(nombreCompleto)
                            .direccion(rs.getString("direccion"))
                            .telefono(rs.getString("telefono"))
                            .build();
                },
                documento
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    public LavadoActivosFormatoDTO obtenerPorId(
            Long idFormatoLavadoActivos
    ) {
        String sql = """
        SELECT
            id_formato_lavado_activos,
            modulo,
            proceso,
            id_origen,
            fecha_transaccion,
            fecha_contable,
            tipo_transaccion,
            valor_transaccion,
            id_agencia,
            codigo_agencia,
            nombre_agencia,
            id_datos_personal,
            tipo_documento,
            documento,
            nombre_completo,
            codigo_producto,
            descripcion_producto,
            numero_producto,
            numero_comprobante,
            actividad_economica,
            tipo_documento_realiza,
            documento_realiza,
            primer_apellido_realiza,
            segundo_apellido_realiza,
            primer_nombre_realiza,
            segundo_nombre_realiza,
            direccion_realiza,
            telefono_realiza,
            departamento_realiza,
            ciudad_realiza,
            nombre_beneficiario,
            direccion_beneficiario,
            telefono_beneficiario,
            requiere_firma,
            firmado,
            fecha_firma,
            observacion_firma,
            veces_impreso,
            fecha_ultima_impresion,
            fk_seguridad_creacion,
            fecha_creacion,
            fk_seguridad_edicion,
            fecha_edicion
        FROM sarlaft.formatos_lavado_activos
        WHERE id_formato_lavado_activos = ?
    """;

        List<LavadoActivosFormatoDTO> datos = jdbc.query(
                sql,
                (rs, rowNum) -> LavadoActivosFormatoDTO.builder()
                        .idFormatoLavadoActivos(rs.getLong("id_formato_lavado_activos"))
                        .modulo(rs.getString("modulo"))
                        .proceso(rs.getString("proceso"))
                        .idOrigen(rs.getObject("id_origen", Long.class))
                        .fechaTransaccion(rs.getObject("fecha_transaccion", java.time.LocalDate.class))
                        .fechaContable(rs.getObject("fecha_contable", java.time.LocalDate.class))
                        .tipoTransaccion(rs.getString("tipo_transaccion"))
                        .valorTransaccion(rs.getBigDecimal("valor_transaccion"))
                        .idAgencia(rs.getObject("id_agencia", Integer.class))
                        .codigoAgencia(rs.getString("codigo_agencia"))
                        .nombreAgencia(rs.getString("nombre_agencia"))
                        .idDatosPersonal(rs.getObject("id_datos_personal", Long.class))
                        .tipoDocumento(rs.getString("tipo_documento"))
                        .documento(rs.getString("documento"))
                        .nombreCompleto(rs.getString("nombre_completo"))
                        .codigoProducto(rs.getString("codigo_producto"))
                        .descripcionProducto(rs.getString("descripcion_producto"))
                        .numeroProducto(rs.getString("numero_producto"))
                        .numeroComprobante(rs.getString("numero_comprobante"))
                        .actividadEconomica(rs.getString("actividad_economica"))
                        .tipoDocumentoRealiza(rs.getString("tipo_documento_realiza"))
                        .documentoRealiza(rs.getString("documento_realiza"))
                        .primerApellidoRealiza(rs.getString("primer_apellido_realiza"))
                        .segundoApellidoRealiza(rs.getString("segundo_apellido_realiza"))
                        .primerNombreRealiza(rs.getString("primer_nombre_realiza"))
                        .segundoNombreRealiza(rs.getString("segundo_nombre_realiza"))
                        .direccionRealiza(rs.getString("direccion_realiza"))
                        .telefonoRealiza(rs.getString("telefono_realiza"))
                        .departamentoRealiza(rs.getString("departamento_realiza"))
                        .ciudadRealiza(rs.getString("ciudad_realiza"))
                        .nombreBeneficiario(rs.getString("nombre_beneficiario"))
                        .direccionBeneficiario(rs.getString("direccion_beneficiario"))
                        .telefonoBeneficiario(rs.getString("telefono_beneficiario"))
                        .requiereFirma(rs.getBoolean("requiere_firma"))
                        .firmado(rs.getBoolean("firmado"))
                        .fechaFirma(rs.getObject("fecha_firma", java.time.LocalDateTime.class))
                        .observacionFirma(rs.getString("observacion_firma"))
                        .vecesImpreso(rs.getObject("veces_impreso", Integer.class))
                        .fechaUltimaImpresion(rs.getObject("fecha_ultima_impresion", java.time.LocalDateTime.class))
                        .fkSeguridadCreacion(rs.getObject("fk_seguridad_creacion", Integer.class))
                        .fechaCreacion(rs.getObject("fecha_creacion", java.time.LocalDateTime.class))
                        .fkSeguridadEdicion(rs.getObject("fk_seguridad_edicion", Integer.class))
                        .fechaEdicion(rs.getObject("fecha_edicion", java.time.LocalDateTime.class))
                        .build(),
                idFormatoLavadoActivos
        );

        return datos.isEmpty() ? null : datos.get(0);
    }

    private String nvl(String value) {
        return value == null ? "" : value.trim();
    }
}