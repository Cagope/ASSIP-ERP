package co.assip.erp.cajas.cierre;

import co.assip.erp.cajas.cierre.dto.CajaCierreComprobantesResumenDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreConfirmarDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreEfectivoDetalleDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreMovimientoResumenDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreMovimientosDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreResumenDTO;
import co.assip.erp.cajas.cierre.dto.CajaCierreVentanillaDTO;
import co.assip.erp.cajas.provisiones.CajasProvisionService;
import co.assip.erp.cajas.provisiones.CajasUsuariosService;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CajasCierreService {

    private final CajasCierreRepository repository;
    private final CajasProvisionService cajasProvisionService;
    private final CajasUsuariosService cajasUsuariosService;
    private final UsuarioSesionService usuarioSesionService;
    private final AccessValidator accessValidator;
    private final UsuarioService usuarioService;

    // =========================================================
    // PREPARAR CIERRE POR FECHA CONTABLE
    // =========================================================

    public CajaCierreResumenDTO prepararCierre(
            LocalDate fechaContable
    ) {

        if (fechaContable == null) {
            throw new RuntimeException(
                    "Debe indicar la fecha contable."
            );
        }

        Usuario usuario =
                obtenerUsuarioAutorizadoCierre();

        boolean puedeCerrarOtrasCajas =
                accessValidator.tienePermiso(
                        usuario,
                        "CAJAS_CIERRE_OTRAS_CAJAS"
                );

        /*
         * Usuario con alcance ampliado:
         *
         * no se asigna automáticamente ninguna caja.
         *
         * Debe seleccionar explícitamente la caja
         * que desea trabajar mediante /disponibles.
         */
        if (puedeCerrarOtrasCajas) {
            return null;
        }

        /*
         * Usuario normal de Caja:
         *
         * únicamente puede trabajar la caja que tuvo
         * vinculada durante la fecha contable indicada.
         */
        CajaUsuarioDTO cajaTrabajada =
                cajasUsuariosService
                        .obtenerUsuarioPorFechaSiExiste(
                                fechaContable
                        );

        if (cajaTrabajada == null) {
            throw new RuntimeException(
                    "El usuario no tiene una caja trabajada "
                            + "para la fecha contable indicada."
            );
        }

        /*
         * La caja trabajada también debe pertenecer
         * al alcance de agencias del usuario.
         */
        usuarioSesionService.validarAgencia(
                cajaTrabajada.getIdAgencia()
        );

        /*
         * Buscamos exclusivamente la provisión abierta
         * correspondiente a esa caja y esa fecha.
         */
        List<CajasProvisionListDTO> provisiones =
                cajasProvisionService
                        .listarCajasAbiertas(
                                cajaTrabajada.getIdAgencia(),
                                fechaContable
                        );

        CajasProvisionListDTO provision =
                provisiones.stream()
                        .filter(item ->
                                cajaTrabajada.getIdCaja()
                                        .equals(
                                                item.getIdCaja()
                                        )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "La caja trabajada por el usuario "
                                                + "no tiene una provisión abierta "
                                                + "para la fecha contable indicada."
                                )
                        );

        return obtenerResumen(
                provision.getIdProvision()
        );
    }

    // =========================================================
    // LISTAR CAJAS DISPONIBLES PARA CIERRE
    // =========================================================

    public List<CajasProvisionListDTO> listarCajasDisponibles(
            LocalDate fechaContable
    ) {

        if (fechaContable == null) {
            throw new RuntimeException(
                    "Debe indicar la fecha contable."
            );
        }

        Usuario usuario =
                obtenerUsuarioAutorizadoCierre();

        /*
         * Este listado permite seleccionar una caja distinta
         * a la trabajada por el usuario.
         */
        if (!accessValidator.tienePermiso(
                usuario,
                "CAJAS_CIERRE_OTRAS_CAJAS"
        )) {

            throw new SecurityException(
                    "El usuario no está autorizado para seleccionar otras cajas."
            );
        }

        List<Integer> agencias =
                usuarioSesionService.agencias();

        if (agencias == null || agencias.isEmpty()) {
            throw new RuntimeException(
                    "El usuario no tiene agencias autorizadas."
            );
        }

        return agencias.stream()
                .flatMap(idAgencia ->
                        cajasProvisionService
                                .listarCajasAbiertas(
                                        idAgencia,
                                        fechaContable
                                )
                                .stream()
                )
                .toList();
    }


    // =========================================================
    // DENOMINACIONES DE BILLETES
    // =========================================================

    public List<BigDecimal> listarDenominacionesBilletes() {

        obtenerUsuarioAutorizadoCierre();

        List<BigDecimal> denominaciones =
                repository
                        .listarDenominacionesBilletesActivas();

        if (denominaciones.isEmpty()) {
            throw new RuntimeException(
                    "No existen denominaciones de billetes activas "
                            + "configuradas para el cierre de caja."
            );
        }

        return denominaciones;
    }

    // =========================================================
    // CONSULTAR RESUMEN POR PROVISIÓN
    // =========================================================

    public CajaCierreResumenDTO obtenerResumen(
            Long idProvision
    ) {

        validarIdProvision(idProvision);

        CajaCierreResumenDTO resumen =
                calcularResumen(idProvision);

        Usuario usuario =
                obtenerUsuarioAutorizadoCierre();

        validarAlcanceCierre(
                usuario,
                resumen
        );

        return resumen;
    }

    // =========================================================
    // CONFIRMAR CIERRE
    // =========================================================

    @Transactional
    public CajaCierreResumenDTO confirmar(
            CajaCierreConfirmarDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar los datos del cierre."
            );
        }

        validarIdProvision(
                dto.getIdProvision()
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * Desde este punto la provisión queda bloqueada.
         *
         * El bloqueo se realiza ANTES de volver a calcular
         * el resumen definitivo.
         */
        repository.bloquearProvisionParaCierre(
                dto.getIdProvision()
        );

        CajaCierreResumenDTO resumen =
                calcularResumen(
                        dto.getIdProvision()
                );

        /*
         * La autorización se vuelve a validar después
         * de bloquear la provisión y recalcular su estado.
         *
         * Esto evita que un idProvision enviado directamente
         * permita cerrar una caja fuera del alcance del usuario.
         */
        Usuario usuario =
                obtenerUsuarioAutorizadoCierre();

        validarAlcanceCierre(
                usuario,
                resumen
        );

        if (!"ABIERTA".equalsIgnoreCase(
                resumen.getEstado()
        )) {
            throw new RuntimeException(
                    "La caja no se encuentra abierta."
            );
        }

        if (resumen.getMovimientosPendientes() != null
                && resumen.getMovimientosPendientes() > 0) {

            throw new RuntimeException(
                    "No es posible cerrar la caja porque existen "
                            + resumen.getMovimientosPendientes()
                            + " movimientos pendientes de confirmar."
            );
        }

        if (resumen.getEfectivoFinCalculado()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "El cierre produce un saldo de efectivo negativo. "
                            + "Revise los movimientos de la fecha contable."
            );
        }

        if (resumen.getChequesFinCalculado()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "El cierre produce un saldo de cheques negativo. "
                            + "Revise los movimientos de la fecha contable."
            );
        }

        /*
         * Validamos y recalculamos la composición física.
         *
         * Para BILLETE:
         * valorTotal = denominacion * cantidad.
         *
         * Para VARIOS y MONEDAS:
         * cantidad = 1
         * denominacion = 0
         * valorTotal = valor capturado.
         */
        List<DetalleEfectivoCalculado> detalleCalculado =
                validarYCalcularDetalle(
                        dto.getEfectivoDetalle()
                );

        BigDecimal efectivoContado =
                detalleCalculado.stream()
                        .map(
                                DetalleEfectivoCalculado::valorTotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal diferencia =
                efectivoContado.subtract(
                        resumen.getEfectivoFinCalculado()
                );

        if (diferencia.compareTo(
                BigDecimal.ZERO
        ) != 0) {

            throw new RuntimeException(
                    "El efectivo contado no cuadra con el efectivo calculado. "
                            + "Efectivo calculado: "
                            + resumen.getEfectivoFinCalculado()
                            + ". Efectivo contado: "
                            + efectivoContado
                            + ". Diferencia: "
                            + diferencia
            );
        }

        /*
         * Solo después de que el arqueo cuadre se persiste
         * la composición física del efectivo.
         *
         * Todo forma parte de la misma transacción.
         */
        for (DetalleEfectivoCalculado detalle :
                detalleCalculado) {

            repository.insertarDetalleEfectivo(
                    resumen.getIdProvision(),
                    detalle.tipoDetalle(),
                    detalle.denominacion(),
                    detalle.cantidad(),
                    detalle.valorTotal(),
                    idUsuario
            );
        }

        int actualizados =
                repository.cerrarProvision(
                        resumen.getIdProvision(),
                        resumen.getEfectivoFinCalculado(),
                        resumen.getChequesFinCalculado(),
                        resumen.getMovimientos()
                                .getTotalIngresos(),
                        resumen.getMovimientos()
                                .getTotalEgresos(),
                        normalizarObservacion(
                                dto.getObservacion()
                        ),
                        idUsuario
                );

        if (actualizados != 1) {
            throw new RuntimeException(
                    "No fue posible cerrar la provisión de la caja."
            );
        }

        /*
         * IMPORTANTE:
         *
         * Una provisión histórica NO debe finalizar la
         * vinculación que el usuario tenga actualmente.
         *
         * Solo finalizamos la jornada actual cuando:
         *
         * 1. La provisión corresponde a CURRENT_DATE.
         * 2. Existe una vinculación activa del usuario.
         * 3. Esa vinculación corresponde exactamente
         *    a la misma caja que acabamos de cerrar.
         */
        finalizarVinculacionActualSiCorresponde(
                resumen
        );

        return calcularResumen(
                dto.getIdProvision()
        );
    }

    // =========================================================
    // CÁLCULO DEL RESUMEN
    // =========================================================

    private CajaCierreResumenDTO calcularResumen(
            Long idProvision
    ) {

        CajaCierreResumenDTO cabecera =
                repository
                        .obtenerCabeceraProvision(
                                idProvision
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No se encontró la provisión de caja."
                                )
                        );

        CajaCierreMovimientosDTO movimientos =
                repository.calcularMovimientos(
                        idProvision
                );

        List<CajaCierreMovimientoResumenDTO>
                resumenMovimientos =
                repository.obtenerResumenMovimientos(
                        idProvision
                );

        List<CajaCierreComprobantesResumenDTO>
                resumenComprobantes =
                repository.obtenerResumenComprobantes(
                        idProvision
                );

        BigDecimal efectivoFin =
                cabecera.getEfectivoInicio()
                        .add(
                                movimientos
                                        .getEntradasEfectivo()
                        )
                        .subtract(
                                movimientos
                                        .getSalidasEfectivo()
                        );

        BigDecimal chequesFin =
                cabecera.getChequesInicio()
                        .add(
                                movimientos
                                        .getEntradasCheques()
                        )
                        .subtract(
                                movimientos
                                        .getSalidasCheques()
                        );

        CajaCierreVentanillaDTO ventanilla =
                repository.calcularVentanilla(
                        idProvision,
                        efectivoFin
                );

        int pendientes =
                repository.contarMovimientosPendientes(
                        idProvision
                );

        return CajaCierreResumenDTO.builder()
                .idProvision(
                        cabecera.getIdProvision()
                )
                .idCaja(
                        cabecera.getIdCaja()
                )
                .idAgencia(
                        cabecera.getIdAgencia()
                )
                .codigoCaja(
                        cabecera.getCodigoCaja()
                )
                .descripcionCaja(
                        cabecera.getDescripcionCaja()
                )
                .tipoCaja(
                        cabecera.getTipoCaja()
                )
                .fechaContable(
                        cabecera.getFechaContable()
                )
                .estado(
                        cabecera.getEstado()
                )
                .efectivoInicio(
                        cabecera.getEfectivoInicio()
                )
                .chequesInicio(
                        cabecera.getChequesInicio()
                )
                .movimientos(
                        movimientos
                )
                .resumenMovimientos(
                        resumenMovimientos
                )
                .resumenComprobantes(
                        resumenComprobantes
                )
                .ventanilla(
                        ventanilla
                )
                .efectivoFinCalculado(
                        efectivoFin
                )
                .chequesFinCalculado(
                        chequesFin
                )
                .totalFisicoFinal(
                        efectivoFin.add(
                                chequesFin
                        )
                )
                .movimientosPendientes(
                        pendientes
                )
                .build();
    }

    // =========================================================
    // FINALIZAR VINCULACIÓN ACTUAL CUANDO CORRESPONDA
    // =========================================================

    private void finalizarVinculacionActualSiCorresponde(
            CajaCierreResumenDTO resumen
    ) {

        if (resumen.getFechaContable() == null
                || !resumen.getFechaContable()
                .equals(LocalDate.now())) {

            return;
        }

        CajaUsuarioDTO vinculacionActual =
                cajasUsuariosService
                        .obtenerActivaUsuarioSiExiste();

        if (vinculacionActual == null) {
            return;
        }

        if (!resumen.getIdCaja().equals(
                vinculacionActual.getIdCaja()
        )) {
            return;
        }

        cajasUsuariosService
                .finalizarPorCierreDefinitivo();
    }

    // =========================================================
