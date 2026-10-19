
package co.assip.erp.depositos.transferencias;

import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasCuentaDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;


// =========================================================
// REPOSITORIO
// TRANSFERENCIA DE FONDOS ENTRE CUENTAS
// =========================================================
//
// Responsabilidades:
//
// 1. Consultar cuentas de depósitos.
// 2. Buscar cuentas por identificación del asociado.
// 3. Consultar información detallada de una cuenta.
// 4. Bloquear cuentas durante la transferencia.
// 5. Consultar parametrización contable.
// 6. Validar cuentas del catálogo contable.
// 7. Consultar información de las agencias.
//
// Este repositorio no registra movimientos financieros
// ni genera comprobantes contables.
//
// =========================================================

@Repository
@RequiredArgsConstructor
public class TransferenciaCuentasRepository {

    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final JdbcTemplate jdbc;


    // =====================================================
    // ESTRUCTURA INTERNA DE CUENTA
    // =====================================================
    //
    // Representa los datos necesarios para validar
    // y ejecutar una transferencia.
    //
    // =====================================================

    public record Cuenta(
            Integer id,
            Integer agencia,
            Integer persona,
            Integer forma,
            String codigo,
            String tipoCaptacion,
            String estado,
            boolean operativa,
            BigDecimal saldo,
            Integer plazo,
            BigDecimal valorCanje
    ) {
    }


    // =====================================================
    // ESTRUCTURA DE PARAMETRIZACIÓN CONTABLE
    // =====================================================

    public record Parametros(
            Integer producto,
            Integer interagencia
    ) {
    }


    // =====================================================
    // CONSULTA BASE DE CUENTAS
    // =====================================================
    //
    // Incluye:
    //
    // - Identificación de la cuenta.
    // - Agencia y asociado.
    // - Forma de ahorro.
    // - Tipo de captación.
    // - Estado operativo.
    // - Saldo actual.
    // - Plazo.
    //
    // =====================================================

    private static final String SELECT_CUENTAS = """
            SELECT
                ca.id_cuenta_ahorro,
                ca.id_agencia,
                ca.id_datos_personal,
                ca.id_forma_ahorro,
                TRIM(ca.codigo_cuenta) AS codigo_cuenta,
                TRIM(fa.tipo_captacion_forma) AS tipo_captacion,
                TRIM(ca.estado_cuenta_cuenta) AS estado_cuenta,
                COALESCE(ea.operativo, FALSE) AS operativa,
                COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,
                ca.plazo_cuenta
            FROM depositos.cuentas_ahorro ca
            JOIN depositos.formas_ahorro fa
                ON fa.id_forma_ahorro = ca.id_forma_ahorro
            LEFT JOIN depositos.estados_ahorros ea
                ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
            """;


    // =====================================================
    // CONSULTAR CUENTA PARA OPERACIÓN
    // =====================================================
    //
    // Cuando bloqueo = true:
    //
    // Se utiliza FOR UPDATE OF ca para bloquear
    // exclusivamente el registro de la cuenta.
    //
    // El bloqueo se mantiene durante la transacción
    // que ejecuta TransferenciaCuentasService.
    //
    // =====================================================

    public Cuenta obtener(
            Integer idCuentaAhorro,
            boolean bloqueo
    ) {

        String sql = SELECT_CUENTAS
                + " WHERE ca.id_cuenta_ahorro = ?"
                + (bloqueo ? " FOR UPDATE OF ca" : "");


        List<Cuenta> cuentas = jdbc.query(

                sql,

                this::mapearCuenta,

                idCuentaAhorro
        );


        if (cuentas.size() != 1) {
            throw new IllegalArgumentException(
                    "Cuenta de ahorro no encontrada: "
                            + idCuentaAhorro
            );
        }

        return cuentas.get(0);
    }


    // =====================================================
    // MAPEAR CUENTA
    // =====================================================

