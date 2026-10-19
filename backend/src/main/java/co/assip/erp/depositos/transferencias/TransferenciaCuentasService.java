
package co.assip.erp.depositos.transferencias;

import co.assip.erp.cajas.captura_depositos.CajaCapturaDepositosRepository;
import co.assip.erp.depositos.movimientos.DepositosMovimientoService;
import co.assip.erp.depositos.movimientos.dto.DepositosMovimientoDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasCuentaDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasPreviewDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasRequestDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasResultadoDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


// =========================================================
// SERVICIO
// TRANSFERENCIA DE FONDOS ENTRE CUENTAS
// =========================================================
//
// Responsabilidades:
//
// 1. Validar los datos de la transferencia.
// 2. Consultar y validar las cuentas.
// 3. Verificar el saldo disponible.
// 4. Preparar la contabilización.
// 5. Registrar los comprobantes contables.
// 6. Registrar el débito y el crédito.
// 7. Retornar el resultado de la operación.
//
// Reglas:
//
// - La cuenta origen debe ser de ahorro.
// - La cuenta destino puede ser de ahorro,
//   aportes sociales o TAC.
// - No se permite transferir a la misma cuenta.
// - La operación debe utilizar la fecha del servidor.
// - La agencia origen debe estar autorizada.
// - Toda la aplicación es transaccional.
//
// =========================================================

@Service
@RequiredArgsConstructor
public class TransferenciaCuentasService {

    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final TransferenciaCuentasRepository repository;

    private final TransferenciaCuentasContabilidadService contabilidad;

    private final DepositosMovimientoService movimientos;

    private final CajaCapturaDepositosRepository capturaRepository;

    private final UsuarioSesionService sesion;


    // =====================================================
    // ESTRUCTURA INTERNA DEL ANÁLISIS
    // =====================================================

    private record Analisis(
            TransferenciaCuentasRepository.Cuenta origen,
            TransferenciaCuentasRepository.Cuenta destino,
            BigDecimal saldoDisponible
    ) {
    }


    // =====================================================
    // VALIDAR DATOS DE ENTRADA
    // =====================================================

    private void validarEntrada(
            TransferenciaCuentasRequestDTO entrada
    ) {

        if (entrada == null) {
            throw new IllegalArgumentException(
                    "Debe suministrar los datos de la transferencia."
            );
        }

        if (entrada.idCuentaOrigen() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar la cuenta origen."
            );
        }

