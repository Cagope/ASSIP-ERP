package co.assip.erp.cartera.originacion.financiero;

import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroDetalleDTO;
import co.assip.erp.cartera.originacion.financiero.dto.SolicitudFinancieroGuardarRequestDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SolicitudFinancieroService {

    private final SolicitudFinancieroRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public SolicitudFinancieroService(
            SolicitudFinancieroRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }


    // =========================================================
    // LISTAR POR SOLICITUD
    // =========================================================

    @Transactional(readOnly = true)
    public List<SolicitudFinancieroDTO> listarPorSolicitud(
            Integer idSolicitudCredito
    ) {

        validarIdSolicitudCredito(
                idSolicitudCredito
        );

        Integer idAgencia =
                repository.obtenerAgenciaSolicitud(
                                idSolicitudCredito
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe la solicitud de crédito con id "
                                                + idSolicitudCredito
                                                + "."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return repository.listarPorSolicitud(
                idSolicitudCredito
        );
    }


    // =========================================================
    // BUSCAR POR DEUDOR
    // =========================================================

    @Transactional(readOnly = true)
    public Optional<SolicitudFinancieroDetalleDTO> buscarPorDeudor(
            Integer idSolicitudDeudor
    ) {

        validarIdSolicitudDeudor(
                idSolicitudDeudor
        );

        Integer idAgencia =
                repository.obtenerAgenciaDeudor(
                                idSolicitudDeudor
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe el deudor de la solicitud con id "
                                                + idSolicitudDeudor
                                                + "."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        return repository.buscarPorDeudor(
                idSolicitudDeudor
        );
    }


    // =========================================================
    // GUARDAR INFORMACIÓN FINANCIERA
    // =========================================================

    public SolicitudFinancieroDetalleDTO guardar(
            SolicitudFinancieroGuardarRequestDTO request
    ) {

        validarRequest(
                request
        );

        /*
         * Bloquea la solicitud y además valida:
         *
         * - solicitud activa
         * - deudor activo
         * - resultado no final
         */
        Integer idAgencia =
                repository.bloquearSolicitudEditable(
                                request.getIdSolicitudDeudor()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "El deudor no existe, la solicitud está inactiva o ya tiene un resultado final."
                                )
                        );

        usuarioSesionService.validarAgencia(
                idAgencia
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * Un deudor mantiene un único registro financiero.
         *
         * Primera vez:
         * INSERT.
         *
         * Siguientes modificaciones:
         * UPDATE sobre el mismo registro.
         *
         * Se conserva:
         * - id_solicitud_deudor_financiero
         * - fk_seguridad_creacion
         * - fecha_creacion
         *
         * El tipo de persona NO se recibe del frontend.
         *
         * El Repository lo obtiene directamente desde:
         *
         * hoja_vida.datos_personales.tipo_persona
         *
         * El Repository también sincroniza en la misma transacción:
         *
         * - cartera.solicitudes_deudores_financieros
         * - hoja_vida.financieros
         * - hoja_vida.datos_personales
         */
        repository.guardar(
                request,
                idUsuario
        );

        return repository.buscarPorDeudor(
                        request.getIdSolicitudDeudor()
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "La información financiera fue registrada pero no pudo ser consultada posteriormente."
                        )
                );
    }


    // =========================================================
    // VALIDACIÓN ESTRUCTURAL
    // =========================================================

    private void validarRequest(
            SolicitudFinancieroGuardarRequestDTO request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La información financiera es obligatoria."
            );
        }

        validarIdSolicitudDeudor(
                request.getIdSolicitudDeudor()
        );


        // =====================================================
        // ACTIVIDAD ECONÓMICA
        // =====================================================

        validarTextoObligatorio(
                request.getCodigoOcupacion(),
                "Debe indicar la ocupación."
        );

        validarTextoObligatorio(
                request.getCodigoSectorEconomico(),
                "Debe indicar el sector económico."
        );

        validarTextoObligatorio(
                request.getCodigoActividadSes(),
                "Debe indicar la actividad económica SES."
        );

        validarTextoObligatorio(
                request.getCodigoActividadDian(),
                "Debe indicar la actividad económica DIAN."
        );


        // =====================================================
        // PERSONA NATURAL - INGRESOS
        // =====================================================

        validarNoNegativo(
                request.getValorSalario(),
                "valor salario"
        );

        validarNoNegativo(
                request.getValorPension(),
                "valor pensión"
        );

        validarNoNegativo(
                request.getIngresoIndependiente(),
                "ingreso independiente"
        );

        validarNoNegativo(
                request.getIngresosArriendo(),
                "ingresos por arriendo"
        );

        validarNoNegativo(
                request.getIngresosComisiones(),
                "ingresos por comisiones"
        );

        validarNoNegativo(
                request.getOtrosIngresos(),
                "otros ingresos"
        );


        // =====================================================
        // PERSONA NATURAL - EGRESOS
        // =====================================================

        validarNoNegativo(
                request.getEgresosFamiliares(),
                "egresos familiares"
        );

        validarNoNegativo(
                request.getEgresosArriendo(),
                "egresos por arriendo"
        );

        validarNoNegativo(
                request.getEgresosCredito(),
                "egresos por crédito"
        );

        validarNoNegativo(
                request.getOtrosEgresos(),
                "otros egresos"
        );


        // =====================================================
        // PERSONA JURÍDICA
        // =====================================================

        validarNoNegativo(
                request.getIngresosOperacionales(),
                "ingresos operacionales"
        );

        validarNoNegativo(
                request.getIngresosNoOperacionales(),
                "ingresos no operacionales"
        );

        validarNoNegativo(
                request.getCostos(),
                "costos"
        );

        validarNoNegativo(
                request.getGastosOperacionales(),
                "gastos operacionales"
        );

        validarNoNegativo(
                request.getGastosFinancieros(),
                "gastos financieros"
        );

        validarNoNegativo(
                request.getOtrosGastos(),
                "otros gastos"
        );


        // =====================================================
        // BALANCE
        // =====================================================

        validarNoNegativo(
                request.getActivoCorriente(),
                "activo corriente"
        );

        validarNoNegativo(
                request.getPasivoCorriente(),
                "pasivo corriente"
        );

        validarNoNegativo(
                request.getTotalActivos(),
                "total activos"
        );

        validarNoNegativo(
                request.getTotalPasivos(),
                "total pasivos"
        );

        validarNoNegativo(
                request.getDeudaRelacionFinanciera(),
                "deuda de relación financiera"
        );

        /*
         * Utilidad operacional y utilidad neta pueden ser
         * negativas porque representan pérdidas válidas.
         */


        // =====================================================
        // COHERENCIA DEL BALANCE
        // =====================================================

        validarMenorOIgual(
                request.getActivoCorriente(),
                request.getTotalActivos(),
                "El activo corriente no puede ser mayor que el total de activos."
        );

        validarMenorOIgual(
                request.getPasivoCorriente(),
                request.getTotalPasivos(),
                "El pasivo corriente no puede ser mayor que el total de pasivos."
        );


        // =====================================================
        // OTROS INGRESOS
        // =====================================================

        if (esPositivo(
                request.getOtrosIngresos()
        )
                && esVacio(
                request.getComentarioOtrosIngresos()
        )) {

            throw new IllegalArgumentException(
                    "Debe indicar un comentario para otros ingresos."
            );
        }


        // =====================================================
        // OTROS EGRESOS
        // =====================================================

        if (esPositivo(
                request.getOtrosEgresos()
        )
                && esVacio(
                request.getComentarioOtrosEgresos()
        )) {

            throw new IllegalArgumentException(
                    "Debe indicar un comentario para otros egresos."
            );
        }


        // =====================================================
        // RELACIÓN FINANCIERA
        // =====================================================

        if (esPositivo(
                request.getDeudaRelacionFinanciera()
        )
                && esVacio(
                request.getRelacionFinanciera()
        )) {

            throw new IllegalArgumentException(
                    "Debe indicar la relación financiera asociada a la deuda."
            );
        }


        // =====================================================
        // DECLARACIÓN DE RENTA
        // =====================================================

        validarDeclaracionRenta(
                request
        );
    }


    // =========================================================
    // DECLARACIÓN DE RENTA
    // =========================================================

    private void validarDeclaracionRenta(
            SolicitudFinancieroGuardarRequestDTO request
    ) {

        /*
         * Si no declara renta, los campos dependientes deben
         * quedar limpios para evitar información residual.
         */
        if (!Boolean.TRUE.equals(
                request.getDeclaraRenta()
        )) {

            request.setAnioDeclaracion(
                    null
            );

            request.setFechaPresentacionDeclaracion(
                    null
            );

            return;
        }


        if (request.getAnioDeclaracion() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar el año de la declaración de renta."
            );
        }


        int anioActual =
                Year.now().getValue();

        if (request.getAnioDeclaracion() < 1900
                || request.getAnioDeclaracion() > anioActual) {

            throw new IllegalArgumentException(
                    "El año de la declaración de renta no es válido."
            );
        }


        if (request.getFechaPresentacionDeclaracion() == null) {

            throw new IllegalArgumentException(
                    "Debe indicar la fecha de presentación de la declaración de renta."
            );
        }


        if (request.getFechaPresentacionDeclaracion()
                .isAfter(
                        LocalDate.now()
                )) {

            throw new IllegalArgumentException(
                    "La fecha de presentación de la declaración de renta no puede ser futura."
            );
        }


        if (request.getFechaPresentacionDeclaracion()
                .getYear()
                < request.getAnioDeclaracion()) {

            throw new IllegalArgumentException(
                    "La fecha de presentación no puede ser anterior al año de la declaración de renta."
            );
        }
    }


    // =========================================================
    // VALIDACIONES GENERALES
    // =========================================================

    private void validarIdSolicitudCredito(
            Integer idSolicitudCredito
    ) {

        if (idSolicitudCredito == null
                || idSolicitudCredito <= 0) {

            throw new IllegalArgumentException(
                    "El identificador de la solicitud no es válido."
            );
        }
    }


    private void validarIdSolicitudDeudor(
            Integer idSolicitudDeudor
    ) {

        if (idSolicitudDeudor == null
                || idSolicitudDeudor <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del deudor no es válido."
            );
        }
    }


    private void validarTextoObligatorio(
            String valor,
            String mensaje
    ) {

        if (esVacio(
                valor
        )) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }


    private void validarNoNegativo(
            BigDecimal valor,
            String campo
    ) {

        if (valor != null
                && valor.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "El campo "
                            + campo
                            + " no puede ser negativo."
            );
        }
    }


    private void validarMenorOIgual(
            BigDecimal valor,
            BigDecimal limite,
            String mensaje
    ) {

        if (valor == null
                || limite == null) {

            return;
        }

        if (valor.compareTo(
                limite
        ) > 0) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }


    private boolean esPositivo(
            BigDecimal valor
    ) {

        return valor != null
                && valor.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }


    private boolean esVacio(
            String valor
    ) {

        return valor == null
                || valor.isBlank();
    }
}