    private Cuenta mapearCuenta(
            ResultSet rs,
            int fila
    ) throws SQLException {

        return new Cuenta(

                rs.getInt("id_cuenta_ahorro"),

                rs.getInt("id_agencia"),

                rs.getInt("id_datos_personal"),

                rs.getInt("id_forma_ahorro"),

                rs.getString("codigo_cuenta"),

                rs.getString("tipo_captacion"),

                rs.getString("estado_cuenta"),

                rs.getBoolean("operativa"),

                rs.getBigDecimal("saldo_actual"),

                (Integer) rs.getObject("plazo_cuenta"),

                BigDecimal.ZERO
        );
    }


    // =====================================================
// CONSULTA BASE PARA DTO DE CUENTA
// =====================================================
//
// Se utiliza tanto para consultar una cuenta
// como para realizar búsquedas.
//
// Incluye:
// - Identificación del asociado.
// - Código y nombre de la agencia.
// - Información del producto.
// - Código y descripción del estado.
// - Saldo actual.
//
// =====================================================

    private static final String SELECT_CUENTA_DTO = """
        SELECT
            ca.id_cuenta_ahorro,
            ca.id_agencia,
            ca.id_datos_personal,

            TRIM(ag.codigo_agencia) AS codigo_agencia,
            ag.nombre_agencia,

            dp.documento,

            TRIM(
                CONCAT_WS(
                    ' ',
                    dp.primer_apellido,
                    dp.segundo_apellido,
                    dp.nombres
                )
            ) AS nombre_completo,

            TRIM(ca.codigo_cuenta) AS codigo_cuenta,
            fa.nombre_forma,
            TRIM(fa.tipo_captacion_forma) AS tipo_captacion,

            TRIM(ca.estado_cuenta_cuenta) AS estado_cuenta,
            ea.descripcion_estado_ahorro AS descripcion_estado_cuenta,
            COALESCE(ea.operativo, FALSE) AS operativa,

            COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual

        FROM depositos.cuentas_ahorro ca

        JOIN general.datos_agencias ag
            ON ag.id_agencia = ca.id_agencia

        JOIN depositos.formas_ahorro fa
            ON fa.id_forma_ahorro = ca.id_forma_ahorro

        JOIN hoja_vida.datos_personales dp
            ON dp.id_datos_personal = ca.id_datos_personal

        LEFT JOIN depositos.estados_ahorros ea
            ON TRIM(ea.codigo_estado_ahorro) =
               TRIM(ca.estado_cuenta_cuenta)
        """;


    // =====================================================
// MAPEAR DTO DE CUENTA
// =====================================================

    private TransferenciaCuentasCuentaDTO mapearCuentaDTO(
            ResultSet rs,
            int fila
    ) throws SQLException {

        // -------------------------------------------------
        // INFORMACIÓN FINANCIERA
        // -------------------------------------------------

        BigDecimal saldoActual =
                rs.getBigDecimal("saldo_actual");

        // -------------------------------------------------
        // ESTADO Y TIPO DE CAPTACIÓN
        // -------------------------------------------------

        boolean operativa =
                rs.getBoolean("operativa");

        String tipoCaptacion =
                rs.getString("tipo_captacion");

        boolean permiteDebito =
                operativa && "2".equals(tipoCaptacion);

        // -------------------------------------------------
        // CONSTRUIR DTO
        // -------------------------------------------------

        return new TransferenciaCuentasCuentaDTO(

                rs.getInt("id_cuenta_ahorro"),

                rs.getInt("id_agencia"),

                rs.getInt("id_datos_personal"),

                rs.getString("codigo_agencia"),

                rs.getString("nombre_agencia"),

                rs.getString("documento"),

                rs.getString("nombre_completo"),

                rs.getString("codigo_cuenta"),

                rs.getString("nombre_forma"),

                tipoCaptacion,

                rs.getString("estado_cuenta"),

                rs.getString("descripcion_estado_cuenta"),

                operativa,

                saldoActual,

                saldoActual,

                permiteDebito
        );
    }


