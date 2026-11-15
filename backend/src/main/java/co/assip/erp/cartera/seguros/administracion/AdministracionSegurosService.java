package co.assip.erp.cartera.seguros.administracion;

import co.assip.erp.seguridad.sesion.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas transaccionales del mantenimiento de seguros de cartera. */
@Service
public class AdministracionSegurosService {
    private static final int ESCALA_PORCENTAJE = 6;
    private final AdministracionSegurosRepository repository;

    public AdministracionSegurosService(AdministracionSegurosRepository repository) {
        this.repository = repository;
    }

    /** Consulta configuraciones del crédito, incluidas las inactivas, respetando la agencia. */
    @Transactional(readOnly = true)
    public List<AdministracionSegurosRepository.SeguroCredito> consultar(
            Integer idCredito, Integer idAgencia) {
        validarCreditoYAgencia(idCredito, idAgencia);
        return repository.consultarSeguros(idCredito);
    }

    /** Consulta el porcentaje base predeterminado de la agencia para crear un seguro. */
    @Transactional(readOnly = true)
    public BigDecimal consultarPorcentajeBaseSeguro(Integer idAgencia) {
        validarId(idAgencia, "agencia");
        SecurityUtils.validarAgencia(idAgencia);
        return repository.consultarPorcentajeBaseSeguro(idAgencia)
                .orElseThrow(() -> new IllegalStateException(
                        "No está configurado el parámetro 641 de seguros para esta agencia."));
    }

    /** Crea el seguro usando la base editable o, si falta, el parámetro 641 de la agencia. */
    @Transactional
    public Integer crear(
            Integer idCredito,
            Integer idAgencia,
            AdministracionSegurosRepository.ConfiguracionSeguro configuracion) {
        validarCreditoYAgencia(idCredito, idAgencia);
        if (configuracion == null) {
            throw new IllegalArgumentException("Debe indicar la configuración del seguro.");
        }
        if (!repository.bloquearCreditoDesembolsado(idCredito, idAgencia)) {
            throw new IllegalArgumentException(
                    "El crédito no está desembolsado o no pertenece a la agencia indicada.");
        }
        if (!repository.consultarSeguros(idCredito).isEmpty()) {
            throw new IllegalStateException(
                    "El crédito ya tiene seguro. Utilice mantenimiento para editarlo.");
        }
        BigDecimal base = configuracion.porcentajeBaseSeguro() != null
                ? configuracion.porcentajeBaseSeguro()
                : consultarPorcentajeBaseSeguro(idAgencia);
        validarConfiguracion(base, configuracion.porcentajeExtraprima(),
                configuracion.fechaInicialCobro());
        AdministracionSegurosRepository.ConfiguracionSeguro calculada =
                prepararCreacion(configuracion, base);
        Integer id = repository.crearSeguro(idCredito, calculada, usuarioActual());
        if (id == null) {
            throw new IllegalStateException(
                    "No fue posible crear el seguro: ya existe una configuración.");
        }
        return id;
    }

