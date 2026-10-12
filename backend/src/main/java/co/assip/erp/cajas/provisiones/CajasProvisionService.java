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
     * Abre la provisión de la caja vinculada al usuario.
     *
     * El frontend no determina la fecha contable.
     */
    /**
     * Abre la provisión de la caja vinculada al usuario.
     *
     * El frontend no determina la fecha contable.
     *
     * Para cajas OPERATIVAS:
     * - crea la provisión diaria;
     * - registra el valor inicial llevado a ventanilla.
     *
     * La apertura y el movimiento inicial de ventanilla
     * quedan dentro de la misma transacción.
     */
    @Transactional
    public Long abrirCaja(
            CajasProvisionSaveDTO dto
    ) {

        if (dto == null) {
            throw new RuntimeException(
                    "Debe indicar los datos de apertura de caja."
            );
        }

        CajaUsuarioDTO vinculacion =
                cajasUsuariosService.obtenerActivaUsuario();

        Long idCaja =
                vinculacion.getIdCaja();

        /*
         * Evita que el cliente intente abrir una caja distinta
         * de la vinculada al usuario.
         */
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
         * Bloqueo pesimista de la caja.
         *
         * Dos solicitudes concurrentes para la misma caja
         * no pueden comprobar y crear la provisión al mismo tiempo.
         */
        repository.bloquearCajaParaApertura(idCaja);

        /*
         * La preparación se consulta después de adquirir el bloqueo.
         * Así conocemos el estado definitivo de la jornada antes
         * de intentar crear una nueva provisión.
         */
        CajaAperturaDTO apertura =
                repository
                        .prepararApertura(idCaja)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No se encontró la caja vinculada al usuario."
                                )
                        );

        /*
         * Este flujo corresponde exclusivamente a cajas
         * operativas con ventanilla.
         */
        if (!"OPERATIVA".equalsIgnoreCase(
                apertura.getTipoCaja()
        )) {
            throw new RuntimeException(
                    "La caja vinculada no es una caja operativa."
            );
        }

        /*
         * Ya existe provisión para la jornada.
         */
        if (Boolean.TRUE.equals(
                apertura.getTieneProvision()
        )) {

            if ("CERRADA".equalsIgnoreCase(
                    apertura.getEstado()
            )) {
                throw new RuntimeException(
                        "La caja ya fue cerrada para la jornada actual."
                );
            }

            if (!"ABIERTA".equalsIgnoreCase(
                    apertura.getEstado()
            )) {
                throw new RuntimeException(
                        "La provisión de caja se encuentra en un estado no válido."
                );
            }

            /*
             * Apertura idempotente.
             *
             * Si la caja ya estaba abierta, no volvemos a crear
             * provisión ni movimiento inicial de ventanilla.
             */
            return apertura.getIdProvision();
        }

        BigDecimal valorInicialVentanilla =
                dto.getValorInicialVentanilla();

        validarValorInicialVentanilla(
                valorInicialVentanilla,
                dto.getEfectivoInicio()
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long idProvision =
                repository.crearApertura(
                        idCaja,
                        dto.getEfectivoInicio(),
                        dto.getChequesInicio(),
                        idUsuario
                );

        /*
         * El traslado inicial Caja -> Ventanilla solamente
         * se registra cuando existe un valor real a trasladar.
         *
         * No genera movimiento de caja ni contabilización.
         */
        if (valorInicialVentanilla.compareTo(
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
    public void cerrar(
            CajasProvisionCerrarDTO dto
    ) {

        if (dto == null
                || dto.getIdProvision() == null) {

            throw new RuntimeException(
                    "Debe indicar la provisión a cerrar."
            );
        }

        if (dto.getEfectivoFin() == null
                || dto.getEfectivoFin()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "El efectivo final no puede ser negativo."
            );
        }

        if (dto.getChequesFin() == null
                || dto.getChequesFin()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "El valor final de cheques no puede ser negativo."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.cerrar(
                dto.getIdProvision(),
                dto.getEfectivoFin(),
                dto.getChequesFin(),
                dto.getObservacion(),
                idUsuario
        );
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