    // =====================================================
    // CONSULTAR INFORMACIÓN DETALLADA DE UNA CUENTA
    // =====================================================
    //
    // Devuelve la información que utiliza el frontend
    // para identificar la cuenta seleccionada.
    //
    // =====================================================

    public TransferenciaCuentasCuentaDTO obtenerDTO(
            Integer idCuentaAhorro
    ) {

        String sql = SELECT_CUENTA_DTO
                + " WHERE ca.id_cuenta_ahorro = ?";


        List<TransferenciaCuentasCuentaDTO> cuentas =
                jdbc.query(

                        sql,

                        this::mapearCuentaDTO,

                        idCuentaAhorro
                );


        if (cuentas.size() != 1) {
            throw new IllegalArgumentException(
                    "Cuenta de ahorro no encontrada: "
                            + idCuentaAhorro
            );
        }

        return cuentas.get(0);
    }


    // =====================================================
    // BUSCAR CUENTAS DE DEPÓSITOS
    // =====================================================
    //
    // Permite buscar por:
    //
    // - Documento.
    // - Nombres.
    // - Primer apellido.
    // - Segundo apellido.
    //
    // La búsqueda incluye cuentas de todas las agencias.
    //
    // No se realiza la consulta cuando todos los
    // criterios están vacíos.
    //
    // Se limita el resultado a 100 cuentas.
    //
    // =====================================================

    public List<TransferenciaCuentasCuentaDTO> buscar(

            String documento,

            String nombres,

            String primerApellido,

            String segundoApellido
    ) {

        // -------------------------------------------------
        // NORMALIZAR FILTROS
        // -------------------------------------------------

        String filtroDocumento =
                limpiar(documento);

        String filtroNombres =
                limpiar(nombres);

        String filtroPrimerApellido =
                limpiar(primerApellido);

        String filtroSegundoApellido =
                limpiar(segundoApellido);


        // -------------------------------------------------
        // VALIDAR CRITERIOS
        // -------------------------------------------------

        if (
                filtroDocumento.isBlank()
                        && filtroNombres.isBlank()
                        && filtroPrimerApellido.isBlank()
                        && filtroSegundoApellido.isBlank()
        ) {
            return List.of();
        }


        // -------------------------------------------------
        // CONSULTAR CUENTAS
        // -------------------------------------------------

        String sql = SELECT_CUENTA_DTO + """
                WHERE dp.documento ILIKE ?
                  AND dp.nombres ILIKE ?
                  AND dp.primer_apellido ILIKE ?
                  AND dp.segundo_apellido ILIKE ?
                ORDER BY
                    dp.primer_apellido,
                    dp.segundo_apellido,
                    ca.id_cuenta_ahorro
                LIMIT 100
                """;


        return jdbc.query(

                sql,

                this::mapearCuentaDTO,

                "%" + filtroDocumento + "%",

                "%" + filtroNombres + "%",

                "%" + filtroPrimerApellido + "%",

                "%" + filtroSegundoApellido + "%"
        );
    }


    // =====================================================
    // NORMALIZAR FILTRO
    // =====================================================

    private String limpiar(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }


    // =====================================================
    // CONSULTAR PARAMETRIZACIÓN CONTABLE
    // =====================================================
    //
    // La forma de ahorro contiene:
    //
    // - Cuenta contable de corto plazo.
    // - Cuenta contable de largo plazo.
    // - Cuenta puente interagencia.
    //
    // Si las cuentas de corto y largo plazo son iguales,
    // se utiliza esa misma cuenta.
    //
    // Si son diferentes, se selecciona según el plazo
    // de la cuenta de depósitos.
    //
    // =====================================================

