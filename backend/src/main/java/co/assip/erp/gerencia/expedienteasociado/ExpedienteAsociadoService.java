package co.assip.erp.gerencia.expedienteasociado;

import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteAfiliacionDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteAlertaDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteAsociadoDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienInmuebleDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienInversionDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienMaquinariaDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteBienVehiculoDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteCdatDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteContactoDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteCreditoDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteCuentaAhorroDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteEstadoCargaDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteGarantiaDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteIndicadoresDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteInformacionFinancieraDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteResumenGeneralDTO;
import co.assip.erp.gerencia.expedienteasociado.dto.ExpedienteSarlaftDTO;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedienteBienesRepository;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedienteAhorrosRepository;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedienteCdatRepository;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedienteCarteraRepository;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedienteParticipacionRepository;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedientePersonaRepository;
import co.assip.erp.seguridad.service.UsuarioSesionService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import co.assip.erp.gerencia.expedienteasociado.repository.ExpedienteAfiliacionRepository;

@Service
@Transactional(readOnly = true)
public class ExpedienteAsociadoService {

    private static final BigDecimal CERO =
            BigDecimal.ZERO;

    private static final BigDecimal CIEN =
            new BigDecimal("100");

    private final ExpedientePersonaRepository personaRepository;
    private final ExpedienteAfiliacionRepository afiliacionRepository;
    private final ExpedienteAhorrosRepository ahorrosRepository;
    private final ExpedienteCdatRepository cdatRepository;
    private final ExpedienteCarteraRepository carteraRepository;
    private final ExpedienteBienesRepository bienesRepository;
    private final ExpedienteParticipacionRepository participacionRepository;
    private final UsuarioSesionService usuarioSesionService;

