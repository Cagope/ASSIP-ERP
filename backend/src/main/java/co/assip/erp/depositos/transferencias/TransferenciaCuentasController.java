
package co.assip.erp.depositos.transferencias;

import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasCuentaDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasPreviewDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasRequestDTO;
import co.assip.erp.depositos.transferencias.dto.TransferenciaCuentasResultadoDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


// =========================================================
// CONTROLADOR
// TRANSFERENCIA DE FONDOS ENTRE CUENTAS
// =========================================================
//
// Permite transferir fondos entre cuentas de depósitos,
// pertenecientes al mismo asociado o a asociados diferentes,
// dentro de la misma agencia o entre agencias.
//
// Operaciones:
//
// 1. Buscar cuentas de depósitos.
// 2. Consultar información de una cuenta.
// 3. Generar vista previa de la transferencia.
// 4. Aplicar la transferencia.
//
// Reglas generales:
//
// - La cuenta origen debe ser de ahorro.
// - La cuenta destino puede ser de ahorro,
//   aportes sociales o TAC.
// - Las cuentas deben estar operativas.
// - La cuenta origen debe tener saldo disponible.
// - La operación genera movimientos de depósitos.
// - La contabilización depende de las agencias.
//
// La validación financiera y la ejecución transaccional
// corresponden a TransferenciaCuentasService.
//
// =========================================================

@RestController
@RequiredArgsConstructor
@RequestMapping("/depositos/transferencias")
public class TransferenciaCuentasController {

    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final TransferenciaCuentasService service;

    private final TransferenciaCuentasRepository repository;


    // =====================================================
    // BUSCAR CUENTAS DE DEPÓSITOS
    // =====================================================
    //
    // Permite buscar cuentas utilizando:
    //
    // - Documento de identificación.
    // - Nombres.
    // - Primer apellido.
    // - Segundo apellido.
    //
    // La búsqueda puede incluir cuentas de diferentes
    // agencias y asociados.
    //
    // No se ejecuta la consulta cuando todos los
    // criterios están vacíos.
    //
    // =====================================================

    @GetMapping("/buscar-cuentas")
    public List<TransferenciaCuentasCuentaDTO> buscarCuentas(

            @RequestParam(defaultValue = "")
            String documento,

            @RequestParam(defaultValue = "")
            String nombres,

            @RequestParam(defaultValue = "")
            String primerApellido,

            @RequestParam(defaultValue = "")
            String segundoApellido
    ) {

        // -------------------------------------------------
        // NORMALIZAR CRITERIOS DE BÚSQUEDA
        // -------------------------------------------------

        String filtroDocumento = documento.trim();

        String filtroNombres = nombres.trim();

        String filtroPrimerApellido = primerApellido.trim();

        String filtroSegundoApellido = segundoApellido.trim();


        // -------------------------------------------------
        // VALIDAR QUE EXISTA AL MENOS UN CRITERIO
        // -------------------------------------------------

        if (
                filtroDocumento.isEmpty()
                        && filtroNombres.isEmpty()
                        && filtroPrimerApellido.isEmpty()
                        && filtroSegundoApellido.isEmpty()
        ) {

            return List.of();
        }


        // -------------------------------------------------
        // CONSULTAR CUENTAS
        // -------------------------------------------------

        return repository.buscar(

                filtroDocumento,

                filtroNombres,

                filtroPrimerApellido,

                filtroSegundoApellido
        );
    }


    // =====================================================
    // CONSULTAR INFORMACIÓN DE UNA CUENTA
    // =====================================================
    //
    // Obtiene la información necesaria para identificar
    // la cuenta seleccionada:
    //
    // - Agencia.
    // - Asociado titular.
    // - Número de cuenta.
    // - Forma de ahorro.
    // - Tipo de captación.
    // - Estado de la cuenta.
    // - Saldo actual.
    //
    // Esta consulta es informativa.
    // No registra movimientos financieros.
    //
    // =====================================================

    @GetMapping("/cuenta/{idCuentaAhorro}")
    public TransferenciaCuentasCuentaDTO consultarCuenta(

            @PathVariable
            Integer idCuentaAhorro
    ) {

        return repository.obtenerDTO(
                idCuentaAhorro
        );
    }


    // =====================================================
    // GENERAR VISTA PREVIA DE LA TRANSFERENCIA
    // =====================================================
    //
    // Valida la operación antes de ejecutarla.
    //
    // Incluye:
    //
    // 1. Validación de cuentas origen y destino.
    // 2. Validación del tipo de captación.
    // 3. Validación del estado de las cuentas.
    // 4. Verificación del saldo disponible.
    // 5. Cálculo de los saldos proyectados.
    // 6. Preparación de la contabilización.
    //
    // Contabilización:
    //
    // - Misma agencia: un comprobante NC.
    // - Agencias diferentes: dos comprobantes CA.
    //
    // No registra movimientos ni comprobantes.
    //
    // =====================================================

    @PostMapping("/preview")
    public TransferenciaCuentasPreviewDTO preview(

            @RequestBody
            TransferenciaCuentasRequestDTO entrada
    ) {

        return service.preview(
                entrada
        );
    }


    // =====================================================
    // APLICAR TRANSFERENCIA DE FONDOS
    // =====================================================
    //
    // Ejecuta definitivamente la transferencia.
    //
    // El servicio realiza:
    //
    // 1. Validación definitiva de las cuentas.
    // 2. Bloqueo de las cuentas involucradas.
    // 3. Verificación del saldo disponible.
    // 4. Preparación de los comprobantes contables.
    // 5. Registro de la contabilización.
    // 6. Débito de la cuenta origen (movimiento 882).
    // 7. Crédito de la cuenta destino (movimiento 442).
    // 8. Retorno de los comprobantes generados.
    //
    // Toda la operación se ejecuta dentro de una
    // única transacción administrada por el servicio.
    //
    // Si se presenta un error, la transacción se revierte.
    //
    // =====================================================

    @PostMapping("/aplicar")
    public TransferenciaCuentasResultadoDTO aplicar(

            @RequestBody
            TransferenciaCuentasRequestDTO entrada
    ) {

        return service.aplicar(
                entrada
        );
    }

}