    public Parametros parametros(
            Cuenta cuenta
    ) {

        String sql = """
                SELECT
                    cuenta_forma_corto,
                    cuenta_forma_largo,
                    cuenta_interagencia
                FROM depositos.formas_ahorro
                WHERE id_forma_ahorro = ?
                """;


        return jdbc.query(

                sql,

                rs -> {

                    if (!rs.next()) {
                        throw new IllegalStateException(
                                "No existe la forma de ahorro "
                                        + cuenta.forma()
                        );
                    }


                    Integer cuentaCorto =
                            (Integer) rs.getObject(
                                    "cuenta_forma_corto"
                            );

                    Integer cuentaLargo =
                            (Integer) rs.getObject(
                                    "cuenta_forma_largo"
                            );

                    Integer cuentaInteragencia =
                            (Integer) rs.getObject(
                                    "cuenta_interagencia"
                            );


                    // -------------------------------------
                    // DETERMINAR CUENTA CONTABLE DEL PRODUCTO
                    // -------------------------------------

                    Integer cuentaProducto;

                    if (Objects.equals(
                            cuentaCorto,
                            cuentaLargo
                    )) {

                        cuentaProducto = cuentaCorto;

                    } else {

                        if (cuenta.plazo() == null) {
                            throw new IllegalStateException(
                                    "La forma de ahorro requiere "
                                            + "un plazo para determinar "
                                            + "la cuenta contable."
                            );
                        }

                        cuentaProducto =
                                cuenta.plazo() <= 12
                                        ? cuentaCorto
                                        : cuentaLargo;
                    }


                    // -------------------------------------
                    // VALIDAR PARAMETRIZACIÓN
                    // -------------------------------------

                    if (cuentaProducto == null) {
                        throw new IllegalStateException(
                                "La forma de ahorro no tiene "
                                        + "cuenta contable del producto."
                        );
                    }

                    if (cuentaInteragencia == null) {
                        throw new IllegalStateException(
                                "La forma de ahorro no tiene "
                                        + "cuenta puente interagencia."
                        );
                    }


                    return new Parametros(
                            cuentaProducto,
                            cuentaInteragencia
                    );
                },

                cuenta.forma()
        );
    }


    // =====================================================
    // CONSULTAR CUENTA DEL CATÁLOGO CONTABLE
    // =====================================================
    //
    // Verifica que la cuenta:
    //
    // - Exista.
    // - Pertenezca a la agencia.
    // - Se encuentre operable.
    //
    // Devuelve:
    //
    // [0] Código de la cuenta.
    // [1] Nombre de la cuenta.
    //
    // =====================================================

    public String[] catalogo(

            Integer idCatalogoCuenta,

            Integer idAgencia
    ) {

        String sql = """
                SELECT
                    TRIM(codigo_cuenta),
                    nombre_cuenta
                FROM contabilidad.catalogo_cuentas
                WHERE id_catalogo_cuenta = ?
                  AND id_agencia = ?
                  AND cuenta_operable = TRUE
                """;


        return jdbc.query(

                sql,

                rs -> {

                    if (!rs.next()) {
                        throw new IllegalStateException(
                                "La cuenta contable "
                                        + idCatalogoCuenta
                                        + " no existe o no está operable "
                                        + "en la agencia "
                                        + idAgencia
                        );
                    }


                    String[] resultado = {

                            rs.getString(1),

                            rs.getString(2)
                    };


                    if (rs.next()) {
                        throw new IllegalStateException(
                                "Se encontraron registros duplicados "
                                        + "para la cuenta contable "
                                        + idCatalogoCuenta
                        );
                    }


                    return resultado;
                },

                idCatalogoCuenta,

                idAgencia
        );
    }


    // =====================================================
    // CONSULTAR NOMBRE DE AGENCIA
    // =====================================================

    public String nombreAgencia(
            Integer idAgencia
    ) {

        String sql = """
                SELECT nombre_agencia
                FROM general.datos_agencias
                WHERE id_agencia = ?
                """;


        return jdbc.queryForObject(

                sql,

                String.class,

                idAgencia
        );
    }

}