        if (entrada.idCuentaDestino() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar la cuenta destino."
            );
        }

        if (entrada.idCuentaOrigen().equals(
                entrada.idCuentaDestino()
        )) {
            throw new IllegalArgumentException(
                    "La cuenta origen y la cuenta destino deben ser diferentes."
            );
        }

        if (entrada.valor() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el valor de la transferencia."
            );
        }

        if (entrada.valor().signum() <= 0) {
            throw new IllegalArgumentException(
                    "El valor de la transferencia debe ser mayor que cero."
            );
        }

        if (entrada.valor().scale() > 2) {
            throw new IllegalArgumentException(
                    "El valor de la transferencia no puede tener más de dos decimales."
            );
        }

        if (entrada.fechaProceso() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha de la transferencia."
            );
        }

        if (!LocalDate.now().equals(
                entrada.fechaProceso()
        )) {
            throw new IllegalArgumentException(
                    "La transferencia debe utilizar la fecha actual del servidor."
            );
        }

        if (
                entrada.concepto() == null
                        || entrada.concepto().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Debe indicar el concepto de la transferencia."
            );
        }

        if (entrada.concepto().trim().length() > 200) {
            throw new IllegalArgumentException(
                    "El concepto de la transferencia no puede superar 200 caracteres."
            );
        }
    }


    // =====================================================
    // CONSULTAR CUENTAS
    // =====================================================
    //
    // Las cuentas se consultan en orden ascendente de ID.
    //
    // Cuando bloqueo = true, el repositorio debe
    // bloquear los registros para evitar operaciones
    // simultáneas sobre las mismas cuentas.
    //
    // El orden fijo evita interbloqueos cuando existen
    // transferencias inversas entre dos cuentas.
    //
    // =====================================================

    private Analisis analizar(
            TransferenciaCuentasRequestDTO entrada,
            boolean bloqueo
    ) {

        validarEntrada(entrada);

        Integer idPrimeraCuenta = Math.min(
                entrada.idCuentaOrigen(),
                entrada.idCuentaDestino()
        );

        Integer idSegundaCuenta = Math.max(
                entrada.idCuentaOrigen(),
                entrada.idCuentaDestino()
        );


        // -------------------------------------------------
        // CONSULTAR CUENTAS
        // -------------------------------------------------

        TransferenciaCuentasRepository.Cuenta primeraCuenta =
                repository.obtener(
                        idPrimeraCuenta,
                        bloqueo
                );

        TransferenciaCuentasRepository.Cuenta segundaCuenta =
                repository.obtener(
                        idSegundaCuenta,
                        bloqueo
                );


        // -------------------------------------------------
        // IDENTIFICAR ORIGEN Y DESTINO
        // -------------------------------------------------

        TransferenciaCuentasRepository.Cuenta origen;

        TransferenciaCuentasRepository.Cuenta destino;

        if (entrada.idCuentaOrigen().equals(
                idPrimeraCuenta
        )) {

            origen = primeraCuenta;
            destino = segundaCuenta;

        } else {

            origen = segundaCuenta;
            destino = primeraCuenta;
        }


        // -------------------------------------------------
        // VALIDAR AGENCIA DEL USUARIO
        // -------------------------------------------------

        sesion.validarAgencia(
                origen.agencia()
        );


        // -------------------------------------------------
        // VALIDAR ESTADO DE LAS CUENTAS
        // -------------------------------------------------

        if (!origen.operativa()) {
            throw new IllegalStateException(
                    "La cuenta origen no se encuentra operativa."
            );
        }

        if (!destino.operativa()) {
            throw new IllegalStateException(
                    "La cuenta destino no se encuentra operativa."
            );
        }


        // -------------------------------------------------
        // VALIDAR TIPO DE CAPTACIÓN DE ORIGEN
        // -------------------------------------------------
        //
        // Solo las cuentas de ahorro permiten transferencias
        // de salida.
        //
        // 1 = Aportes sociales
        // 2 = Ahorro
        // 3 = TAC
        //
        // -------------------------------------------------

        if (!"2".equals(
                origen.tipoCaptacion()
        )) {
            throw new IllegalStateException(
                    "La cuenta origen debe corresponder a una cuenta de ahorro. "
                            + "No se permiten transferencias de salida desde "
                            + "Aportes Sociales ni TAC."
            );
        }


        // -------------------------------------------------
        // VALIDAR TIPO DE CAPTACIÓN DE DESTINO
        // -------------------------------------------------

        if (!List.of(
                "1",
                "2",
                "3"
        ).contains(destino.tipoCaptacion())) {

            throw new IllegalStateException(
                    "El tipo de captación de la cuenta destino no permite recibir transferencias."
            );
        }


        // -------------------------------------------------
        // CONSULTAR SALDO DISPONIBLE
        // -------------------------------------------------

        var detalleCuenta = capturaRepository.obtenerCuenta(
                origen.id().longValue()
        );

        if (detalleCuenta == null) {
            throw new IllegalStateException(
                    "No fue posible consultar el saldo disponible de la cuenta origen."
            );
        }

        BigDecimal saldoDisponible =
                detalleCuenta.getSaldoDisponible();


        // -------------------------------------------------
        // VALIDAR SALDO DISPONIBLE
        // -------------------------------------------------

        if (saldoDisponible == null) {
            throw new IllegalStateException(
                    "La cuenta origen no tiene un saldo disponible válido."
            );
        }

        if (saldoDisponible.compareTo(
                entrada.valor()
        ) < 0) {
            throw new IllegalStateException(
                    "La cuenta origen no tiene saldo disponible suficiente "
                            + "para realizar la transferencia."
            );
        }


        return new Analisis(
                origen,
                destino,
                saldoDisponible
        );
    }


    // =====================================================
    // GENERAR VISTA PREVIA
    // =====================================================
    //
    // Consulta las cuentas y valida la operación.
    //
    // Prepara los movimientos contables para mostrar
    // cómo se realizará la transferencia.
    //
    // No registra movimientos ni comprobantes.
    //
    // =====================================================

    @Transactional(readOnly = true)
    public TransferenciaCuentasPreviewDTO preview(
            TransferenciaCuentasRequestDTO entrada
    ) {

        // -------------------------------------------------
        // VALIDAR TRANSFERENCIA
        // -------------------------------------------------

        Analisis analisis = analizar(
                entrada,
                false
        );


        // -------------------------------------------------
        // PREPARAR CONTABILIZACIÓN
        // -------------------------------------------------

        var comprobantesPreparados = contabilidad.preparar(
                analisis.origen(),
                analisis.destino(),
                entrada.valor(),
                entrada.fechaProceso(),
                entrada.concepto().trim()
        );


        // -------------------------------------------------
        // CONSULTAR INFORMACIÓN DE LA CUENTA ORIGEN
        // -------------------------------------------------

        TransferenciaCuentasCuentaDTO cuentaOrigen =
                repository.obtenerDTO(
                        analisis.origen().id()
                );



        // -------------------------------------------------
        // ACTUALIZAR SALDO DISPONIBLE DE LA VISTA
        // -------------------------------------------------

        TransferenciaCuentasCuentaDTO origenDTO =
                new TransferenciaCuentasCuentaDTO(

                        cuentaOrigen.idCuentaAhorro(),

                        cuentaOrigen.idAgencia(),

                        cuentaOrigen.idDatosPersonal(),

                        // Agencia
                        cuentaOrigen.codigoAgencia(),

                        cuentaOrigen.nombreAgencia(),

                        // Titular
                        cuentaOrigen.documento(),

                        cuentaOrigen.nombreCompleto(),

                        // Cuenta y producto
                        cuentaOrigen.codigoCuenta(),

                        cuentaOrigen.nombreForma(),

                        cuentaOrigen.tipoCaptacion(),

                        // Estado
                        cuentaOrigen.estadoCuenta(),

                        cuentaOrigen.descripcionEstadoCuenta(),

                        cuentaOrigen.operativa(),

                        // Información financiera
                        cuentaOrigen.saldoActual(),

                        analisis.saldoDisponible(),

                        // Validación
                        cuentaOrigen.permiteDebito()
                );



        // -------------------------------------------------
        // CONSULTAR INFORMACIÓN DE LA CUENTA DESTINO
        // -------------------------------------------------

        TransferenciaCuentasCuentaDTO destinoDTO =
                repository.obtenerDTO(
                        analisis.destino().id()
                );


        // -------------------------------------------------
        // CALCULAR SALDOS PROYECTADOS
        // -------------------------------------------------

        BigDecimal saldoOrigenProyectado =
                analisis.origen()
                        .saldo()
                        .subtract(
                                entrada.valor()
                        );

        BigDecimal saldoDestinoProyectado =
                analisis.destino()
                        .saldo()
                        .add(
                                entrada.valor()
                        );


        // -------------------------------------------------
        // RETORNAR VISTA PREVIA
        // -------------------------------------------------

        return new TransferenciaCuentasPreviewDTO(

                origenDTO,

                destinoDTO,

                entrada.valor(),

                entrada.concepto().trim(),

                saldoOrigenProyectado,

                saldoDestinoProyectado,

                contabilidad.vista(
                        comprobantesPreparados
                )
        );
    }


    // =====================================================
    // APLICAR TRANSFERENCIA
    // =====================================================
    //
    // Ejecuta toda la transferencia en una transacción.
    //
    // Incluye:
    //
    // 1. Validación definitiva.
    // 2. Bloqueo de las cuentas.
    // 3. Preparación de la contabilización.
    // 4. Registro de comprobantes.
    // 5. Débito de la cuenta origen.
    // 6. Crédito de la cuenta destino.
    //
    // Si ocurre una excepción, la transacción se revierte.
    //
    // =====================================================

    @Transactional
    public TransferenciaCuentasResultadoDTO aplicar(
            TransferenciaCuentasRequestDTO entrada
    ) {

        // -------------------------------------------------
        // VALIDAR Y BLOQUEAR CUENTAS
        // -------------------------------------------------

        Analisis analisis = analizar(
                entrada,
                true
        );


        // -------------------------------------------------
        // IDENTIFICAR USUARIO AUTENTICADO
        // -------------------------------------------------

        Integer idUsuario = sesion.idUsuario();


        // -------------------------------------------------
        // PREPARAR COMPROBANTES
        // -------------------------------------------------

        var comprobantesPreparados = contabilidad.preparar(

                analisis.origen(),

                analisis.destino(),

                entrada.valor(),

                entrada.fechaProceso(),

                entrada.concepto().trim()
        );


        // -------------------------------------------------
        // REGISTRAR CONTABILIZACIÓN
        // -------------------------------------------------

        var comprobantesRegistrados = contabilidad.registrar(

                comprobantesPreparados,

                analisis.origen().id(),

                idUsuario
        );


        // -------------------------------------------------
        // IDENTIFICAR COMPROBANTES
        // -------------------------------------------------

        var comprobanteOrigen =
                comprobantesRegistrados.get(0);

        var comprobanteDestino =
                comprobantesRegistrados.get(
                        comprobantesRegistrados.size() - 1
                );


        // -------------------------------------------------
        // REGISTRAR DÉBITO DE LA CUENTA ORIGEN
        // -------------------------------------------------

        DepositosMovimientoDTO movimientoDebito =
                construirMovimiento(

                        analisis.origen().id(),

                        entrada.fechaProceso(),

                        "882",

                        comprobanteOrigen.tipoComprobante(),

                        comprobanteOrigen.numeroComprobante(),

                        entrada.valor(),

                        true
                );

        movimientos.registrarDebito(
                movimientoDebito,
                idUsuario
        );


        // -------------------------------------------------
        // REGISTRAR CRÉDITO DE LA CUENTA DESTINO
        // -------------------------------------------------

        DepositosMovimientoDTO movimientoCredito =
                construirMovimiento(

                        analisis.destino().id(),

                        entrada.fechaProceso(),

                        "442",

                        comprobanteDestino.tipoComprobante(),

                        comprobanteDestino.numeroComprobante(),

                        entrada.valor(),

                        false
                );

        movimientos.registrarCredito(
                movimientoCredito,
                idUsuario
        );


        // -------------------------------------------------
        // CALCULAR SALDOS RESULTANTES
        // -------------------------------------------------

        BigDecimal saldoOrigenFinal =
                analisis.origen()
                        .saldo()
                        .subtract(
                                entrada.valor()
                        );

        BigDecimal saldoDestinoFinal =
                analisis.destino()
                        .saldo()
                        .add(
                                entrada.valor()
                        );


        // -------------------------------------------------
        // RETORNAR RESULTADO
        // -------------------------------------------------

        return new TransferenciaCuentasResultadoDTO(

                analisis.origen().id(),

                analisis.destino().id(),

                entrada.valor(),

                saldoOrigenFinal,

                saldoDestinoFinal,

                comprobantesRegistrados
        );
    }


    // =====================================================
    // CONSTRUIR MOVIMIENTO DE DEPÓSITOS
    // =====================================================
    //
    // 882 = Traslado entre cuentas - salida
    // 442 = Traslado entre cuentas - entrada
    //
    // El movimiento se construye aquí.
    //
    // Su persistencia y actualización financiera se
    // delegan a DepositosMovimientoService.
    //
    // =====================================================

    private DepositosMovimientoDTO construirMovimiento(

            Integer idCuentaAhorro,

            LocalDate fechaMovimiento,

            String tipoMovimiento,

            String tipoComprobante,

            String numeroComprobante,

            BigDecimal valor,

            boolean esDebito
    ) {

        DepositosMovimientoDTO movimiento =
                new DepositosMovimientoDTO();


        // -------------------------------------------------
        // IDENTIFICACIÓN DE LA CUENTA
        // -------------------------------------------------

        movimiento.setIdCuentaAhorro(
                idCuentaAhorro
        );


        // -------------------------------------------------
        // FECHA Y TIPO DE MOVIMIENTO
        // -------------------------------------------------

        movimiento.setFechaMovimiento(
                fechaMovimiento
        );

        movimiento.setTipoMovimiento(
                tipoMovimiento
        );


        // -------------------------------------------------
        // COMPROBANTE CONTABLE
        // -------------------------------------------------

        movimiento.setTipoComprobante(
                tipoComprobante
        );

        movimiento.setNumeroComprobante(
                numeroComprobante
        );


        // -------------------------------------------------
        // IDENTIFICACIÓN DEL PROCESO
        // -------------------------------------------------

        movimiento.setModulo(
                "02"
        );

        movimiento.setModuloOrigen(
                "DEPOSITOS"
        );

        movimiento.setProcesoOrigen(
                "TRANSFERENCIA_CUENTAS"
        );

        movimiento.setIdOrigen(
                idCuentaAhorro.longValue()
        );


        // -------------------------------------------------
        // VALOR DEL MOVIMIENTO
        // -------------------------------------------------

        if (esDebito) {

            movimiento.setValorDebito(
                    valor
            );

        } else {

            movimiento.setValorCredito(
                    valor
            );
        }


        return movimiento;
    }

}