    public ExpedienteAsociadoService(
            ExpedientePersonaRepository personaRepository,
            ExpedienteAfiliacionRepository afiliacionRepository,
            ExpedienteAhorrosRepository ahorrosRepository,
            ExpedienteCdatRepository cdatRepository,
            ExpedienteCarteraRepository carteraRepository,
            ExpedienteBienesRepository bienesRepository,
            ExpedienteParticipacionRepository participacionRepository,
            UsuarioSesionService usuarioSesionService
    ) {

        this.personaRepository = personaRepository;
        this.afiliacionRepository = afiliacionRepository;
        this.ahorrosRepository = ahorrosRepository;
        this.cdatRepository = cdatRepository;
        this.carteraRepository = carteraRepository;
        this.bienesRepository = bienesRepository;
        this.participacionRepository = participacionRepository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // =========================================================
    // Consulta integral
    // =========================================================

    public ExpedienteAsociadoDTO consultar(
            Long idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        ExpedienteAsociadoDTO expediente =
                inicializarExpediente(idDatosPersonal);

        if (!personaRepository.existePersona(idDatosPersonal)) {
            expediente.marcarNoEncontrado(idDatosPersonal);
            return expediente;
        }

        cargarResumenGeneral(expediente, idDatosPersonal);
        cargarAfiliacion(expediente, idDatosPersonal);
        cargarContacto(expediente, idDatosPersonal);
        cargarInformacionFinanciera(expediente, idDatosPersonal);
        cargarSarlaft(expediente, idDatosPersonal);
        cargarParticipacionInstitucional(expediente, idDatosPersonal);

        cargarCuentasAhorro(expediente, idDatosPersonal);
        cargarCdats(expediente, idDatosPersonal);
        cargarCreditos(expediente, idDatosPersonal);

        cargarBienesInmuebles(expediente, idDatosPersonal);
        cargarBienesVehiculos(expediente, idDatosPersonal);
        cargarBienesMaquinaria(expediente, idDatosPersonal);
        cargarBienesInversiones(expediente, idDatosPersonal);

        cargarGarantias(expediente, idDatosPersonal);

        expediente.setIndicadores(
                calcularIndicadores(expediente)
        );

        expediente.getEstadoCarga()
                .setIndicadoresCalculados(Boolean.TRUE);

        expediente.setAlertas(
                generarAlertas(expediente)
        );

        expediente.getEstadoCarga()
                .setAlertasCalculadas(Boolean.TRUE);

        expediente.actualizarConteos();

        sincronizarIndicadoresConAlertas(expediente);

        expediente.getEstadoCarga()
                .actualizarEstadoGeneral();

        expediente.marcarEncontrado();

        return expediente;
    }

    // =========================================================
    // Inicialización
    // =========================================================

    private ExpedienteAsociadoDTO inicializarExpediente(
            Long idDatosPersonal
    ) {
        ExpedienteAsociadoDTO expediente =
                new ExpedienteAsociadoDTO();

        expediente.setIdDatosPersonal(idDatosPersonal);
        expediente.setFechaHoraConsulta(LocalDateTime.now());

        expediente.setIdUsuarioConsulta(
                usuarioSesionService.idUsuario()
        );

        expediente.setIdAgenciaConsulta(
                usuarioSesionService.agenciaPrincipal()
        );

        return expediente;
    }

    // =========================================================
    // Carga: resumen general
    // =========================================================

    private void cargarResumenGeneral(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        Optional<ExpedienteResumenGeneralDTO> resultado =
                personaRepository.obtenerResumenGeneral(idDatosPersonal);

        if (resultado.isEmpty()) {

            expediente.getEstadoCarga().agregarError(
                    "No fue posible obtener el resumen general."
            );

            return;
        }

        ExpedienteResumenGeneralDTO resumen =
                resultado.get();

        expediente.setResumenGeneral(resumen);

        expediente.setIdDatosPersonal(
                resumen.getIdDatosPersonal()
        );

        expediente.setTipoDocumento(
                resumen.getTipoDocumento()
        );

        expediente.setNombreTipoDocumento(
                resumen.getNombreTipoDocumento()
        );

        expediente.setDocumento(
                resumen.getDocumento()
        );

        expediente.setNombreCompleto(
                resumen.getNombreCompleto()
        );

        expediente.getEstadoCarga()
                .setResumenGeneralCargado(Boolean.TRUE);
    }

    // =========================================================
    // Carga: afiliación
    // =========================================================

    private void cargarAfiliacion(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {

        ExpedienteAfiliacionDTO afiliacion =
                afiliacionRepository
                        .obtenerAfiliacion(idDatosPersonal)
                        .orElse(null);

        expediente.setAfiliacion(afiliacion);

        expediente.getEstadoCarga()
                .setAfiliacionCargada(Boolean.TRUE);

        if (afiliacion == null) {

            expediente.getEstadoCarga()
                    .agregarAdvertencia(
                            "El asociado no tiene información de afiliación."
                    );
        }

    }

    // =========================================================
    // Carga: contacto
    // =========================================================

    private void cargarContacto(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        ExpedienteContactoDTO contacto =
                personaRepository.obtenerContacto(idDatosPersonal)
                        .orElse(null);

        if (contacto != null) {
            contacto.evaluarContactoCompleto();
            contacto.calcularPorcentajeCompletitud();
        }

        expediente.setContacto(contacto);

        expediente.getEstadoCarga()
                .setContactoCargado(Boolean.TRUE);

        if (contacto == null) {
            expediente.getEstadoCarga().agregarAdvertencia(
                    "El asociado no tiene información de contacto."
            );
        }
    }

    // =========================================================
    // Carga: financiero
    // =========================================================

    private void cargarInformacionFinanciera(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        ExpedienteInformacionFinancieraDTO financiero =
                personaRepository.obtenerInformacionFinanciera(
                        idDatosPersonal
                ).orElse(null);

        expediente.setInformacionFinanciera(financiero);

        expediente.getEstadoCarga()
                .setInformacionFinancieraCargada(Boolean.TRUE);

        if (financiero == null) {
            expediente.getEstadoCarga().agregarAdvertencia(
                    "El asociado no tiene información financiera."
            );
        }
    }

    // =========================================================
    // Carga: SARLAFT
    // =========================================================

    private void cargarSarlaft(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        ExpedienteSarlaftDTO sarlaft =
                personaRepository.obtenerSarlaft(idDatosPersonal)
                        .orElse(null);

        expediente.setSarlaft(sarlaft);

        expediente.getEstadoCarga()
                .setSarlaftCargado(Boolean.TRUE);

        if (sarlaft == null) {
            expediente.getEstadoCarga().agregarAdvertencia(
                    "El asociado no tiene información SARLAFT."
            );
        }
    }


    // =========================================================
    // Carga: participación institucional
    // =========================================================

    private void cargarParticipacionInstitucional(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {

        var participaciones =
                participacionRepository
                        .listarParticipacionInstitucional(
                                idDatosPersonal
                        );

        expediente.setParticipacionInstitucional(
                listaSegura(participaciones)
        );

        expediente.getEstadoCarga()
                .setParticipacionInstitucionalCargada(
                        Boolean.TRUE
                );

        if (expediente.getParticipacionInstitucional().isEmpty()) {

            expediente.getEstadoCarga()
                    .agregarAdvertencia(
                            "El asociado no registra participación institucional."
                    );
        }
    }

    // =========================================================
    // Carga: depósitos
    // =========================================================

    private void cargarCuentasAhorro(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        List<ExpedienteCuentaAhorroDTO> cuentas =
                ahorrosRepository.listarCuentasAhorro(idDatosPersonal);

        expediente.setCuentasAhorro(
                listaSegura(cuentas)
        );

        expediente.getEstadoCarga()
                .setCuentasAhorroCargadas(Boolean.TRUE);
    }

    private void cargarCdats(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        List<ExpedienteCdatDTO> cdats =
                cdatRepository.listarCdats(idDatosPersonal);

        expediente.setCdats(
                listaSegura(cdats)
        );

        expediente.getEstadoCarga()
                .setCdatsCargados(Boolean.TRUE);
    }

    // =========================================================
    // Carga: cartera
    // =========================================================

    private void cargarCreditos(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        List<ExpedienteCreditoDTO> creditos =
                carteraRepository.listarCreditos(idDatosPersonal);

        expediente.setCreditos(
                listaSegura(creditos)
        );

        expediente.getEstadoCarga()
                .setCreditosCargados(Boolean.TRUE);
    }

    // =========================================================
    // Carga: bienes
    // =========================================================

    private void cargarBienesInmuebles(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        expediente.setBienesInmuebles(
                listaSegura(
                        bienesRepository.listarBienesInmuebles(
                                idDatosPersonal
                        )
                )
        );

        expediente.getEstadoCarga()
                .setBienesInmueblesCargados(Boolean.TRUE);
    }

    private void cargarBienesVehiculos(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        expediente.setBienesVehiculos(
                listaSegura(
                        bienesRepository.listarBienesVehiculos(
                                idDatosPersonal
                        )
                )
        );

        expediente.getEstadoCarga()
                .setBienesVehiculosCargados(Boolean.TRUE);
    }

    private void cargarBienesMaquinaria(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        expediente.setBienesMaquinaria(
                listaSegura(
                        bienesRepository.listarBienesMaquinaria(
                                idDatosPersonal
                        )
                )
        );

        expediente.getEstadoCarga()
                .setBienesMaquinariaCargados(Boolean.TRUE);
    }

    private void cargarBienesInversiones(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        expediente.setBienesInversiones(
                listaSegura(
                        bienesRepository.listarBienesInversiones(
                                idDatosPersonal
                        )
                )
        );

        expediente.getEstadoCarga()
                .setBienesInversionesCargados(Boolean.TRUE);
    }

    // =========================================================
    // Carga: garantías
    // =========================================================

    private void cargarGarantias(
            ExpedienteAsociadoDTO expediente,
            Long idDatosPersonal
    ) {
        List<ExpedienteGarantiaDTO> garantias =
                carteraRepository.listarGarantias(idDatosPersonal);

        expediente.setGarantias(
                listaSegura(garantias)
        );

        expediente.getEstadoCarga()
                .setGarantiasCargadas(Boolean.TRUE);

        if (garantias == null || garantias.isEmpty()) {
            expediente.getEstadoCarga().agregarAdvertencia(
                    "Las garantías detalladas todavía no están "
                            + "integradas al expediente."
            );
        }
    }

    // =========================================================
    // Indicadores
    // =========================================================

    private ExpedienteIndicadoresDTO calcularIndicadores(
            ExpedienteAsociadoDTO expediente
    ) {
        ExpedienteIndicadoresDTO indicadores =
                new ExpedienteIndicadoresDTO();

        indicadores.setIdDatosPersonal(
                expediente.getIdDatosPersonal()
        );

        indicadores.setDocumento(
                expediente.getDocumento()
        );

        indicadores.setNombreCompleto(
                expediente.getNombreCompleto()
        );

        indicadores.setCantidadCuentasAhorro(
                expediente.getCuentasAhorro().size()
        );

        indicadores.setCantidadCdats(
                expediente.getCdats().size()
        );

        indicadores.setCantidadCreditos(
                expediente.getCreditos().size()
        );

        indicadores.setCantidadBienes(
                expediente.getBienesInmuebles().size()
                        + expediente.getBienesVehiculos().size()
                        + expediente.getBienesMaquinaria().size()
                        + expediente.getBienesInversiones().size()
        );

        indicadores.setCantidadGarantias(
                expediente.getGarantias().size()
        );

        BigDecimal totalAportes =
                expediente.getCuentasAhorro()
                        .stream()
                        .filter(this::esCuentaDeAportes)
                        .map(ExpedienteCuentaAhorroDTO::getSaldoTotal)
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal totalAhorros =
                expediente.getCuentasAhorro()
                        .stream()
                        .filter(cuenta -> !esCuentaDeAportes(cuenta))
                        .map(ExpedienteCuentaAhorroDTO::getSaldoTotal)
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal totalCdats =
                expediente.getCdats()
                        .stream()
                        .map(ExpedienteCdatDTO::getSaldoTotal)
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal totalBienes =
                totalBienes(expediente);

        BigDecimal saldoCapital =
                expediente.getCreditos()
                        .stream()
                        .map(ExpedienteCreditoDTO::getSaldoActual)
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal saldoTotalCredito =
                expediente.getCreditos()
                        .stream()
                        .map(ExpedienteCreditoDTO::getSaldoTotal)
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal provisionTotal =
                expediente.getCreditos()
                        .stream()
                        .map(ExpedienteCreditoDTO::getProvisionTotal)
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal valorGarantias =
                expediente.getGarantias()
                        .stream()
                        .map(
                                ExpedienteGarantiaDTO
                                        ::getValorCoberturaReconocido
                        )
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        int creditosEnMora =
                (int) expediente.getCreditos()
                        .stream()
                        .filter(
                                credito ->
                                        Boolean.TRUE.equals(
                                                credito.getEnMora()
                                        )
                        )
                        .count();

        int diasMayorMora =
                expediente.getCreditos()
                        .stream()
                        .map(ExpedienteCreditoDTO::getDiasMora)
                        .filter(valor -> valor != null)
                        .max(Integer::compareTo)
                        .orElse(0);

        String mayorEdadRiesgo =
                expediente.getCreditos()
                        .stream()
                        .map(ExpedienteCreditoDTO::getEdadRiesgoFinal)
                        .filter(this::tieneTexto)
                        .max(
                                Comparator.comparingInt(
                                        this::ordenEdadRiesgo
                                )
                        )
                        .orElse(null);

        BigDecimal totalPatrimonio =
                totalAportes
                        .add(totalAhorros)
                        .add(totalCdats)
                        .add(totalBienes);

        BigDecimal patrimonioNeto =
                totalPatrimonio.subtract(
                        saldoTotalCredito
                );

        indicadores.setTotalAportes(totalAportes);
        indicadores.setTotalAhorros(totalAhorros);
        indicadores.setTotalCdats(totalCdats);
        indicadores.setTotalBienes(totalBienes);
        indicadores.setTotalPatrimonio(totalPatrimonio);

        indicadores.setTotalObligaciones(
                saldoTotalCredito
        );

        indicadores.setPatrimonioNeto(
                patrimonioNeto
        );

        indicadores.setSaldoCapital(
                saldoCapital
        );

        indicadores.setSaldoTotalCredito(
                saldoTotalCredito
        );

        indicadores.setCreditosEnMora(
                creditosEnMora
        );

        indicadores.setDiasMayorMora(
                diasMayorMora
        );

        indicadores.setMayorEdadRiesgo(
                mayorEdadRiesgo
        );

        indicadores.setProvisionTotal(
                provisionTotal
        );

        indicadores.setValorGarantias(
                valorGarantias
        );

        BigDecimal coberturaGarantias =
                porcentaje(
                        valorGarantias,
                        saldoTotalCredito
                );

        indicadores.setCoberturaGarantias(
                coberturaGarantias
        );

        indicadores.setGarantiasSuficientes(
                coberturaGarantias != null
                        && coberturaGarantias.compareTo(CIEN) >= 0
        );

        indicadores.setPorcentajeCoberturaPatrimonial(
                porcentaje(
                        totalPatrimonio,
                        saldoTotalCredito
                )
        );

        completarIndicadoresFinancieros(
                expediente,
                indicadores
        );

        completarIndicadoresCumplimiento(
                expediente,
                indicadores
        );

        determinarRiesgoGeneral(
                indicadores
        );

        indicadores.setFechaActualizacion(
                LocalDate.now()
        );

        return indicadores;
    }

    private void completarIndicadoresFinancieros(
            ExpedienteAsociadoDTO expediente,
            ExpedienteIndicadoresDTO indicadores
    ) {
        ExpedienteInformacionFinancieraDTO financiero =
                expediente.getInformacionFinanciera();

        if (financiero == null) {
            return;
        }

        indicadores.setPorcentajeEndeudamiento(
                financiero.getPorcentajeEndeudamiento()
        );

        indicadores.setPorcentajeCompromisoIngresos(
                financiero.getPorcentajeGastos()
        );
    }

    private void completarIndicadoresCumplimiento(
            ExpedienteAsociadoDTO expediente,
            ExpedienteIndicadoresDTO indicadores
    ) {
        indicadores.setContactoCompleto(
                expediente.getContacto() != null
                        && Boolean.TRUE.equals(
                        expediente.getContacto()
                                .getContactoCompleto()
                )
        );

        indicadores.setInformacionFinancieraCompleta(
                expediente.getInformacionFinanciera() != null
                        && Boolean.TRUE.equals(
                        expediente.getInformacionFinanciera()
                                .getInformacionActualizada()
                )
        );

        /*
         * Temporalmente se considera cargado el bloque SARLAFT
         * cuando la vista devuelve información del asociado.
         *
         * La vigencia por fecha se implementará posteriormente
         * utilizando el parámetro 121.
         */
        indicadores.setSarlaftVigente(
                expediente.getSarlaft() != null
        );

        ExpedienteAfiliacionDTO afiliacion =
                expediente.getAfiliacion();

        indicadores.setDocumentacionCompleta(
                afiliacion != null
                        && Boolean.TRUE.equals(
                        afiliacion.getDocumentacionCompleta()
                )
        );
    }

    private void determinarRiesgoGeneral(
            ExpedienteIndicadoresDTO indicadores
    ) {
        if (indicadores.getCreditosEnMora() != null
                && indicadores.getCreditosEnMora() > 0) {

            indicadores.setNivelRiesgo("ALTO");
            indicadores.setColorRiesgo("ROJO");
            indicadores.setEstadoGeneral("REQUIERE_ATENCION");

            indicadores.setResumenEjecutivo(
                    "El asociado presenta obligaciones en mora."
            );

            return;
        }

        if (!Boolean.TRUE.equals(indicadores.getSarlaftVigente())
                || !Boolean.TRUE.equals(
                indicadores.getContactoCompleto()
        )
                || !Boolean.TRUE.equals(
                indicadores.getInformacionFinancieraCompleta()
        )) {

            indicadores.setNivelRiesgo("MEDIO");
            indicadores.setColorRiesgo("AMARILLO");
            indicadores.setEstadoGeneral("REQUIERE_ACTUALIZACION");

            indicadores.setResumenEjecutivo(
                    "El asociado presenta información pendiente "
                            + "de actualización."
            );

            return;
        }

        indicadores.setNivelRiesgo("BAJO");
        indicadores.setColorRiesgo("VERDE");
        indicadores.setEstadoGeneral("NORMAL");

        indicadores.setResumenEjecutivo(
                "El expediente no presenta novedades críticas."
        );
    }

    // =========================================================
    // Alertas
    // =========================================================

    private List<ExpedienteAlertaDTO> generarAlertas(
            ExpedienteAsociadoDTO expediente
    ) {
        List<ExpedienteAlertaDTO> alertas =
                new ArrayList<>();

        generarAlertasContacto(expediente, alertas);
        generarAlertasFinancieras(expediente, alertas);
        generarAlertasSarlaft(expediente, alertas);
        generarAlertasCartera(expediente, alertas);
        generarAlertasCdats(expediente, alertas);
        generarAlertasBienes(expediente, alertas);

        return alertas;
    }

    private void generarAlertasContacto(
            ExpedienteAsociadoDTO expediente,
            List<ExpedienteAlertaDTO> alertas
    ) {
        ExpedienteContactoDTO contacto =
                expediente.getContacto();

        if (contacto == null
                || !Boolean.TRUE.equals(
                contacto.getContactoCompleto()
        )) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "CONTACTO_INCOMPLETO",
                            "Información de contacto incompleta",
                            "El asociado no tiene dirección, celular "
                                    + "o correo electrónico completo.",
                            "HOJA_VIDA",
                            "CONTACTO",
                            "ADVERTENCIA"
                    )
            );
        }
    }

