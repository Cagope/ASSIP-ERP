package co.assip.erp.depositos.retiro_asociado;

import co.assip.erp.depositos.retiro_asociado.dto.RetiroAsociadoEntradaDTO;
import co.assip.erp.depositos.retiro_asociado.dto.RetiroAsociadoPreviewDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.tesoreria.operaciones_pendientes.TesoreriaOperacionesPendientesService;
import co.assip.erp.cajas.movimientos.CajasMovimientosService;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;

@Service
@RequiredArgsConstructor
public class RetiroAsociadoService {

    private final RetiroAsociadoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    private final DepositosMovimientoService depositosMovimientoService;
    private final TesoreriaOperacionesPendientesService tesoreriaOperacionesPendientesService;
    private final RetiroAsociadoContabilizacionService contabilizacionService;
    private final JdbcTemplate jdbc;
    private final CajasMovimientosService cajasMovimientosService;

    /**
     * Consulta informativa de las cuentas con saldo del asociado.
     * No requiere fecha, agencia, estado final ni motivo del retiro.
     * Los controles de operatividad y agencia se mantienen en preview
     * y en aplicar; esta consulta no autoriza movimientos.
     */
    @Transactional(readOnly = true)
    public List<RetiroAsociadoRepository.CuentaRetiro> consultarCuentas(
            Integer idDatosPersonal
    ) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "Debe identificar al asociado."
            );
        }

        return seleccionarPendientes(repository.consultarCuentas(idDatosPersonal));
    }

    @Transactional(readOnly = true)
    public RetiroAsociadoPreviewDTO preview(
            RetiroAsociadoEntradaDTO entrada
    ) {
        validarIdentificacion(entrada);

        List<RetiroAsociadoRepository.CuentaRetiro> cuentas = seleccionarPendientes(
                repository.consultarCuentas(entrada.idDatosPersonal()));

        validarCuentas(cuentas);

        BigDecimal total = sumarSaldos(cuentas);
        BigDecimal caja = sumarMedio(entrada, "CAJA");
        BigDecimal cxp = sumarMedio(entrada, "CXP");

        DistribucionGlobal global = validarDistribucion(entrada, cuentas);
        List<DistribucionCuenta> distribuciones = distribuir(cuentas, global.caja());
        Integer cuentaCaja = null;
        if (global.caja().signum() > 0) {
            Integer agenciaCaja = jdbc.queryForObject(
                    "SELECT id_agencia FROM cajas.cajas WHERE id_caja = ? AND activa = true",
                    Integer.class, entrada.idCaja());
            if (!entrada.idAgenciaOperacion().equals(agenciaCaja)) {
                throw new IllegalStateException("La caja no pertenece a la agencia de operación.");
            }
            cuentaCaja = cajasMovimientosService.obtenerCuentaContableCaja(entrada.idCaja().longValue());
        }
        String tipoGlobal = global.caja().signum() > 0 ? "CE" : "NC";
        List<RetiroAsociadoContabilizacionService.ComprobantePreparado> preparados = new ArrayList<>();
        for (DistribucionCuenta distribucion : distribuciones) {
            var cuenta = distribucion.cuenta();
            var parametros = obtenerCuentasContables(cuenta.idCuentaAhorro(), distribucion.cxp().signum() > 0);
            var datos = new RetiroAsociadoContabilizacionService.DatosContabilizacion(
                    cuenta.idCuentaAhorro(), cuenta.idAgencia(), entrada.idDatosPersonal(),
                    parametros.idCuentaProducto(), parametros.idCuentaCxp(),
                    distribucion.caja().signum() > 0 ? cuentaCaja : null,
                    distribucion.caja(), distribucion.cxp(), cuenta.saldoActual(),
                    tipoGlobal, entrada.fechaProceso(), entrada.idAgenciaOperacion(),
                    parametros.idCuentaInteragenciaPropietaria(),
                    distribucion.caja().signum() > 0 && !cuenta.idAgencia().equals(entrada.idAgenciaOperacion())
                            ? obtenerCuentaInteragenciaOperacion(entrada.idAgenciaOperacion(), cuenta.tipoCaptacion())
                            : null);
            preparados.addAll(contabilizacionService.preparar(datos));
        }
        List<RetiroAsociadoPreviewDTO.ComprobantePreview> comprobantes = construirPreviewContable(prepararConceptos(entrada, cuentas, preparados));
        return new RetiroAsociadoPreviewDTO(
                entrada.idDatosPersonal(), entrada.idAgenciaOperacion(), cuentas,
                total, caja, cxp, total.subtract(caja).subtract(cxp), comprobantes,
                entrada.numeroActa(), entrada.fechaActa());
    }

    private List<RetiroAsociadoContabilizacionService.ComprobantePreparado> prepararConceptos(
            RetiroAsociadoEntradaDTO entrada,
            List<RetiroAsociadoRepository.CuentaRetiro> cuentas,
            List<RetiroAsociadoContabilizacionService.ComprobantePreparado> preparados) {
        if (preparados.isEmpty()) return List.of();

        var asociado = jdbc.queryForMap("""
                SELECT documento, nombre_completo
                FROM depositos.vw_depositos_cuentas_ahorro_total
                WHERE id_datos_personal = ?
                LIMIT 1
                """, entrada.idDatosPersonal());
        String documento = String.valueOf(asociado.get("documento")).trim();
        String nombre = String.valueOf(asociado.get("nombre_completo")).trim();

        Map<Integer, String> agencias = new HashMap<>();
        for (var comprobante : preparados) {
            agencias.computeIfAbsent(comprobante.idAgencia(), id -> jdbc.queryForObject(
                    "SELECT nombre_agencia FROM general.datos_agencias WHERE id_agencia = ?",
                    String.class, id));
        }
        agencias.computeIfAbsent(entrada.idAgenciaOperacion(), id -> jdbc.queryForObject(
                "SELECT nombre_agencia FROM general.datos_agencias WHERE id_agencia = ?",
                String.class, id));

        String productos = cuentas.stream()
                .filter(c -> c.saldoActual().signum() > 0)
                .map(RetiroAsociadoRepository.CuentaRetiro::nombreForma)
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));
        var contexto = new RetiroAsociadoContabilizacionService.ContextoConcepto(
                nombre, documento, "R".equals(entrada.estadoFinal()) ? "forzoso" : "voluntario",
                entrada.numeroActa().trim(), entrada.fechaActa(), productos,
                entrada.idAgenciaOperacion(), agencias.get(entrada.idAgenciaOperacion()),
                Map.copyOf(agencias));
        return contabilizacionService.describirComprobantes(
                contabilizacionService.consolidar(preparados), contexto);
    }

    private List<RetiroAsociadoPreviewDTO.ComprobantePreview> construirPreviewContable(
            List<RetiroAsociadoContabilizacionService.ComprobantePreparado> preparados) {
        List<Integer> ids = preparados.stream()
                .flatMap(c -> c.movimientos().stream())
                .map(MovimientoContableDTO::getIdCatalogoCuenta).distinct().toList();
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        Map<Integer, String[]> catalogo = new HashMap<>();
        jdbc.query("SELECT id_catalogo_cuenta, codigo_cuenta, nombre_cuenta "
                        + "FROM contabilidad.catalogo_cuentas WHERE id_catalogo_cuenta IN (" + placeholders + ")",
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> catalogo.put(rs.getInt("id_catalogo_cuenta"),
                        new String[]{rs.getString("codigo_cuenta"), rs.getString("nombre_cuenta")}),
                ids.toArray());
        if (catalogo.size() != ids.size()) {
            throw new IllegalStateException("Existen cuentas contables sin código o nombre en el catálogo.");
        }
        Map<Integer, String> nombresAgencias = new HashMap<>();
        for (var preparado : preparados) {
            nombresAgencias.computeIfAbsent(preparado.idAgencia(), id -> jdbc.queryForObject(
                    "SELECT nombre_agencia FROM general.datos_agencias WHERE id_agencia = ?",
                    String.class, id));
        }
        List<RetiroAsociadoPreviewDTO.ComprobantePreview> resultado = new ArrayList<>();
        for (var preparado : preparados) {
            List<RetiroAsociadoPreviewDTO.MovimientoPreview> movimientos = new ArrayList<>();
            BigDecimal debitos = BigDecimal.ZERO;
            BigDecimal creditos = BigDecimal.ZERO;
            for (var movimiento : preparado.movimientos()) {
                String[] cuenta = catalogo.get(movimiento.getIdCatalogoCuenta());
                if (cuenta == null || cuenta[0] == null || cuenta[1] == null) {
                    throw new IllegalStateException("Cuenta contable sin identificación completa.");
                }
                movimientos.add(new RetiroAsociadoPreviewDTO.MovimientoPreview(
                        cuenta[0], cuenta[1], movimiento.getDetalleMovimiento(),
                        movimiento.getValorDebito(), movimiento.getValorCredito()));
                debitos = debitos.add(movimiento.getValorDebito());
                creditos = creditos.add(movimiento.getValorCredito());
            }
            if (debitos.signum() <= 0 || debitos.compareTo(creditos) != 0) {
                throw new IllegalStateException("El comprobante previsto no está cuadrado.");
            }
            resultado.add(new RetiroAsociadoPreviewDTO.ComprobantePreview(
                    preparado.idAgencia(), nombresAgencias.get(preparado.idAgencia()),
                    preparado.tipoComprobante(), preparado.concepto(),
                    List.copyOf(movimientos), debitos, creditos));
        }
        return List.copyOf(resultado);
    }

    /**
     * Bloquea y valida la totalidad de las cuentas.
     *
     * Debe invocarse desde el método transaccional
     * que ejecutará el retiro.
     */
    @Transactional
    public List<RetiroAsociadoRepository.CuentaRetiro>
    bloquearYValidar(RetiroAsociadoEntradaDTO entrada) {

        validarIdentificacion(entrada);

        List<RetiroAsociadoRepository.CuentaRetiro> cuentas = seleccionarPendientes(
                repository.bloquearCuentas(entrada.idDatosPersonal()));

        validarCuentas(cuentas);
        validarDistribucion(entrada, cuentas);

        return cuentas;
    }


    /**
     * Cancela todos los saldos positivos en una única transacción.
     * La distribución por cuenta se calcula en el servidor.
     */
    @Transactional(rollbackFor = Exception.class)
    public void aplicar(RetiroAsociadoEntradaDTO entrada) {
        validarIdentificacion(entrada);
        Integer idUsuario = usuarioSesionService.idUsuario();
        if (idUsuario == null || idUsuario <= 0) {
            throw new IllegalStateException("No fue posible identificar al usuario autenticado.");
        }

        List<RetiroAsociadoRepository.CuentaRetiro> cuentas = seleccionarPendientes(
                repository.bloquearCuentas(entrada.idDatosPersonal()));
        validarCuentas(cuentas);
        DistribucionGlobal global = validarDistribucion(entrada, cuentas);
        List<DistribucionCuenta> distribuciones = distribuir(cuentas, global.caja());

        Integer cuentaCaja = null;
        if (global.caja().signum() > 0) {
            cuentaCaja = cajasMovimientosService.obtenerCuentaContableCaja(
                    entrada.idCaja().longValue());
        }

        Map<Integer, CuentasContablesRetiro> cuentasContables = new HashMap<>();
        for (DistribucionCuenta distribucion : distribuciones) {
            var cuenta = distribucion.cuenta();
            cuentasContables.put(cuenta.idCuentaAhorro(),
                    obtenerCuentasContables(cuenta.idCuentaAhorro(),
                            distribucion.cxp().signum() > 0));
        }

        String tipoGlobal = global.caja().signum() > 0 ? "CE" : "NC";
        if (global.caja().signum() > 0) {
            Integer agenciaCaja = jdbc.queryForObject(
                    "SELECT id_agencia FROM cajas.cajas WHERE id_caja = ? AND activa = true",
                    Integer.class, entrada.idCaja());
            if (!entrada.idAgenciaOperacion().equals(agenciaCaja)) {
                throw new IllegalStateException("La caja no pertenece a la agencia de operación.");
            }
        }
        List<RetiroAsociadoContabilizacionService.ComprobantePreparado> preparados = new ArrayList<>();
        for (DistribucionCuenta distribucion : distribuciones) {
            var cuenta = distribucion.cuenta();
            var parametros = cuentasContables.get(cuenta.idCuentaAhorro());
            var datos = new RetiroAsociadoContabilizacionService.DatosContabilizacion(
                    cuenta.idCuentaAhorro(), cuenta.idAgencia(), entrada.idDatosPersonal(),
                    parametros.idCuentaProducto(), parametros.idCuentaCxp(),
                    distribucion.caja().signum() > 0 ? cuentaCaja : null,
                    distribucion.caja(), distribucion.cxp(), cuenta.saldoActual(),
                    tipoGlobal, entrada.fechaProceso(), entrada.idAgenciaOperacion(),
                    parametros.idCuentaInteragenciaPropietaria(),
                    distribucion.caja().signum() > 0 && !cuenta.idAgencia().equals(entrada.idAgenciaOperacion())
                            ? obtenerCuentaInteragenciaOperacion(entrada.idAgenciaOperacion(), cuenta.tipoCaptacion())
                            : null);
            preparados.addAll(contabilizacionService.preparar(datos));
        }
        Map<RetiroAsociadoContabilizacionService.ClaveComprobante, String> numeros =
                distribuciones.isEmpty() ? Map.of() : contabilizacionService.contabilizarConsolidado(
                        prepararConceptos(entrada, cuentas, preparados), entrada.idDatosPersonal(),
                        distribuciones.get(0).cuenta().idCuentaAhorro(), idUsuario);
        String numeroCaja = numeros.get(new RetiroAsociadoContabilizacionService.ClaveComprobante(
                entrada.idAgenciaOperacion(), "CE"));
        if (global.caja().signum() > 0 && numeroCaja == null) {
            throw new IllegalStateException("No se generó el comprobante de la agencia de Caja.");
        }
        for (DistribucionCuenta distribucion : distribuciones) {
            var cuenta = distribucion.cuenta();
            String tipoCuenta = distribucion.caja().signum() > 0
                    ? (cuenta.idAgencia().equals(entrada.idAgenciaOperacion()) ? "CE" : "CA")
                    : "NC";
            String numeroComprobante = numeros.get(
                    new RetiroAsociadoContabilizacionService.ClaveComprobante(cuenta.idAgencia(), tipoCuenta));
            if (numeroComprobante == null) {
                throw new IllegalStateException("No se generó el comprobante de la cuenta " + cuenta.codigoCuenta());
            }

            DepositosMovimientoDTO movimiento = new DepositosMovimientoDTO();
            movimiento.setIdCuentaAhorro(cuenta.idCuentaAhorro());
            movimiento.setFechaMovimiento(entrada.fechaProceso());
            movimiento.setTipoComprobante(tipoCuenta);
            movimiento.setNumeroComprobante(numeroComprobante);
            movimiento.setTipoMovimiento("551");
            movimiento.setModulo("02");
            movimiento.setTarjeta("N");
            movimiento.setEstablecimiento("RETIRO INTEGRAL ASOCIADO");
            movimiento.setValorDebito(cuenta.saldoActual());
            movimiento.setValorCredito(BigDecimal.ZERO);
            movimiento.setModuloOrigen("DEPOSITOS");
            movimiento.setProcesoOrigen("RETIRO_ASOCIADO");
            movimiento.setIdOrigen(cuenta.idCuentaAhorro().longValue());
            depositosMovimientoService.registrarRetiroIntegral(movimiento, idUsuario);

            if (distribucion.cxp().signum() > 0) {
                tesoreriaOperacionesPendientesService.publicar(
                        cuenta.idAgencia(), "DEPOSITOS", "RETIRO_ASOCIADO",
                        "depositos.cuentas_ahorro", cuenta.idCuentaAhorro().longValue(),
                        entrada.idDatosPersonal(), "SALIDA", entrada.fechaProceso(),
                        "RETIRO ASOCIADO - CUENTA " + cuenta.codigoCuenta(),
                        distribucion.cxp(), idUsuario);
            }

        }

        for (var cuenta : cuentas) {
            repository.actualizarEstadoRetiro(cuenta.idCuentaAhorro(),
                    entrada.estadoFinal(), entrada.fechaProceso(),
                    entrada.comentarioCuentas(), idUsuario);
        }

        jdbc.update("""
                INSERT INTO hoja_vida.vinculaciones_asociados
                    (id_datos_personal, tipo_evento, tipo_retiro, numero_acta,
                     fecha_acta, fecha_efectiva, proceso_origen, observaciones,
                     fk_seguridad_creacion)
                VALUES (?, 'RETIRO', ?, ?, ?, ?, 'RETIRO_INTEGRAL', ?, ?)
                """, entrada.idDatosPersonal(), entrada.estadoFinal(),
                entrada.numeroActa(), entrada.fechaActa(), entrada.fechaProceso(),
                entrada.comentarioCuentas().trim(), idUsuario);

        if (global.caja().signum() > 0) {
            cajasMovimientosService.registrarRetiroIntegralAsociado(
                    entrada.idCaja().longValue(), global.caja(), entrada.fechaProceso(),
                    entrada.idDatosPersonal(), "RETIRO-" + entrada.idDatosPersonal(),
                    tipoGlobal, numeroCaja, idUsuario);
        }
    }

    private List<RetiroAsociadoRepository.CuentaRetiro> seleccionarPendientes(
            List<RetiroAsociadoRepository.CuentaRetiro> cuentas) {
        return cuentas.stream()
                .filter(c -> !Set.of("C", "R").contains(
                        c.estadoCuenta() == null ? "" : c.estadoCuenta().trim()))
                .toList();
    }

    private record DistribucionGlobal(BigDecimal caja, BigDecimal cxp) {}
    private record DistribucionCuenta(RetiroAsociadoRepository.CuentaRetiro cuenta,
                                      BigDecimal caja, BigDecimal cxp) {}

    private List<DistribucionCuenta> distribuir(
            List<RetiroAsociadoRepository.CuentaRetiro> cuentas, BigDecimal valorCaja) {
        List<DistribucionCuenta> resultado = new ArrayList<>();
        BigDecimal restanteCaja = valorCaja;
        for (var cuenta : cuentas.stream()
                .filter(c -> c.saldoActual().signum() > 0)
                .sorted(Comparator.comparing(RetiroAsociadoRepository.CuentaRetiro::idCuentaAhorro))
                .toList()) {
            BigDecimal caja = cuenta.saldoActual().min(restanteCaja);
            BigDecimal cxp = cuenta.saldoActual().subtract(caja);
            resultado.add(new DistribucionCuenta(cuenta, caja, cxp));
            restanteCaja = restanteCaja.subtract(caja);
        }
        if (restanteCaja.signum() != 0) {
            throw new IllegalStateException("La distribución de Caja no coincide con los saldos.");
        }
        return resultado;
    }

    /**
     * Cuentas contables requeridas para cancelar
     * un producto contra una cuenta por pagar.
     */

    private record CuentasContablesRetiro(
            Integer idCuentaProducto,
            Integer idCuentaCxp,
            Integer idCuentaInteragenciaPropietaria
    ) {}

    private CuentasContablesRetiro obtenerCuentasContables(
            Integer idCuentaAhorro, boolean requiereCxp
    ) {

        String sql = """
        SELECT
            fa.cuenta_forma_corto,
            fa.cuenta_forma_largo,
            ca.id_agencia,
            fa.cuenta_interagencia,
            fa.codigo_forma,
            fa.nombre_forma,
            ca.plazo_cuenta
        FROM depositos.cuentas_ahorro ca
        INNER JOIN depositos.formas_ahorro fa
            ON fa.id_forma_ahorro = ca.id_forma_ahorro
        WHERE ca.id_cuenta_ahorro = ?
        """;

        return jdbc.query(
                sql,
                rs -> {
                    if (!rs.next()) {
                        throw new IllegalStateException(
                                "No existe parametrización para la cuenta "
                                        + idCuentaAhorro
                        );
                    }

                    Integer corto =
                            (Integer) rs.getObject("cuenta_forma_corto");

                    Integer largo =
                            (Integer) rs.getObject("cuenta_forma_largo");

                    Integer cxp = null;
                    Integer interagencia =
                            (Integer) rs.getObject("cuenta_interagencia");

                    String producto = rs.getString("nombre_forma");

                    if (corto == null && largo == null) {
                        throw new IllegalStateException(
                                "La forma de ahorro " + producto
                                        + " no tiene cuentas contables configuradas."
                        );
                    }

                    Integer cuentaProducto;
                    if (corto != null && corto.equals(largo)) {
                        cuentaProducto = corto;
                    } else {
                        Integer plazo = rs.getObject("plazo_cuenta", Integer.class);
                        if (plazo == null) {
                            throw new IllegalStateException(
                                    "La cuenta " + idCuentaAhorro
                                            + " requiere plazo para determinar "
                                            + "su cuenta contable."
                            );
                        }
                        cuentaProducto = plazo <= 12 ? corto : largo;
                        if (cuentaProducto == null) {
                            throw new IllegalStateException(
                                    "No está configurada la cuenta contable de "
                                            + (plazo <= 12 ? "corto" : "largo")
                                            + " plazo para " + producto
                            );
                        }
                    }

                    if (requiereCxp) {
                        // El retiro integral implica desvinculación definitiva.
                        // Aportes (forma 01) y depósitos usan cuentas distintas
                        // de remanentes de exasociados, en la agencia propietaria.
                        String codigoCxp = "01".equals(rs.getString("codigo_forma"))
                                ? "24650501" : "24651001";
                        Integer agenciaProducto = rs.getInt("id_agencia");
                        List<Integer> cuentasCxp = jdbc.query(
                                "SELECT id_catalogo_cuenta FROM contabilidad.catalogo_cuentas "
                                        + "WHERE id_agencia = ? AND codigo_cuenta = ? "
                                        + "AND cuenta_operable = true AND naturaleza_cuenta = 'C'",
                                (fila, indice) -> fila.getInt("id_catalogo_cuenta"),
                                agenciaProducto, codigoCxp);
                        if (cuentasCxp.size() != 1) {
                            throw new IllegalStateException(
                                    "Debe existir una única cuenta CXP operable " + codigoCxp
                                            + " en la agencia " + agenciaProducto + ".");
                        }
                        cxp = cuentasCxp.get(0);
                        if (cuentaProducto.equals(cxp)) {
                            throw new IllegalStateException(
                                    "La cuenta CXP coincide con la cuenta del producto " + producto);
                        }
                    }

                    return new CuentasContablesRetiro(cuentaProducto, cxp, interagencia);
                },
                idCuentaAhorro
        );
    }

    private Integer obtenerCuentaInteragenciaOperacion(Integer agencia, String tipoCaptacion) {
        if (tipoCaptacion == null || tipoCaptacion.isBlank()) {
            throw new IllegalStateException("La cuenta no tiene tipo de captación para interagencia.");
        }
        List<Integer> cuentas = jdbc.query("""
                SELECT DISTINCT cuenta_interagencia
                FROM depositos.formas_ahorro
                WHERE id_agencia = ?
                  AND TRIM(tipo_captacion_forma) = TRIM(?)
                  AND cuenta_interagencia IS NOT NULL
                """, (rs, fila) -> rs.getInt(1), agencia, tipoCaptacion);
        if (cuentas.size() != 1) {
            throw new IllegalStateException("Debe existir una única cuenta interagencia para "
                    + tipoCaptacion + " en la agencia " + agencia + ".");
        }
        return cuentas.get(0);
    }

    private void validarIdentificacion(
            RetiroAsociadoEntradaDTO entrada
    ) {
        if (entrada == null
                || entrada.idDatosPersonal() == null
                || entrada.idDatosPersonal() <= 0) {
            throw new IllegalArgumentException(
                    "Debe identificar al asociado."
            );
        }

        if (entrada.idAgenciaOperacion() == null) {
            throw new IllegalArgumentException(
                    "Debe identificar la agencia de operación."
            );
        }

        usuarioSesionService.validarAgencia(
                entrada.idAgenciaOperacion()
        );

        if (entrada.fechaProceso() == null
                || !LocalDate.now().equals(entrada.fechaProceso())) {
            throw new IllegalArgumentException(
                    "La fecha del retiro debe ser la fecha actual."
            );
        }

        if (!Set.of("C", "R").contains(entrada.estadoFinal())) {
            throw new IllegalArgumentException(
                    "Debe seleccionar el estado final: "
                            + "C (Cancelado) o R (Retirado)."
            );
        }

        if (entrada.numeroActa() == null || entrada.numeroActa().isBlank()
                || entrada.fechaActa() == null) {
            throw new IllegalArgumentException("Debe indicar número y fecha del acta de retiro.");
        }
        if (entrada.fechaActa().isAfter(entrada.fechaProceso())) {
            throw new IllegalArgumentException("La fecha del acta no puede ser posterior al retiro.");
        }

        if (entrada.comentarioCuentas() == null
                || entrada.comentarioCuentas().isBlank()) {
            throw new IllegalArgumentException(
                    "Debe registrar el motivo del retiro del asociado."
            );
        }

        if (entrada.comentarioCuentas().trim().length() > 500) {
            throw new IllegalArgumentException(
                    "El comentario no puede superar 500 caracteres."
            );
        }

    }

    private Integer validarCuentas(
            List<RetiroAsociadoRepository.CuentaRetiro> cuentas
    ) {
        if (cuentas == null || cuentas.isEmpty()) {
            throw new IllegalStateException(
                    "El asociado no tiene cuentas registradas."
            );
        }

        Integer agenciaOrigen = null;
        for (var cuenta : cuentas) {
            BigDecimal saldo = cuenta.saldoActual();
            if (saldo == null || saldo.signum() < 0) {
                throw new IllegalStateException("Saldo inválido en la cuenta " + cuenta.codigoCuenta());
            }
            if (cuenta.idAgencia() == null) {
                throw new IllegalStateException("La cuenta " + cuenta.codigoCuenta() + " no tiene agencia.");
            }
            if (saldo.signum() > 0 && !Boolean.TRUE.equals(cuenta.estadoOperativo())) {
                throw new IllegalStateException("La cuenta " + cuenta.codigoCuenta()
                        + " tiene saldo y su estado no permite movimientos.");
            }
            if (agenciaOrigen == null) agenciaOrigen = cuenta.idAgencia();
        }
        return agenciaOrigen;
    }

    private DistribucionGlobal validarDistribucion(
            RetiroAsociadoEntradaDTO entrada,
            List<RetiroAsociadoRepository.CuentaRetiro> cuentas) {
        if ((entrada.mediosPago() == null || entrada.mediosPago().isEmpty())
                && sumarSaldos(cuentas).signum() > 0) {
            throw new IllegalArgumentException("Debe informar el valor destinado a Caja o CXP.");
        }
        BigDecimal caja = sumarMedio(entrada, "CAJA");
        BigDecimal cxp = sumarMedio(entrada, "CXP");
        BigDecimal total = sumarSaldos(cuentas);
        if (caja.add(cxp).compareTo(total) != 0) {
            throw new IllegalArgumentException(
                    "Los valores de Caja y CXP deben sumar el saldo total del asociado.");
        }
        if (caja.signum() > 0 && (entrada.idCaja() == null || entrada.idCaja() <= 0)) {
            throw new IllegalArgumentException("Debe seleccionar una caja para el pago en efectivo.");
        }
        return new DistribucionGlobal(caja, cxp);
    }

    private BigDecimal sumarSaldos(
            List<RetiroAsociadoRepository.CuentaRetiro> cuentas
    ) {
        return cuentas.stream()
                .filter(c -> c.saldoActual().signum() > 0)
                .map(RetiroAsociadoRepository.CuentaRetiro::saldoActual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumarMedio(
            RetiroAsociadoEntradaDTO entrada,
            String destino
    ) {
        if (entrada.mediosPago() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (var medio : entrada.mediosPago()) {
            if (medio == null || medio.destino() == null
                    || medio.valor() == null
                    || medio.valor().signum() < 0
                    || !Set.of("CAJA", "CXP").contains(medio.destino())) {
                throw new IllegalArgumentException(
                        "Medio de pago inválido."
                );
            }

            if (destino.equals(medio.destino())) {
                total = total.add(medio.valor());
            }
        }

        return total;
    }
}
