package co.assip.erp.cajas.provisiones;

import co.assip.erp.cajas.provisiones.dto.CajaAperturaDTO;
import co.assip.erp.cajas.provisiones.dto.CajaDisponibleDTO;
import co.assip.erp.cajas.provisiones.dto.CajaEstadoDTO;
import co.assip.erp.cajas.provisiones.dto.CajaProvisionActivaDTO;
import co.assip.erp.cajas.provisiones.dto.CajaUsuarioDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionCerrarDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionFormDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionListDTO;
import co.assip.erp.cajas.provisiones.dto.CajasProvisionSaveDTO;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasProvisionService {

    private final CajasProvisionRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final CajasUsuariosService cajasUsuariosService;

    public List<CajasProvisionListDTO> listar() {
        return repository.listar();
    }

    public CajasProvisionFormDTO obtenerPorId(
            Long idProvision
    ) {

        return repository
                .obtenerPorId(idProvision)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la provisión de caja."
                        )
                );
    }

    /*
     * Se conserva este método para los procesos administrativos
     * existentes de provisiones.
     *
     * El flujo normal de inicio de caja utiliza abrirCaja().
     */
    @Transactional
    public Long guardar(
            CajasProvisionSaveDTO dto
    ) {

        validarMantenimiento(dto);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        if (repository.existeProvision(
                dto.getIdCaja(),
                dto.getFechaContable(),
                dto.getIdProvision()
        )) {
            throw new RuntimeException(
                    "Ya existe una provisión para esta caja y fecha contable."
            );
        }

        if (dto.getIdProvision() == null) {
            return repository.crear(
                    dto,
                    idUsuario
            );
        }

        repository.actualizar(
                dto,
                idUsuario
        );

        return dto.getIdProvision();
    }

    /**
     * Prepara el inicio de caja del usuario autenticado.
     *
     * La vinculación activa determina la caja.
     * PostgreSQL determina la fecha de jornada.
     */
    public CajaAperturaDTO prepararAperturaUsuario() {

        CajaUsuarioDTO vinculacion =
                cajasUsuariosService.obtenerActivaUsuario();

        return repository
                .prepararApertura(
                        vinculacion.getIdCaja()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la caja vinculada al usuario."
                        )
                );
    }

    /**
     * Abre la provisión diaria de la caja vinculada al usuario.
     *
     * OPERATIVA:
     * - Crea la provisión diaria.
     * - Registra el traslado inicial a ventanilla, cuando corresponde.
     *
     * CONTROL:
     * - Crea la provisión diaria.
     * - No registra movimientos iniciales de ventanilla.
     *
     * La fecha de jornada es determinada por PostgreSQL.
     */
    @Transactional
    public Long abrirCaja(CajasProvisionSaveDTO dto) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar los datos de apertura de caja."
            );
        }

        CajaUsuarioDTO vinculacion =
                cajasUsuariosService.obtenerActivaUsuario();

        Long idCaja = vinculacion.getIdCaja();

        if (dto.getIdCaja() == null
                || !dto.getIdCaja().equals(idCaja)) {

            throw new RuntimeException(
                    "La apertura debe realizarse sobre la caja vinculada al usuario."
            );
        }

        validarValoresIniciales(
                dto.getEfectivoInicio(),
                dto.getChequesInicio()
        );

        /*
         * Evita aperturas concurrentes sobre la misma caja.
         */
        repository.bloquearCajaParaApertura(idCaja);

        CajaAperturaDTO apertura = repository
                .prepararApertura(idCaja)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró la caja vinculada al usuario."
                        )
                );

        String tipoCaja = apertura.getTipoCaja();

        boolean esOperativa =
                "OPERATIVA".equalsIgnoreCase(tipoCaja);

        boolean esControl =
                "CONTROL".equalsIgnoreCase(tipoCaja);

        if (!esOperativa && !esControl) {
            throw new RuntimeException(
                    "El tipo de caja no permite apertura de jornada."
            );
        }

        /*
         * Si la provisión ya existe, no se vuelve a crear.
         */
        if (Boolean.TRUE.equals(apertura.getTieneProvision())) {

            if ("CERRADA".equalsIgnoreCase(apertura.getEstado())) {
                throw new RuntimeException(
                        "La caja ya fue cerrada para la jornada actual."
                );
            }

            if (!"ABIERTA".equalsIgnoreCase(apertura.getEstado())) {
                throw new RuntimeException(
                        "La provisión de caja se encuentra en un estado no válido."
                );
            }

            return apertura.getIdProvision();
        }

        /*
         * El valor inicial de ventanilla solamente aplica
         * para cajas OPERATIVAS.
         */
        BigDecimal valorInicialVentanilla =
                dto.getValorInicialVentanilla();

        if (esOperativa) {

            validarValorInicialVentanilla(
                    valorInicialVentanilla,
                    dto.getEfectivoInicio()
            );

        } else {

            /*
             * CONTROL no maneja apertura de ventanilla.
             * Rechazamos cualquier valor diferente de cero.
             */
            if (valorInicialVentanilla != null
                    && valorInicialVentanilla.compareTo(
                    BigDecimal.ZERO
            ) != 0) {

                throw new RuntimeException(
                        "La caja CONTROL no permite asignar un valor inicial a ventanilla."
                );
            }
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        /*
         * Ambas cajas utilizan la misma tabla
         * de provisiones diarias.
         */
        Long idProvision = repository.crearApertura(
                idCaja,
                dto.getEfectivoInicio(),
                dto.getChequesInicio(),
                idUsuario
        );

        /*
         * Solo OPERATIVA registra el movimiento
         * inicial hacia ventanilla.
         */
        if (esOperativa
                && valorInicialVentanilla.compareTo(
                BigDecimal.ZERO
        ) > 0) {

            repository.registrarEntradaInicialVentanilla(
                    idProvision,
                    valorInicialVentanilla,
                    idUsuario
            );
        }

        return idProvision;
    }

    @Transactional
    public void cerrar(CajasProvisionCerrarDTO dto) {

        if (dto == null || dto.getIdProvision() == null) {
            throw new RuntimeException(
                    "Debe indicar la provisión a cerrar."
            );
        }

        if (dto.getEfectivoFin() == null
                || dto.getEfectivoFin().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "El efectivo final no puede ser negativo."
            );
        }

        if (dto.getChequesFin() == null
                || dto.getChequesFin().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(
                    "El valor final de cheques no puede ser negativo."
            );
        }

        CajaUsuarioDTO vinculacion =
                cajasUsuariosService.obtenerActivaUsuario();

        Integer idUsuario = usuarioSesionService.idUsuario();

        int actualizados = repository.cerrar(
                dto.getIdProvision(),
                vinculacion.getIdCaja(),
                dto.getEfectivoFin(),
                dto.getChequesFin(),
                dto.getObservacion(),
                idUsuario
        );

        if (actualizados != 1) {
            throw new RuntimeException(
                    "No fue posible cerrar la provisión. "
                            + "Verifique que corresponda a la caja vinculada, "
                            + "que pertenezca a la jornada actual "
                            + "y que se encuentre abierta."
            );
        }
    }

    public List<CajasProvisionListDTO> listarCajasAbiertas(
            Integer idAgencia,
            java.time.LocalDate fecha
    ) {

        if (idAgencia == null || fecha == null) {
            throw new RuntimeException(
                    "Agencia y fecha son obligatorias."
            );
        }

        return repository.listarCajasAbiertas(
                idAgencia,
                fecha
        );
    }

    public List<CajaDisponibleDTO> listarCajasDisponibles(
            Integer idAgencia
    ) {

        if (idAgencia == null) {
            throw new RuntimeException(
                    "La agencia es obligatoria."
            );
        }

        return repository.listarCajasDisponibles(
                idAgencia
        );
    }

    public List<CajaEstadoDTO> listarEstadoCajas(
            Integer idAgencia,
            java.time.LocalDate fechaContable
    ) {

        if (idAgencia == null) {
            throw new RuntimeException(
                    "La agencia es obligatoria."
            );
        }

        if (fechaContable == null) {
            throw new RuntimeException(
                    "La fecha contable es obligatoria."
            );
        }

        return repository.listarEstadoCajas(
                idAgencia,
                fechaContable
        );
    }

    public CajaProvisionActivaDTO
    obtenerProvisionActivaUsuario() {

        CajaUsuarioDTO vinculacion =
                cajasUsuariosService.obtenerActivaUsuario();

        return repository
                .obtenerProvisionActivaCaja(
                        vinculacion.getIdCaja()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "La caja vinculada al usuario no tiene una provisión abierta para la jornada actual."
                        )
                );
    }

    private void validarMantenimiento(
            CajasProvisionSaveDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar los datos de la provisión."
            );
        }

        if (dto.getIdCaja() == null) {
            throw new RuntimeException(
                    "Debe seleccionar la caja."
            );
        }

        if (dto.getFechaContable() == null) {
            throw new RuntimeException(
                    "Debe ingresar la fecha contable."
            );
        }

        validarValoresIniciales(
                dto.getEfectivoInicio(),
                dto.getChequesInicio()
        );
    }

    private void validarValoresIniciales(
            BigDecimal efectivoInicio,
            BigDecimal chequesInicio
    ) {

        if (efectivoInicio == null
                || efectivoInicio.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new RuntimeException(
                    "El efectivo inicial no puede ser negativo."
            );
        }

        if (chequesInicio == null
                || chequesInicio.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new RuntimeException(
                    "El valor inicial de cheques no puede ser negativo."
            );
        }
    }

    private void validarValorInicialVentanilla(
            BigDecimal valorInicialVentanilla,
            BigDecimal efectivoInicio
    ) {

        if (valorInicialVentanilla == null) {
            throw new RuntimeException(
                    "Debe indicar el valor inicial de ventanilla."
            );
        }

        if (valorInicialVentanilla.compareTo(
                BigDecimal.ZERO
        ) < 0) {
            throw new RuntimeException(
                    "El valor inicial de ventanilla no puede ser negativo."
            );
        }

        /*
         * La ventanilla pertenece a la misma caja.
         *
         * No puede trasladarse a ventanilla más efectivo
         * del que físicamente tiene la caja al iniciar.
         */
        if (valorInicialVentanilla.compareTo(
                efectivoInicio
        ) > 0) {
            throw new RuntimeException(
                    "El valor inicial de ventanilla no puede superar el efectivo disponible de la caja."
            );
        }
    }
}