// VALIDACIÓN Y CÁLCULO DEL EFECTIVO
// =========================================================

    private List<DetalleEfectivoCalculado>
    validarYCalcularDetalle(
            List<CajaCierreEfectivoDetalleDTO> detalle
    ) {

        if (detalle == null || detalle.isEmpty()) {
            throw new RuntimeException(
                    "Debe registrar la composición del efectivo del cierre."
            );
        }

        /*
         * Las denominaciones válidas no se encuentran
         * codificadas en Java.
         *
         * Se obtienen del catálogo activo de PostgreSQL.
         */
        List<BigDecimal> denominacionesActivas =
                repository
                        .listarDenominacionesBilletesActivas()
                        .stream()
                        .map(BigDecimal::stripTrailingZeros)
                        .toList();

        if (denominacionesActivas.isEmpty()) {
            throw new RuntimeException(
                    "No existen denominaciones de billetes activas "
                            + "configuradas para el cierre de caja."
            );
        }

        /*
         * Debe llegar exactamente:
         *
         * - una fila por cada denominación activa
         * - una fila VARIOS
         * - una fila MONEDAS
         */
        int cantidadEsperada =
                denominacionesActivas.size() + 2;

        if (detalle.size() != cantidadEsperada) {
            throw new RuntimeException(
                    "La composición del efectivo está incompleta. "
                            + "Debe contener todas las denominaciones "
                            + "de billetes activas, billetes varios y monedas."
            );
        }

        Set<BigDecimal> denominacionesValidas =
                new HashSet<>(
                        denominacionesActivas
                );

        Set<String> conceptosEncontrados =
                new HashSet<>();

        List<DetalleEfectivoCalculado> resultado =
                detalle.stream()
                        .map(item ->
                                validarYCalcularDetalleItem(
                                        item,
                                        denominacionesValidas,
                                        conceptosEncontrados
                                )
                        )
                        .toList();

        /*
         * La cantidad de conceptos únicos debe coincidir
         * exactamente con la composición esperada.
         *
         * Esto detecta duplicados y conceptos faltantes.
         */
        if (conceptosEncontrados.size()
                != cantidadEsperada) {

            throw new RuntimeException(
                    "La composición del efectivo está incompleta "
                            + "o contiene conceptos duplicados."
            );
        }

        /*
         * Además de comprobar la cantidad, verificamos que
         * todas las denominaciones activas estén presentes.
         *
         * De esta forma no es posible reemplazar una
         * denominación requerida por otra.
         */
        for (BigDecimal denominacion :
                denominacionesActivas) {

            String concepto =
                    "BILLETE:"
                            + denominacion.toPlainString();

            if (!conceptosEncontrados.contains(
                    concepto
            )) {

                throw new RuntimeException(
                        "Falta registrar la denominación de billete "
                                + denominacion.toPlainString()
                                + "."
                );
            }
        }

        if (!conceptosEncontrados.contains(
                "VARIOS"
        )) {
            throw new RuntimeException(
                    "Falta registrar el valor de billetes varios."
            );
        }

        if (!conceptosEncontrados.contains(
                "MONEDAS"
        )) {
            throw new RuntimeException(
                    "Falta registrar el valor de monedas."
            );
        }

        return resultado;
    }


    private DetalleEfectivoCalculado
    validarYCalcularDetalleItem(
            CajaCierreEfectivoDetalleDTO item,
            Set<BigDecimal> denominacionesValidas,
            Set<String> conceptosEncontrados
    ) {

        if (item == null) {
            throw new RuntimeException(
                    "Existe un registro inválido "
                            + "en la composición del efectivo."
            );
        }

        if (item.getTipoDetalle() == null
                || item.getTipoDetalle().isBlank()) {

            throw new RuntimeException(
                    "Debe indicar el tipo del detalle de efectivo."
            );
        }

        String tipoDetalle =
                item.getTipoDetalle()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if ("BILLETE".equals(
                tipoDetalle
        )) {

            return validarBillete(
                    item,
                    denominacionesValidas,
                    conceptosEncontrados
            );
        }

        if ("VARIOS".equals(tipoDetalle)
                || "MONEDAS".equals(tipoDetalle)) {

            return validarValorLibre(
                    item,
                    tipoDetalle,
                    conceptosEncontrados
            );
        }

        throw new RuntimeException(
                "Tipo de detalle de efectivo no válido: "
                        + tipoDetalle
        );
    }


    private DetalleEfectivoCalculado validarBillete(
            CajaCierreEfectivoDetalleDTO item,
            Set<BigDecimal> denominacionesValidas,
            Set<String> conceptosEncontrados
    ) {

        if (item.getDenominacion() == null) {
            throw new RuntimeException(
                    "Debe indicar la denominación del billete."
            );
        }

        BigDecimal denominacion =
                item.getDenominacion()
                        .stripTrailingZeros();

        if (denominacion.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new RuntimeException(
                    "La denominación del billete debe ser mayor que cero."
            );
        }

        if (!denominacionesValidas.contains(
                denominacion
        )) {

            throw new RuntimeException(
                    "La denominación de billete "
                            + item.getDenominacion()
                            + " no se encuentra activa."
            );
        }

        if (item.getCantidad() == null
                || item.getCantidad() < 0) {

            throw new RuntimeException(
                    "La cantidad de billetes no puede ser negativa."
            );
        }

        String concepto =
                "BILLETE:"
                        + denominacion.toPlainString();

        if (!conceptosEncontrados.add(
                concepto
        )) {

            throw new RuntimeException(
                    "La denominación "
                            + denominacion.toPlainString()
                            + " está repetida."
            );
        }

        /*
         * El valor enviado por Angular no es autoritativo.
         *
         * El backend calcula nuevamente:
         *
         * denominación × cantidad
         */
        BigDecimal valorTotal =
                denominacion.multiply(
                        BigDecimal.valueOf(
                                item.getCantidad()
                        )
                );

        return new DetalleEfectivoCalculado(
                "BILLETE",
                denominacion,
                item.getCantidad(),
                valorTotal
        );
    }


    private DetalleEfectivoCalculado validarValorLibre(
            CajaCierreEfectivoDetalleDTO item,
            String tipoDetalle,
            Set<String> conceptosEncontrados
    ) {

        if (!conceptosEncontrados.add(
                tipoDetalle
        )) {

            throw new RuntimeException(
                    "El concepto "
                            + tipoDetalle
                            + " está repetido."
            );
        }

        BigDecimal valorTotal =
                item.getValorTotal() != null
                        ? item.getValorTotal()
                        : BigDecimal.ZERO;

        if (valorTotal.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new RuntimeException(
                    "El valor de "
                            + tipoDetalle.toLowerCase(
                            Locale.ROOT
                    )
                            + " no puede ser negativo."
            );
        }

        /*
         * VARIOS y MONEDAS son capturas consolidadas.
         *
         * El backend no confía en denominación ni cantidad
         * enviadas por el frontend y las normaliza.
         */
        return new DetalleEfectivoCalculado(
                tipoDetalle,
                BigDecimal.ZERO,
                1,
                valorTotal
        );
    }

    // =========================================================
    // AUTORIZACIÓN DEL CIERRE
    // =========================================================

    /**
     * Obtiene el usuario autenticado y exige el permiso
     * funcional para ejecutar cierres de caja.
     */
    private Usuario obtenerUsuarioAutorizadoCierre() {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Usuario usuario =
                usuarioService
                        .buscarPorId(idUsuario)
                        .orElseThrow(() ->
                                new SecurityException(
                                        "El usuario autenticado no existe."
                                )
                        );

        accessValidator.validarAcceso(
                usuario,
                "CAJAS_CIERRE_EXECUTE"
        );

        return usuario;
    }


    /**
     * Valida el alcance del usuario sobre una provisión.
     *
     * CAJAS_CIERRE_OTRAS_CAJAS:
     * permite trabajar cualquier caja abierta perteneciente
     * a una agencia autorizada para el usuario.
     *
     * Sin ese permiso:
     * únicamente puede trabajar la caja que el usuario
     * tuvo vinculada en la fecha contable de la provisión.
     */
    private void validarAlcanceCierre(
            Usuario usuario,
            CajaCierreResumenDTO resumen
    ) {

        if (usuario == null
                || resumen == null) {

            throw new SecurityException(
                    "No fue posible validar la autorización del cierre."
            );
        }

        /*
         * La agencia siempre se valida.
         *
         * Tener permiso para cerrar otras cajas no permite
         * salir del alcance de agencias autorizado al usuario.
         */
        usuarioSesionService.validarAgencia(
                resumen.getIdAgencia()
        );

        boolean puedeCerrarOtrasCajas =
                accessValidator.tienePermiso(
                        usuario,
                        "CAJAS_CIERRE_OTRAS_CAJAS"
                );

        if (puedeCerrarOtrasCajas) {
            return;
        }

        /*
         * Usuario sin alcance ampliado:
         * debe haber trabajado una caja en esa fecha.
         *
         * La vinculación puede estar ACTIVA o FINALIZADA.
         */
        CajaUsuarioDTO cajaTrabajada =
                cajasUsuariosService
                        .obtenerUsuarioPorFechaSiExiste(
                                resumen.getFechaContable()
                        );

        if (cajaTrabajada == null) {
            throw new SecurityException(
                    "El usuario no tiene una caja trabajada "
                            + "para la fecha contable indicada."
            );
        }

        if (!resumen.getIdCaja().equals(
                cajaTrabajada.getIdCaja()
        )) {

            throw new SecurityException(
                    "El usuario no está autorizado para cerrar esta caja."
            );
        }
    }

    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    private void validarIdProvision(
            Long idProvision
    ) {

        if (idProvision == null
                || idProvision <= 0) {

            throw new RuntimeException(
                    "Debe indicar una provisión válida."
            );
        }
    }

    private String normalizarObservacion(
            String observacion
    ) {

        if (observacion == null) {
            return null;
        }

        String valor =
                observacion.trim();

        if (valor.isEmpty()) {
            return null;
        }

        if (valor.length() > 500) {
            throw new RuntimeException(
                    "La observación del cierre "
                            + "no puede superar 500 caracteres."
            );
        }

        return valor;
    }

    // =========================================================
    // DETALLE CALCULADO
    // =========================================================

    private record DetalleEfectivoCalculado(
            String tipoDetalle,
            BigDecimal denominacion,
            Integer cantidad,
            BigDecimal valorTotal
    ) {
    }
}