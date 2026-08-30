package co.assip.erp.depositos.cierre_mensual_depositos;

import co.assip.erp.depositos.cierre_mensual_depositos.dto.*;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CierreMensualDepositosService {

    private final CierreMensualDepositosRepository repository;
    private final UsuarioSesionService usuarioSesionService;


    // =========================================================
    // PREVIEW
    //
    // - no persiste información
    // - centralizado para toda la entidad
    // - incluye todas las agencias
    // =========================================================

    @Transactional(readOnly = true)
    public CierreMensualDepositosPreviewDTO preview(
            CierreMensualDepositosRequestDTO request
    ) {

        validarRequest(
                request
        );

        validarUltimoDiaMes(
                request.getFechaCierre()
        );

        return construirPreview(
                request.getFechaCierre()
        );
    }


    // =========================================================
    // GENERAR FOTOGRAFÍA MENSUAL
    //
    // Una sola fotografía para toda la entidad.
    //
    // P = En proceso / fotografía abierta
    //
    // - incluye todas las agencias
    // - crea cabecera
    // - guarda detalle
    // - guarda resumen por agencia + forma
    // - queda regenerable
    // - NO queda cerrada en firme
    // =========================================================

    public CierreMensualDepositosApplyResponseDTO generar(
            CierreMensualDepositosRequestDTO request
    ) {

        validarRequest(
                request
        );

        validarUltimoDiaMes(
                request.getFechaCierre()
        );


        Integer idUsuario =
                usuarioSesionService.idUsuario();


        // =====================================================
        // 1. VALIDAR QUE NO EXISTA CIERRE PARA LA FECHA
        // =====================================================

        if (repository.existeCierre(
                request.getFechaCierre()
        )) {

            throw new IllegalStateException(
                    "Ya existe un cierre mensual de depósitos "
                            + "para la fecha "
                            + request.getFechaCierre()
                            + "."
            );
        }


        // =====================================================
        // 2. GENERAR PREVIEW CENTRALIZADO
        // =====================================================

        CierreMensualDepositosPreviewDTO preview =
                construirPreview(
                        request.getFechaCierre()
                );


        validarPreviewConInformacion(
                preview
        );


        // =====================================================
        // 3. CREAR CABECERA EN ESTADO P
        // =====================================================

        Long idCierre =
                repository.crearCierre(
                        request,
                        preview.getResumen(),
                        idUsuario
                );


        if (idCierre == null
                || idCierre <= 0) {

            throw new IllegalStateException(
                    "No fue posible crear la cabecera "
                            + "del cierre mensual de depósitos."
            );
        }


        // =====================================================
        // 4. GUARDAR DETALLE DE TODAS LAS AGENCIAS
        // =====================================================

        repository.guardarDetalle(
                idCierre,
                preview.getDetalle()
        );


        // =====================================================
        // 5. GUARDAR RESUMEN POR AGENCIA + FORMA
        // =====================================================

        repository.guardarResumenFormas(
                idCierre,
                preview.getResumenFormas()
        );


        // =====================================================
        // 6. VALIDAR PERSISTENCIA
        // =====================================================

        validarPersistencia(
                idCierre,
                preview
        );


        // =====================================================
        // 7. RESULTADO
        // =====================================================

        return CierreMensualDepositosApplyResponseDTO
                .builder()

                .idCierreMensual(
                        idCierre
                )

                .mensaje(
                        "Fotografía mensual de depósitos "
                                + "generada correctamente para toda la entidad "
                                + "en estado En proceso."
                )

                .build();
    }


    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // - conserva id_cierre_mensual
    // - solamente estado P
    // - incluye nuevamente todas las agencias
    // - elimina detalle anterior
    // - elimina resumen anterior
    // - vuelve a calcular
    // - vuelve a persistir
    // =========================================================

    public CierreMensualDepositosApplyResponseDTO regenerar(
            Long idCierre
    ) {

        validarIdCierre(
                idCierre
        );


        Integer idUsuario =
                usuarioSesionService.idUsuario();


        // =====================================================
        // 1. RECUPERAR CIERRE
        // =====================================================

        CierreMensualDepositosPreviewDTO cierre =
                repository.obtenerPorId(
                        idCierre
                );


        if (cierre == null) {

            throw new IllegalArgumentException(
                    "No existe el cierre mensual de depósitos: "
                            + idCierre
            );
        }


        // =====================================================
        // 2. VALIDAR ESTADO
        // =====================================================

        validarEstadoAbierto(
                cierre.getEstado()
        );


        // =====================================================
        // 3. GENERAR NUEVA INFORMACIÓN
        //
        // Primero calculamos.
        //
        // Si el cálculo falla, todavía no hemos eliminado
        // la fotografía anterior.
        // =====================================================

        CierreMensualDepositosPreviewDTO nuevoPreview =
                construirPreview(
                        cierre.getFechaCierre()
                );


        validarPreviewConInformacion(
                nuevoPreview
        );


        // =====================================================
        // 4. ELIMINAR RESUMEN ANTERIOR
        // =====================================================

        repository.eliminarResumen(
                idCierre
        );


        // =====================================================
        // 5. ELIMINAR DETALLE ANTERIOR
        // =====================================================

        repository.eliminarDetalle(
                idCierre
        );


        // =====================================================
        // 6. VALIDAR LIMPIEZA
        // =====================================================

        int detalleRestante =
                repository.contarDetalle(
                        idCierre
                );


        int resumenRestante =
                repository.contarResumen(
                        idCierre
                );


        if (detalleRestante != 0
                || resumenRestante != 0) {

            throw new IllegalStateException(
                    "No fue posible limpiar completamente "
                            + "la fotografía anterior del cierre "
                            + idCierre
                            + ". Detalles restantes: "
                            + detalleRestante
                            + ". Resúmenes restantes: "
                            + resumenRestante
                            + "."
            );
        }


        // =====================================================
        // 7. GUARDAR NUEVO DETALLE
        // =====================================================

        repository.guardarDetalle(
                idCierre,
                nuevoPreview.getDetalle()
        );


        // =====================================================
        // 8. GUARDAR NUEVO RESUMEN
        // =====================================================

        repository.guardarResumenFormas(
                idCierre,
                nuevoPreview.getResumenFormas()
        );


        // =====================================================
        // 9. ACTUALIZAR CABECERA
        // =====================================================

        int cabeceraActualizada =
                repository.actualizarPrecierre(
                        idCierre,
                        nuevoPreview.getResumen(),
                        idUsuario
                );


        if (cabeceraActualizada != 1) {

            throw new IllegalStateException(
                    "No fue posible actualizar la cabecera "
                            + "del cierre mensual de depósitos "
                            + idCierre
                            + "."
            );
        }


        // =====================================================
        // 10. VALIDAR PERSISTENCIA
        // =====================================================

        validarPersistencia(
                idCierre,
                nuevoPreview
        );


        // =====================================================
        // 11. RESULTADO
        // =====================================================

        return CierreMensualDepositosApplyResponseDTO
                .builder()

                .idCierreMensual(
                        idCierre
                )

                .mensaje(
                        "Fotografía mensual de depósitos "
                                + "regenerada correctamente para toda la entidad."
                )

                .build();
    }


    // =========================================================
    // CERRAR FOTOGRAFÍA EN FIRME
    //
    // P -> C
    //
    // - no recalcula
    // - valida detalle
    // - valida resumen
    // - valida cantidad contra cabecera
    // - después queda inmutable
    // =========================================================

    public CierreMensualDepositosApplyResponseDTO cerrarFotografia(
            Long idCierre
    ) {

        validarIdCierre(
                idCierre
        );


        Integer idUsuario =
                usuarioSesionService.idUsuario();


        // =====================================================
        // 1. RECUPERAR CIERRE
        // =====================================================

        CierreMensualDepositosPreviewDTO cierre =
                repository.obtenerPorId(
                        idCierre
                );


        if (cierre == null) {

            throw new IllegalArgumentException(
                    "No existe el cierre mensual de depósitos: "
                            + idCierre
            );
        }


        // =====================================================
        // 2. VALIDAR ESTADO P
        // =====================================================

        validarEstadoAbierto(
                cierre.getEstado()
        );


        // =====================================================
        // 3. VALIDAR DETALLE
        // =====================================================

        int cantidadDetalle =
                repository.contarDetalle(
                        idCierre
                );


        if (cantidadDetalle <= 0) {

            throw new IllegalStateException(
                    "El cierre mensual de depósitos "
                            + idCierre
                            + " no tiene fotografía de cuentas."
            );
        }


        // =====================================================
        // 4. VALIDAR RESUMEN
        // =====================================================

        int cantidadResumen =
                repository.contarResumen(
                        idCierre
                );


        if (cantidadResumen <= 0) {

            throw new IllegalStateException(
                    "El cierre mensual de depósitos "
                            + idCierre
                            + " no tiene resumen por agencia "
                            + "y forma de ahorro."
            );
        }


        // =====================================================
        // 5. VALIDAR CANTIDAD DETALLE VS CABECERA
        // =====================================================

        if (cierre.getTotalCuentas() == null
                || cierre.getTotalCuentas() != cantidadDetalle) {

            throw new IllegalStateException(
                    "No es posible cerrar la fotografía de depósitos. "
                            + "La cabecera registra "
                            + cierre.getTotalCuentas()
                            + " cuentas y el detalle contiene "
                            + cantidadDetalle
                            + "."
            );
        }


        // =====================================================
        // 6. CERRAR EN FIRME
        // =====================================================

        int actualizados =
                repository.finalizar(
                        idCierre,
                        idUsuario
                );


        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible cerrar en firme "
                            + "la fotografía mensual de depósitos "
                            + idCierre
                            + "."
            );
        }


        // =====================================================
        // 7. RESULTADO
        // =====================================================

        return CierreMensualDepositosApplyResponseDTO
                .builder()

                .idCierreMensual(
                        idCierre
                )

                .mensaje(
                        "Fotografía mensual de depósitos "
                                + "cerrada en firme correctamente."
                )

                .build();
    }


    // =========================================================
    // LISTAR
    // =========================================================

    @Transactional(readOnly = true)
    public List<CierreMensualDepositosPreviewDTO> listar() {

        return repository.listar();
    }


    // =========================================================
    // OBTENER POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public CierreMensualDepositosPreviewDTO obtenerPorId(
            Long idCierre
    ) {

        validarIdCierre(
                idCierre
        );


        CierreMensualDepositosPreviewDTO cierre =
                repository.obtenerPorId(
                        idCierre
                );


        if (cierre == null) {

            throw new IllegalArgumentException(
                    "No existe el cierre mensual de depósitos: "
                            + idCierre
            );
        }


        return cierre;
    }


    // =========================================================
    // ELIMINAR PRECierre
    //
    // Solo estado P.
    // =========================================================

    public void eliminar(
            Long idCierre
    ) {

        validarIdCierre(
                idCierre
        );


        // =====================================================
        // 1. VALIDAR EXISTENCIA Y ESTADO
        // =====================================================

        String estado =
                repository.buscarEstado(
                                idCierre
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el cierre mensual "
                                                + "de depósitos: "
                                                + idCierre
                                )
                        );


        validarEstadoAbierto(
                estado
        );


        // =====================================================
        // 2. ELIMINAR
        // =====================================================

        int eliminados =
                repository.eliminar(
                        idCierre
                );


        if (eliminados != 1) {

            throw new IllegalStateException(
                    "No fue posible eliminar "
                            + "el precierre mensual de depósitos "
                            + idCierre
                            + "."
            );
        }
    }


    // =========================================================
