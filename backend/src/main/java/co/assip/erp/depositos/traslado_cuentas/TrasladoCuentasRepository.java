
package co.assip.erp.depositos.traslado_cuentas;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Objects;


// =========================================================
// REPOSITORIO
// TRASLADO INTEGRAL DE CUENTAS ENTRE AGENCIAS
// =========================================================

@Repository
@RequiredArgsConstructor
public class TrasladoCuentasRepository {

 // =====================================================
 // DEPENDENCIAS
 // =====================================================

 private final JdbcTemplate jdbc;


 // =====================================================
 // ESTRUCTURA DE CUENTA DE AHORRO
 // =====================================================

 public record Cuenta(
         Integer idCuentaAhorro,
         Integer idAgencia,
         Integer idFormaAhorro,
         String codigoCuenta,
         String codigoForma,
         String nombreForma,
         String tipoCaptacion,
         String estadoCuenta,
         boolean operativo,
         BigDecimal saldoActual,
         Integer plazoCuenta,
         String comentario
 ) {
 }


 // =====================================================
 // CONSULTAR CUENTAS DEL ASOCIADO
 // =====================================================

 public List<Cuenta> cuentas(
         Integer persona,
         boolean bloqueo
 ) {

  String sql = """
                SELECT
                    ca.id_cuenta_ahorro,
                    ca.id_agencia,
                    ca.id_forma_ahorro,
                    TRIM(ca.codigo_cuenta) AS codigo_cuenta,
                    TRIM(fa.codigo_forma) AS codigo_forma,
                    fa.nombre_forma,
                    TRIM(fa.tipo_captacion_forma) AS tipo_captacion,
                    TRIM(ca.estado_cuenta_cuenta) AS estado_cuenta,
                    COALESCE(ea.operativo, false) AS operativo,
                    COALESCE(ca.saldo_actual_cuenta, 0) AS saldo_actual,
                    ca.plazo_cuenta,
                    ca.comentario_cuentas
                FROM depositos.cuentas_ahorro ca
                JOIN depositos.formas_ahorro fa
                    ON fa.id_forma_ahorro = ca.id_forma_ahorro
                LEFT JOIN depositos.estados_ahorros ea
                    ON ea.codigo_estado_ahorro = ca.estado_cuenta_cuenta
                WHERE ca.id_datos_personal = ?
                ORDER BY ca.id_cuenta_ahorro
                """
          + (bloqueo ? " FOR UPDATE OF ca" : "");

  return jdbc.query(
          sql,
          (rs, rowNum) -> new Cuenta(
                  rs.getInt(1),
                  rs.getInt(2),
                  rs.getInt(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getString(6),
                  rs.getString(7),
                  rs.getString(8),
                  rs.getBoolean(9),
                  rs.getBigDecimal(10),
                  (Integer) rs.getObject(11),
                  rs.getString(12)
          ),
          persona
  );
 }


 // =====================================================
 // BLOQUEAR ASOCIADO DURANTE EL TRASLADO
 // =====================================================

 public void bloquearAsociado(Integer id) {

  String sql = """
                SELECT
                    id_datos_personal
                FROM hoja_vida.datos_personales
                WHERE id_datos_personal = ?
                FOR UPDATE
                """;

  List<Integer> ids = jdbc.query(
          sql,
          (rs, rowNum) -> rs.getInt(1),
          id
  );

  if (ids.size() != 1) {

   throw new IllegalStateException(
           "El asociado no existe."
   );
  }
 }


 // =====================================================
 // OBTENER FORMA DE AHORRO EN AGENCIA DESTINO
 // =====================================================

 public Integer formaDestino(
         Cuenta cuenta,
         Integer agencia
 ) {

  String sql = """
                SELECT
                    id_forma_ahorro
                FROM depositos.formas_ahorro
                WHERE id_agencia = ?
                  AND TRIM(codigo_forma) = ?
                """;

  List<Integer> ids = jdbc.query(
          sql,
          (rs, rowNum) -> rs.getInt(1),
          agencia,
          cuenta.codigoForma()
  );

  if (ids.size() != 1) {

   throw new IllegalStateException(
           "Debe existir una única forma "
                   + cuenta.codigoForma()
                   + " en agencia "
                   + agencia
   );
  }

  return ids.get(0);
 }


 // =====================================================
 // VALIDAR CUENTA DUPLICADA EN DESTINO
 // =====================================================

 public boolean duplicada(
         Cuenta cuenta,
         Integer agencia,
         Integer forma
 ) {

  String sql = """
                SELECT COUNT(*)
                FROM depositos.cuentas_ahorro
                WHERE id_agencia = ?
                  AND id_forma_ahorro = ?
                  AND TRIM(codigo_cuenta) = ?
                """;

  Integer cantidad = jdbc.queryForObject(
          sql,
          Integer.class,
          agencia,
          forma,
          cuenta.codigoCuenta()
  );

  return cantidad != null && cantidad > 0;
 }


 // =====================================================
 // CREAR CUENTA EN AGENCIA DESTINO
 // =====================================================

 public Integer crearDestino(
         Cuenta cuenta,
         Integer agencia,
         Integer forma,
         String comentario,
         Integer usuario
 ) {

  String sql = """
                INSERT INTO depositos.cuentas_ahorro (
                    id_agencia,
                    id_forma_ahorro,
                    codigo_cuenta,
                    id_datos_personal,
                    fecha_apertura_cuenta,
                    saldo_inicial_cuenta,
                    saldo_actual_cuenta,
                    estado_cuenta_cuenta,
                    fecha_estado_cuenta,
                    gmf_cuenta_cuenta,
                    fecha_gmf_cuenta,
                    libranza_cuenta,
                    libranzatiempo_pago,
                    cuota_mensual_cuenta,
                    retencion_fuente_cuenta,
                    plazo_cuenta,
                    fecha_final_cuenta,
                    cuenta_activa,
                    cuenta_conjunta,
                    accion_conjunta,
                    fk_seguridad_creacion,
                    fecha_creacion,
                    fk_seguridad_edicion,
                    fecha_edicion,
                    tasa,
                    comentario_cuentas
                )
                SELECT
                    ?,
                    ?,
                    codigo_cuenta,
                    id_datos_personal,
                    fecha_apertura_cuenta,
                    0,
                    0,
                    estado_cuenta_cuenta,
                    fecha_estado_cuenta,
                    gmf_cuenta_cuenta,
                    fecha_gmf_cuenta,
                    libranza_cuenta,
                    libranzatiempo_pago,
                    cuota_mensual_cuenta,
                    retencion_fuente_cuenta,
                    plazo_cuenta,
                    fecha_final_cuenta,
                    cuenta_activa,
                    cuenta_conjunta,
                    accion_conjunta,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    tasa,
                    ?
                FROM depositos.cuentas_ahorro
                WHERE id_cuenta_ahorro = ?
                RETURNING id_cuenta_ahorro
                """;

  return jdbc.queryForObject(
          sql,
          Integer.class,
          agencia,
          forma,
          usuario,
          usuario,
          comentario,
          cuenta.idCuentaAhorro()
  );
 }


 // =====================================================
 // MARCAR CUENTA ORIGEN COMO TRASLADADA
 // =====================================================

 public void marcarTrasladada(
         Integer id,
         LocalDate fecha,
         String comentario,
         Integer usuario
 ) {

  String sql = """
                UPDATE depositos.cuentas_ahorro
                SET
                    estado_cuenta_cuenta = 'T',
                    fecha_estado_cuenta = ?,
                    comentario_cuentas = ?,
                    fk_seguridad_edicion = ?,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_cuenta_ahorro = ?
                  AND saldo_actual_cuenta = 0
                """;

  int actualizados = jdbc.update(
          sql,
          fecha,
          comentario,
          usuario,
          id
  );

  if (actualizados != 1) {

   throw new IllegalStateException(
           "No fue posible marcar como trasladada "
                   + "la cuenta "
                   + id
   );
  }
 }


 // =====================================================
 // CONSULTAR IDENTIFICACIÓN DEL ASOCIADO
 // =====================================================

 public Map<String, String> persona(Integer id) {

  String sql = """
                SELECT
                    documento,
                    TRIM(
                        CONCAT_WS(
                            ' ',
                            primer_apellido,
                            segundo_apellido,
                            nombres
                        )
                    ) AS nombre
                FROM hoja_vida.datos_personales
                WHERE id_datos_personal = ?
                """;

  return jdbc.query(
          sql,
          rs -> {

           if (!rs.next()) {

            throw new IllegalStateException(
                    "Asociado no encontrado."
            );
           }

           return Map.of(
                   "documento",
                   rs.getString(1),
                   "nombre",
                   rs.getString(2)
           );
          },
          id
  );
 }


 // =====================================================
 // CONSULTAR NOMBRE DE AGENCIA
 // =====================================================

 public String nombreAgencia(Integer agencia) {

  String sql = """
                SELECT
                    nombre_agencia
                FROM general.datos_agencias
                WHERE id_agencia = ?
                """;

  return jdbc.queryForObject(
          sql,
          String.class,
          agencia
  );
 }


 // =====================================================
 // ESTRUCTURA DE PARÁMETROS CONTABLES
 // =====================================================

 public record Parametros(
         Integer producto,
         Integer interagencia
 ) {
 }


 // =====================================================
 // CONSULTAR PARAMETRIZACIÓN CONTABLE
 // =====================================================

 public Parametros parametros(
         Integer forma,
         Integer plazo
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
                    "Forma de ahorro no encontrada."
            );
           }