    /** Edita base y extraprima, conserva otras condiciones y archiva la configuración anterior. */
    @Transactional
    public void editar(
            Integer idCredito,
            Integer idAgencia,
            Integer idCreditoSeguro,
            AdministracionSegurosRepository.ConfiguracionSeguro configuracion,
            boolean activo) {
        validarCreditoYAgencia(idCredito, idAgencia);
        validarId(idCreditoSeguro, "seguro");
        if (configuracion == null || configuracion.porcentajeBaseSeguro() == null
                || configuracion.porcentajeExtraprima() == null) {
            throw new IllegalArgumentException(
                    "Debe indicar el porcentaje base y la extraprima del seguro.");
        }
        validarPorcentajeNoNegativo(configuracion.porcentajeBaseSeguro(), "base");
        validarPorcentajeNoNegativo(configuracion.porcentajeExtraprima(), "extraprima");
        if (!repository.bloquearCreditoDesembolsado(idCredito, idAgencia)) {
            throw new IllegalArgumentException(
                    "El crédito no está desembolsado o no pertenece a la agencia indicada.");
        }
        var anterior = repository.bloquearSeguroParaEdicion(idCreditoSeguro, idCredito)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el seguro solicitado para este crédito."));
        if (anterior.activo() && !activo
                && repository.consultarSaldoSeguro(idCreditoSeguro)
                .compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "No se puede desactivar un seguro con saldo pendiente.");
        }
        AdministracionSegurosRepository.ConfiguracionSeguro calculada =
                prepararEdicion(anterior, configuracion.porcentajeBaseSeguro(),
                        configuracion.porcentajeExtraprima());
        boolean cambiaCondiciones =
                anterior.porcentajeBaseSeguro().compareTo(calculada.porcentajeBaseSeguro()) != 0
                        || anterior.porcentajeExtraprima().compareTo(calculada.porcentajeExtraprima()) != 0
                        || anterior.porcentajeAplicar().compareTo(calculada.porcentajeAplicar()) != 0
                        || anterior.activo() != activo;
        if (!cambiaCondiciones) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate vigenciaNueva = hoy.withDayOfMonth(1).plusMonths(1);
        LocalDate inicioAnterior = repository.consultarInicioVigenciaActual(
                idCreditoSeguro, anterior.fechaCreacion());
        if (inicioAnterior == null || !inicioAnterior.isBefore(vigenciaNueva)) {
            throw new IllegalStateException(
                    "Ya existe una modificación pendiente para el próximo período.");
        }
        int usuario = usuarioActual();
        repository.registrarHistorial(
                anterior, inicioAnterior, vigenciaNueva.minusDays(1), usuario);
        if (!repository.actualizarSeguro(
                idCreditoSeguro, idCredito, calculada, activo, usuario)) {
            throw new IllegalStateException("No fue posible actualizar el seguro.");
        }
    }

    /** Aplica las reglas generales del seguro nuevo y calcula la extraprima a cargo del asociado. */
    private AdministracionSegurosRepository.ConfiguracionSeguro prepararCreacion(
            AdministracionSegurosRepository.ConfiguracionSeguro c, BigDecimal base) {
        return new AdministracionSegurosRepository.ConfiguracionSeguro(
                base,
                "S",
                c.fechaInicialCobro(),
                BigDecimal.ZERO,
                c.porcentajeExtraprima(),
                calcularPorcentajeAplicar(base, c.porcentajeExtraprima()),
                true,
                false,
                true
        );
    }

    /** Actualiza base y extraprima sin alterar las demás condiciones originales del seguro. */
    private AdministracionSegurosRepository.ConfiguracionSeguro prepararEdicion(
            AdministracionSegurosRepository.SeguroMantenimiento anterior,
            BigDecimal base,
            BigDecimal extraprima) {
        return new AdministracionSegurosRepository.ConfiguracionSeguro(
                base,
                anterior.tipoCobroSeguro(),
                anterior.fechaInicialCobro(),
                anterior.porcentajeAuxilioEntidad(),
                extraprima,
                calcularPorcentajeAplicar(base, extraprima),
                anterior.sobreSaldoActual(),
                anterior.incluyeInteresesCausados(),
                anterior.entidadAsumeSeguroBase()
        );
    }

    /** Calcula exclusivamente la extraprima que debe asumir el asociado. */
    private BigDecimal calcularPorcentajeAplicar(BigDecimal porcentajeBase, BigDecimal extraprima) {
        validarPorcentajeNoNegativo(porcentajeBase, "base");
        validarPorcentajeNoNegativo(extraprima, "extraprima");
        return porcentajeBase.multiply(extraprima)
                .divide(new BigDecimal("100"), ESCALA_PORCENTAJE, RoundingMode.HALF_UP);
    }

    /** Valida los campos necesarios para crear el seguro, sin confiar en el cálculo del frontend. */
    private void validarConfiguracion(BigDecimal base, BigDecimal extraprima, LocalDate fechaInicial) {
        if (base == null || extraprima == null || fechaInicial == null) {
            throw new IllegalArgumentException(
                    "El porcentaje base, la extraprima y la fecha inicial son obligatorios.");
        }
        validarPorcentajeNoNegativo(base, "base");
        validarPorcentajeNoNegativo(extraprima, "extraprima");
    }

    /** Rechaza porcentajes nulos o negativos. */
    private void validarPorcentajeNoNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException(
                    "El porcentaje de " + campo + " no puede ser negativo.");
        }
    }

    /** Valida identificadores y acceso a la agencia del crédito mediante JWT. */
    private void validarCreditoYAgencia(Integer idCredito, Integer idAgencia) {
        validarId(idCredito, "crédito");
        validarId(idAgencia, "agencia");
        SecurityUtils.validarAgencia(idAgencia);
    }

    /** Impide identificadores nulos o no positivos. */
    private void validarId(Integer id, String nombre) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de " + nombre + " no es válido.");
        }
    }

    /** Obtiene el identificador del usuario autenticado para auditoría. */
    private int usuarioActual() {
        Integer usuario = SecurityUtils.getIdUsuario();
        if (usuario == null || usuario <= 0) {
            throw new SecurityException(
                    "No hay usuario autenticado para registrar la operación.");
        }
        return usuario;
    }
}