// CONSTRUIR PREVIEW CENTRALIZADO
//
// - reutilizado por preview()
// - reutilizado por generar()
// - reutilizado por regenerar()
// - incluye todas las agencias
// - no persiste información
// =========================================================

    private CierreMensualDepositosPreviewDTO construirPreview(
            LocalDate fechaCierre
    ) {

        // =====================================================
        // 1. GENERAR DETALLE DE TODAS LAS AGENCIAS
        // =====================================================

        List<CierreMensualDepositosDetalleDTO> detalle =
                repository.generarDetalle(
                        fechaCierre
                );


        // =====================================================
        // 2. GENERAR RESUMEN POR AGENCIA
        // =====================================================

        List<CierreMensualDepositosResumenAgenciaDTO>
                resumenAgencias =
                repository.generarResumenAgencias(
                        detalle
                );


        // =====================================================
        // 3. GENERAR RESUMEN POR AGENCIA + FORMA
        // =====================================================

        List<CierreMensualDepositosResumenFormaDTO>
                resumenFormas =
                repository.generarResumenFormas(
                        detalle
                );


        // =====================================================
        // 4. GENERAR RESUMEN GENERAL DE LA ENTIDAD
        // =====================================================

        CierreMensualDepositosResumenDTO resumen =
                repository.generarResumenGeneral(
                        detalle,
                        resumenFormas
                );


        // =====================================================
        // 5. CONSTRUIR RESULTADO
        // =====================================================

        return CierreMensualDepositosPreviewDTO
                .builder()

                .fechaCierre(
                        fechaCierre
                )

                .anio(
                        fechaCierre.getYear()
                )

                .mes(
                        fechaCierre.getMonthValue()
                )

                .resumen(
                        resumen
                )

                .resumenAgencias(
                        resumenAgencias
                )

                .resumenFormas(
                        resumenFormas
                )

                .detalle(
                        detalle
                )

                .build();
    }


    // =========================================================
    // VALIDAR PREVIEW CON INFORMACIÓN
    // =========================================================

    private void validarPreviewConInformacion(
            CierreMensualDepositosPreviewDTO preview
    ) {

        if (preview == null
                || preview.getDetalle() == null
                || preview.getDetalle().isEmpty()) {

            throw new IllegalStateException(
                    "No hay cuentas con saldo para generar "
                            + "el cierre mensual de depósitos."
            );
        }


        if (preview.getResumen() == null) {

            throw new IllegalStateException(
                    "No fue posible generar el resumen "
                            + "del cierre mensual de depósitos."
            );
        }

        if (preview.getResumenAgencias() == null
                || preview.getResumenAgencias().isEmpty()) {

            throw new IllegalStateException(
                    "No fue posible generar el resumen "
                            + "por agencia del cierre mensual de depósitos."
            );
        }


        if (preview.getResumenFormas() == null
                || preview.getResumenFormas().isEmpty()) {

            throw new IllegalStateException(
                    "No fue posible generar el resumen "
                            + "por agencia y forma de ahorro."
            );
        }
    }


    // =========================================================
    // VALIDAR PERSISTENCIA
    // =========================================================

    private void validarPersistencia(
            Long idCierre,
            CierreMensualDepositosPreviewDTO preview
    ) {

        int cantidadDetalle =
                repository.contarDetalle(
                        idCierre
                );


        int cantidadResumen =
                repository.contarResumen(
                        idCierre
                );


        int detalleEsperado =
                preview.getDetalle()
                        .size();


        int resumenEsperado =
                preview.getResumenFormas()
                        .size();


        if (cantidadDetalle != detalleEsperado) {

            throw new IllegalStateException(
                    "Inconsistencia al guardar la fotografía "
                            + "mensual de depósitos. "
                            + "Cuentas esperadas: "
                            + detalleEsperado
                            + ". Cuentas guardadas: "
                            + cantidadDetalle
                            + "."
            );
        }


        if (cantidadResumen != resumenEsperado) {

            throw new IllegalStateException(
                    "Inconsistencia al guardar el resumen "
                            + "del cierre mensual de depósitos. "
                            + "Registros esperados de agencia/forma: "
                            + resumenEsperado
                            + ". Registros guardados: "
                            + cantidadResumen
                            + "."
            );
        }
    }


    // =========================================================
    // VALIDAR REQUEST
    //
    // El cierre ya NO recibe agencia.
    // =========================================================

    private void validarRequest(
            CierreMensualDepositosRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Solicitud inválida."
            );
        }


        if (request.getFechaCierre() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar la fecha de cierre."
            );
        }
    }


    // =========================================================
    // VALIDAR ID CIERRE
    // =========================================================

    private void validarIdCierre(
            Long idCierre
    ) {

        if (idCierre == null
                || idCierre <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre mensual de depósitos "
                            + "es obligatorio."
            );
        }
    }


    // =========================================================
    // VALIDAR ESTADO ABIERTO
    // =========================================================

    private void validarEstadoAbierto(
            String estado
    ) {

        if (estado == null
                || !"P".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "La operación solamente está permitida "
                            + "cuando la fotografía mensual "
                            + "de depósitos se encuentra "
                            + "en estado En proceso."
            );
        }
    }


    // =========================================================
    // VALIDAR ÚLTIMO DÍA DEL MES
    // =========================================================

    private void validarUltimoDiaMes(
            LocalDate fecha
    ) {

        YearMonth ym =
                YearMonth.of(
                        fecha.getYear(),
                        fecha.getMonth()
                );


        if (!fecha.equals(
                ym.atEndOfMonth()
        )) {

            throw new IllegalArgumentException(
                    "La fecha de cierre debe ser "
                            + "el último día del mes."
            );
        }
    }
}