           // -------------------------------------
           // CUENTAS PARAMETRIZADAS
           // -------------------------------------

           Integer cuentaCorto =
                   (Integer) rs.getObject(1);

           Integer cuentaLargo =
                   (Integer) rs.getObject(2);

           Integer cuentaInteragencia =
                   (Integer) rs.getObject(3);


           // -------------------------------------
           // DETERMINAR CUENTA DEL PRODUCTO
           // -------------------------------------

           Integer cuentaProducto;

           if (Objects.equals(cuentaCorto, cuentaLargo)) {

            cuentaProducto = cuentaCorto;

           } else {

            if (plazo == null) {

             throw new IllegalStateException(
                     "La forma requiere plazo para "
                             + "seleccionar la cuenta contable."
             );
            }

            cuentaProducto = plazo <= 12
                    ? cuentaCorto
                    : cuentaLargo;
           }


           // -------------------------------------
           // VALIDAR PARAMETRIZACIÓN
           // -------------------------------------

           if (
                   cuentaProducto == null
                           || cuentaInteragencia == null
           ) {

            throw new IllegalStateException(
                    "Falta parametrización de cuenta "
                            + "de producto o interagencia."
            );
           }

           return new Parametros(
                   cuentaProducto,
                   cuentaInteragencia
           );
          },
          forma
  );
 }


 // =====================================================
 // VALIDAR CUENTA DEL CATÁLOGO CONTABLE
 // =====================================================

 public String[] catalogo(
         Integer id,
         Integer agencia
 ) {

  String sql = """
                SELECT
                    TRIM(codigo_cuenta),
                    nombre_cuenta
                FROM contabilidad.catalogo_cuentas
                WHERE id_catalogo_cuenta = ?
                  AND id_agencia = ?
                  AND cuenta_operable = true
                """;

  return jdbc.query(
          sql,
          rs -> {

           if (!rs.next()) {

            throw new IllegalStateException(
                    "Cuenta contable no operable "
                            + "en agencia "
                            + agencia
                            + ": "
                            + id
            );
           }

           String[] cuenta = {
                   rs.getString(1),
                   rs.getString(2)
           };

           if (rs.next()) {

            throw new IllegalStateException(
                    "Cuenta contable duplicada."
            );
           }

           return cuenta;
          },
          id,
          agencia
  );
 }

}