    private void generarAlertasFinancieras(
            ExpedienteAsociadoDTO expediente,
            List<ExpedienteAlertaDTO> alertas
    ) {
        ExpedienteInformacionFinancieraDTO financiero =
                expediente.getInformacionFinanciera();

        if (financiero == null) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "FINANCIERO_INEXISTENTE",
                            "Información financiera no registrada",
                            "No se encontró información financiera "
                                    + "del asociado.",
                            "HOJA_VIDA",
                            "FINANCIERO",
                            "ADVERTENCIA"
                    )
            );

            return;
        }

        if (!Boolean.TRUE.equals(
                financiero.getInformacionActualizada()
        )) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "FINANCIERO_DESACTUALIZADO",
                            "Información financiera desactualizada",
                            "La información financiera requiere "
                                    + "actualización.",
                            "HOJA_VIDA",
                            "FINANCIERO",
                            "ADVERTENCIA"
                    )
            );
        }

        if (!Boolean.TRUE.equals(
                financiero.getCapacidadPagoSuficiente()
        )) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "CAPACIDAD_PAGO_INSUFICIENTE",
                            "Capacidad de pago insuficiente",
                            "Los egresos registrados comprometen "
                                    + "la capacidad de pago.",
                            "GERENCIA",
                            "CAPACIDAD_PAGO",
                            "CRITICA"
                    )
            );
        }
    }

    private void generarAlertasSarlaft(
            ExpedienteAsociadoDTO expediente,
            List<ExpedienteAlertaDTO> alertas
    ) {
        ExpedienteSarlaftDTO sarlaft =
                expediente.getSarlaft();

        if (sarlaft == null) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "SARLAFT_INEXISTENTE",
                            "Información SARLAFT no registrada",
                            "No se encontró información SARLAFT del asociado.",
                            "HOJA_VIDA",
                            "SARLAFT",
                            "ADVERTENCIA"
                    )
            );

            return;
        }

        /*
         * La condición PEP es información declarada del asociado.
         * No constituye por sí sola un análisis de riesgo.
         */
        if (Boolean.TRUE.equals(sarlaft.getAsociadoPeps())
                || Boolean.TRUE.equals(sarlaft.getFamiliaPeps())) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "CONDICION_PEP",
                            "Condición PEP registrada",
                            "El asociado o un familiar registra condición PEP.",
                            "HOJA_VIDA",
                            "SARLAFT_PEP",
                            "INFORMATIVA"
                    )
            );
        }

        /*
         * Información declarada sobre operaciones internacionales.
         */
        if (Boolean.TRUE.equals(sarlaft.getMonedaExtranjera())) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "OPERACIONES_MONEDA_EXTRANJERA",
                            "Operaciones en moneda extranjera",
                            "El asociado declara operaciones en moneda extranjera.",
                            "HOJA_VIDA",
                            "SARLAFT_MONEDA_EXTRANJERA",
                            "INFORMATIVA"
                    )
            );
        }

        if (Boolean.TRUE.equals(sarlaft.getCuentaExtranjero())) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "CUENTA_EN_EXTERIOR",
                            "Cuenta registrada en el exterior",
                            "El asociado declara una cuenta financiera en el exterior.",
                            "HOJA_VIDA",
                            "SARLAFT_CUENTA_EXTERIOR",
                            "INFORMATIVA"
                    )
            );
        }

        /*
         * Información fiscal internacional declarada.
         */
        if (Boolean.TRUE.equals(sarlaft.getCiudadanoEstadosUnidos())
                || Boolean.TRUE.equals(
                sarlaft.getResidenteFiscalEstadosUnidos()
        )
                || Boolean.TRUE.equals(
                sarlaft.getResidenteFiscalExterior()
        )) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "RESIDENCIA_FISCAL_INTERNACIONAL",
                            "Residencia fiscal internacional registrada",
                            "El asociado registra información fiscal relacionada "
                                    + "con Estados Unidos o con otro país.",
                            "HOJA_VIDA",
                            "SARLAFT_RESIDENCIA_FISCAL",
                            "INFORMATIVA"
                    )
            );
        }

        /*
         * Condiciones declaradas de protección constitucional.
         */
        if (Boolean.TRUE.equals(
                sarlaft.getPerteneceGrupoProteccionConstitucional()
        )
                || Boolean.TRUE.equals(
                sarlaft.getGrupoProteccionEspecialConstitucional()
        )) {

            alertas.add(
                    crearAlerta(
                            expediente,
                            "PROTECCION_CONSTITUCIONAL",
                            "Condición de protección constitucional",
                            "El asociado registra pertenencia a un grupo "
                                    + "de protección constitucional.",
                            "HOJA_VIDA",
                            "CONDICIONES_PROTECCION",
                            "INFORMATIVA"
                    )
            );
        }
    }

    private void generarAlertasCartera(
            ExpedienteAsociadoDTO expediente,
            List<ExpedienteAlertaDTO> alertas
    ) {
        for (ExpedienteCreditoDTO credito
                : expediente.getCreditos()) {

            if (!Boolean.TRUE.equals(credito.getEnMora())) {
                continue;
            }

            ExpedienteAlertaDTO alerta =
                    crearAlerta(
                            expediente,
                            "CREDITO_EN_MORA",
                            "Crédito en mora",
                            "El crédito "
                                    + valorTexto(
                                    credito.getNumeroCredito()
                            )
                                    + " presenta mora.",
                            "CARTERA",
                            "MORA",
                            nivelMora(credito.getDiasMora())
                    );

            alerta.setIdCredito(
                    credito.getIdCredito()
            );

            alerta.setReferenciaRegistro(
                    credito.getNumeroCredito()
            );

            alertas.add(alerta);
        }
    }

    private void generarAlertasCdats(
            ExpedienteAsociadoDTO expediente,
            List<ExpedienteAlertaDTO> alertas
    ) {
        for (ExpedienteCdatDTO cdat : expediente.getCdats()) {

            if (Boolean.TRUE.equals(cdat.getVencido())) {

                ExpedienteAlertaDTO alerta =
                        crearAlerta(
                                expediente,
                                "CDAT_VENCIDO",
                                "CDAT vencido",
                                "El CDAT "
                                        + valorTexto(
                                        cdat.getNumeroCdat()
                                )
                                        + " se encuentra vencido.",
                                "CDAT",
                                "VENCIMIENTOS",
                                "CRITICA"
                        );

                alerta.setIdCdat(cdat.getIdCdat());
                alerta.setReferenciaRegistro(
                        cdat.getNumeroCdat()
                );

                alertas.add(alerta);

            } else if (Boolean.TRUE.equals(
                    cdat.getProximoVencer()
            )) {

                ExpedienteAlertaDTO alerta =
                        crearAlerta(
                                expediente,
                                "CDAT_PROXIMO_VENCER",
                                "CDAT próximo a vencer",
                                "El CDAT "
                                        + valorTexto(
                                        cdat.getNumeroCdat()
                                )
                                        + " está próximo a vencer.",
                                "CDAT",
                                "VENCIMIENTOS",
                                "ADVERTENCIA"
                        );

                alerta.setIdCdat(cdat.getIdCdat());
                alerta.setReferenciaRegistro(
                        cdat.getNumeroCdat()
                );

                alertas.add(alerta);
            }
        }
    }

    private void generarAlertasBienes(
            ExpedienteAsociadoDTO expediente,
            List<ExpedienteAlertaDTO> alertas
    ) {
        for (ExpedienteBienInmuebleDTO bien
                : expediente.getBienesInmuebles()) {

            if (Boolean.TRUE.equals(
                    bien.getRequiereRevision()
            )) {

                alertas.add(
                        crearAlertaBien(
                                expediente,
                                bien.getIdBien(),
                                "INMUEBLE_REQUIERE_REVISION",
                                "Inmueble requiere revisión",
                                bien.getDescripcionGeneral(),
                                "INMUEBLES"
                        )
                );
            }
        }

        for (ExpedienteBienVehiculoDTO bien
                : expediente.getBienesVehiculos()) {

            if (Boolean.TRUE.equals(
                    bien.getRequiereRevision()
            )) {

                alertas.add(
                        crearAlertaBien(
                                expediente,
                                bien.getIdBien(),
                                "VEHICULO_REQUIERE_REVISION",
                                "Vehículo requiere revisión",
                                bien.getDescripcionGeneral(),
                                "VEHICULOS"
                        )
                );
            }
        }

        for (ExpedienteBienMaquinariaDTO bien
                : expediente.getBienesMaquinaria()) {

            if (Boolean.TRUE.equals(
                    bien.getRequiereRevision()
            )) {

                alertas.add(
                        crearAlertaBien(
                                expediente,
                                bien.getIdBien(),
                                "MAQUINARIA_REQUIERE_REVISION",
                                "Maquinaria requiere revisión",
                                bien.getDescripcionGeneral(),
                                "MAQUINARIA"
                        )
                );
            }
        }

        for (ExpedienteBienInversionDTO bien
                : expediente.getBienesInversiones()) {

            if (Boolean.TRUE.equals(
                    bien.getRequiereRevision()
            )) {

                alertas.add(
                        crearAlertaBien(
                                expediente,
                                bien.getIdBien(),
                                "INVERSION_REQUIERE_REVISION",
                                "Inversión requiere revisión",
                                bien.getDescripcionGeneral(),
                                "INVERSIONES"
                        )
                );
            }
        }
    }

    private ExpedienteAlertaDTO crearAlertaBien(
            ExpedienteAsociadoDTO expediente,
            Long idBien,
            String codigo,
            String titulo,
            String descripcion,
            String submodulo
    ) {
        ExpedienteAlertaDTO alerta =
                crearAlerta(
                        expediente,
                        codigo,
                        titulo,
                        valorTexto(descripcion),
                        "HOJA_VIDA",
                        submodulo,
                        "ADVERTENCIA"
                );

        alerta.setIdBien(idBien);

        return alerta;
    }

    private ExpedienteAlertaDTO crearAlerta(
            ExpedienteAsociadoDTO expediente,
            String codigo,
            String titulo,
            String descripcion,
            String modulo,
            String submodulo,
            String nivel
    ) {
        ExpedienteAlertaDTO alerta =
                new ExpedienteAlertaDTO();

        alerta.setCodigoAlerta(codigo);
        alerta.setTitulo(titulo);
        alerta.setDescripcion(descripcion);

        alerta.setIdDatosPersonal(
                expediente.getIdDatosPersonal()
        );

        alerta.setTipoDocumento(
                expediente.getTipoDocumento()
        );

        alerta.setDocumento(
                expediente.getDocumento()
        );

        alerta.setNombreCompleto(
                expediente.getNombreCompleto()
        );

        alerta.setModulo(modulo);
        alerta.setSubmodulo(submodulo);

        alerta.setNivel(nivel);
        alerta.setPrioridad(nivel);

        alerta.setTipoAlerta("AUTOMATICA");
        alerta.setCategoria("EXPEDIENTE_ASOCIADO");

        alerta.setCodigoEstado("ABIERTA");
        alerta.setNombreEstado("Abierta");

        alerta.setActiva(Boolean.TRUE);
        alerta.setAbierta(Boolean.TRUE);
        alerta.setAtendida(Boolean.FALSE);
        alerta.setDescartada(Boolean.FALSE);

        alerta.setRequiereGestion(
                !"INFORMATIVA".equalsIgnoreCase(nivel)
        );

        alerta.setFechaGeneracion(
                LocalDateTime.now()
        );

        alerta.setFechaPrimeraDeteccion(
                LocalDateTime.now()
        );

        alerta.setFechaUltimaDeteccion(
                LocalDateTime.now()
        );

        alerta.setIdAgencia(
                expediente.getIdAgenciaConsulta() == null
                        ? null
                        : expediente.getIdAgenciaConsulta()
                        .longValue()
        );

        alerta.setFkSeguridadCreacion(
                expediente.getIdUsuarioConsulta()
        );

        alerta.setFechaCreacion(
                LocalDateTime.now()
        );

        return alerta;
    }

    // =========================================================
    // Sincronización final
    // =========================================================

    private void sincronizarIndicadoresConAlertas(
            ExpedienteAsociadoDTO expediente
    ) {
        if (expediente.getIndicadores() == null) {
            return;
        }

        expediente.getIndicadores()
                .setCantidadAlertasCriticas(
                        expediente.getCantidadAlertasCriticas()
                );

        expediente.getIndicadores()
                .setCantidadAlertasAdvertencia(
                        expediente.getCantidadAlertasAdvertencia()
                );

        expediente.getIndicadores()
                .setCantidadAlertasInformativas(
                        expediente.getCantidadAlertasInformativas()
                );
    }

    private boolean esCuentaDeAportes(
            ExpedienteCuentaAhorroDTO cuenta
    ) {

        if (cuenta == null) {
            return false;
        }

        String codigoTipoCaptacion =
                cuenta.getCodigoTipoCaptacion();

        if (codigoTipoCaptacion == null
                || codigoTipoCaptacion.isBlank()) {

            return false;
        }

        return "1".equals(
                codigoTipoCaptacion.trim()
        );
    }

    // =========================================================
    // Utilidades financieras
    // =========================================================



    private BigDecimal totalBienes(
            ExpedienteAsociadoDTO expediente
    ) {
        BigDecimal inmuebles =
                expediente.getBienesInmuebles()
                        .stream()
                        .map(
                                ExpedienteBienInmuebleDTO
                                        ::getValorNeto
                        )
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal vehiculos =
                expediente.getBienesVehiculos()
                        .stream()
                        .map(
                                ExpedienteBienVehiculoDTO
                                        ::getValorNeto
                        )
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal maquinaria =
                expediente.getBienesMaquinaria()
                        .stream()
                        .map(
                                ExpedienteBienMaquinariaDTO
                                        ::getValorNeto
                        )
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        BigDecimal inversiones =
                expediente.getBienesInversiones()
                        .stream()
                        .map(
                                ExpedienteBienInversionDTO
                                        ::getValorNeto
                        )
                        .map(this::valorSeguro)
                        .reduce(CERO, BigDecimal::add);

        return inmuebles
                .add(vehiculos)
                .add(maquinaria)
                .add(inversiones);
    }


    private BigDecimal porcentaje(
            BigDecimal numerador,
            BigDecimal denominador
    ) {
        BigDecimal divisor =
                valorSeguro(denominador);

        if (divisor.compareTo(CERO) <= 0) {
            return null;
        }

        return valorSeguro(numerador)
                .multiply(CIEN)
                .divide(
                        divisor,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal valorSeguro(
            BigDecimal valor
    ) {
        return valor == null ? CERO : valor;
    }

    // =========================================================
    // Utilidades generales
    // =========================================================

    private <T> List<T> listaSegura(
            List<T> lista
    ) {
        return lista == null
                ? new ArrayList<>()
                : new ArrayList<>(lista);
    }

    private String nivelMora(
            Integer diasMora
    ) {
        if (diasMora == null || diasMora <= 0) {
            return "ADVERTENCIA";
        }

        if (diasMora > 30) {
            return "CRITICA";
        }

        return "ADVERTENCIA";
    }

    private int ordenEdadRiesgo(
            String edad
    ) {
        if (edad == null || edad.isBlank()) {
            return 0;
        }

        return switch (edad.trim().toUpperCase()) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            case "D" -> 4;
            case "E" -> 5;
            default -> 0;
        };
    }

    private boolean tieneTexto(
            String valor
    ) {
        return valor != null && !valor.isBlank();
    }

    private String valorTexto(
            String valor
    ) {
        return tieneTexto(valor)
                ? valor.trim()
                : "sin referencia";
    }

    private String mensajeSeguro(
            RuntimeException ex
    ) {
        if (ex.getMessage() == null
                || ex.getMessage().isBlank()) {

            return ex.getClass().getSimpleName();
        }

        return ex.getMessage();
    }

    private void validarIdDatosPersonal(
            Long idDatosPersonal
    ) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El idDatosPersonal debe ser mayor que cero."
            );
        }
    }
}