package co.assip.erp.cartera.cierremensual;

import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import co.assip.erp.cartera.cierremensual.hojavida.CierreHojaVidaCarteraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CierreMensualService {

    private final CierreMensualRepository repository;
    private final CierreMensualFotoRepository fotoRepository;
    private final UsuarioSesionService usuarioSesionService;
    private final CierreHojaVidaCarteraService cierreHojaVidaCarteraService;

    public CierreMensualService(
            CierreMensualRepository repository,
            CierreMensualFotoRepository fotoRepository,
            UsuarioSesionService usuarioSesionService,
            CierreHojaVidaCarteraService cierreHojaVidaCarteraService
    ) {
        this.repository = repository;
        this.fotoRepository = fotoRepository;
        this.usuarioSesionService = usuarioSesionService;
        this.cierreHojaVidaCarteraService =
                cierreHojaVidaCarteraService;
    }

    // =========================================================
    // LISTAR CIERRES
    // =========================================================

    @Transactional(readOnly = true)
    public List<CierreMensualDTO> listar() {
        return repository.listar();
    }

    // =========================================================
    // BUSCAR CIERRE POR ID
    // =========================================================

    @Transactional(readOnly = true)
    public CierreMensualDTO buscarPorId(
            Integer idCierreCartera
    ) {

        validarIdCierre(idCierreCartera);

        return repository.buscarPorId(idCierreCartera)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el cierre de cartera: "
                                        + idCierreCartera
                        )
                );
    }

    // =========================================================
    // EJECUTAR CIERRE MENSUAL
    //
    // Genera:
    // - fotografía de cartera
    // - precierre de Hoja de Vida
    // - base común de cálculos
    //
    // Al finalizar:
    // estado_fotografia = E
    // =========================================================

    public CierreMensualDTO ejecutar(
            LocalDate fechaCorte
    ) {

        validarFechaCorte(fechaCorte);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        CierreMensualDTO cierre =
                repository
                        .buscarPorFecha(fechaCorte)
                        .orElse(null);

        Integer idCierreCartera;

        if (cierre == null) {

            idCierreCartera =
                    repository.crear(
                            fechaCorte,
                            idUsuario
                    );

        } else {

            idCierreCartera =
                    cierre.getIdCierreCartera();

            String estadoFotografia =
                    normalizarEstado(
                            cierre.getEstadoFotografia()
                    );

            if ("C".equals(estadoFotografia)) {
                throw new IllegalStateException(
                        "La fotografía del cierre "
                                + fechaCorte
                                + " ya se encuentra cerrada en firme."
                );
            }

            if (fotoRepository.existeFoto(
                    idCierreCartera
            )) {
                throw new IllegalStateException(
                        "El cierre de cartera para la fecha "
                                + fechaCorte
                                + " ya tiene una fotografía generada."
                );
            }
        }

        generarFoto(
                idCierreCartera,
                fechaCorte,
                idUsuario
        );

        return recuperarCierreActualizado(
                idCierreCartera
        );
    }

    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // Permitido:
    // P = pendiente
    // E = en proceso
    //
    // No permitido:
    // C = fotografía cerrada en firme
    // =========================================================

    public CierreMensualDTO regenerar(
            Integer idCierreCartera
    ) {

        validarIdCierre(idCierreCartera);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        CierreMensualDTO cierre =
                repository.buscarPorId(idCierreCartera)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el cierre de cartera: "
                                                + idCierreCartera
                                )
                        );

        String estadoFotografia =
                normalizarEstado(
                        cierre.getEstadoFotografia()
                );

        if ("C".equals(estadoFotografia)) {

            throw new IllegalStateException(
                    "La fotografía del cierre "
                            + idCierreCartera
                            + " se encuentra cerrada en firme "
                            + "y no puede regenerarse."
            );
        }

        // =====================================================
        // 1. Eliminar tablas hijas de resultados
        // =====================================================

        fotoRepository.eliminarResultadosGarantias(
                idCierreCartera
        );

        // =====================================================
        // 2. Eliminar base de resultados
        // =====================================================

        fotoRepository.eliminarResultadosBase(
                idCierreCartera
        );

        // =====================================================
        // 3. Eliminar fotografía de créditos
        // =====================================================

        fotoRepository.eliminarFotoCreditos(
                idCierreCartera
        );

        int cantidadFotoAnterior =
                fotoRepository.contarCreditosFoto(
                        idCierreCartera
                );

        int cantidadResultadosAnteriores =
                fotoRepository.contarResultadosBase(
                        idCierreCartera
                );

        if (cantidadFotoAnterior != 0
                || cantidadResultadosAnteriores != 0) {

            throw new IllegalStateException(
                    "No fue posible limpiar completamente "
                            + "la fotografía anterior del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        generarFoto(
                idCierreCartera,
                cierre.getFechaCorte(),
                idUsuario
        );

        return recuperarCierreActualizado(
                idCierreCartera
        );
    }

    // =========================================================
    // GENERAR FOTOGRAFÍA
    // =========================================================

    private void generarFoto(
            Integer idCierreCartera,
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        // =====================================================
        // 1. Generar fotografía de créditos
        // =====================================================

        int cantidadInsertadaFoto =
                fotoRepository.generarFotoCreditos(
                        idCierreCartera,
                        idUsuario
                );

        if (cantidadInsertadaFoto <= 0) {

            throw new IllegalStateException(
                    "No se encontraron créditos con saldo "
                            + "para generar la fotografía del cierre "
                            + fechaCorte
            );
        }

        // =====================================================
        // 2. Generar precierre de Hoja de Vida
        // =====================================================

        CierreHojaVidaCarteraService.ResultadoPrecierreHojaVida
                resultadoHojaVida =
                cierreHojaVidaCarteraService.generarPrecierre(
                        idCierreCartera,
                        fechaCorte,
                        idUsuario
                );

        if (resultadoHojaVida == null) {

            throw new IllegalStateException(
                    "No fue posible generar el precierre "
                            + "de Hoja de Vida para el cierre "
                            + idCierreCartera
                            + "."
            );
        }

        // =====================================================
        // 3. Crear base de cálculos
        // =====================================================

        int cantidadResultadosCreados =
                fotoRepository.crearResultadosBase(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 4. Contar fotografía
        // =====================================================

        int cantidadFoto =
                fotoRepository.contarCreditosFoto(
                        idCierreCartera
                );

        int cantidadFotoConSaldo =
                fotoRepository.contarCreditosFotoConSaldo(
                        idCierreCartera
                );

        // =====================================================
        // 5. Contar base de cálculos
        // =====================================================

        int cantidadResultados =
                fotoRepository.contarResultadosBase(
                        idCierreCartera
                );

        // =====================================================
        // 6. Validar fotografía
        // =====================================================

        if (cantidadFoto != cantidadInsertadaFoto) {

            throw new IllegalStateException(
                    "Inconsistencia en la cantidad de créditos "
                            + "fotografiados. Insertados: "
                            + cantidadInsertadaFoto
                            + ". Encontrados en la foto: "
                            + cantidadFoto
                            + "."
            );
        }

        // =====================================================
        // 7. Validar foto contra base
        // =====================================================

        if (cantidadFotoConSaldo != cantidadResultados) {

            throw new IllegalStateException(
                    "No es posible preparar la fotografía. "
                            + "Créditos fotografiados con saldo: "
                            + cantidadFotoConSaldo
                            + ". Registros de resultados: "
                            + cantidadResultados
                            + "."
            );
        }

        // =====================================================
        // 8. Validar resultados insertados
        // =====================================================

        if (cantidadResultados
                != cantidadResultadosCreados) {

            throw new IllegalStateException(
                    "Inconsistencia en la creación de la base "
                            + "de cálculos comunes. Insertados: "
                            + cantidadResultadosCreados
                            + ". Encontrados: "
                            + cantidadResultados
                            + "."
            );
        }

        // =====================================================
        // 9. Calcular saldo total
        // =====================================================

        BigDecimal saldoCarteraMaestro =
                fotoRepository.obtenerSaldoCarteraFoto(
                        idCierreCartera
                );

        // =====================================================
        // 10. Actualizar saldo maestro
        // =====================================================

        repository.actualizarSaldoCarteraMaestro(
                idCierreCartera,
                saldoCarteraMaestro,
                idUsuario
        );

        // =====================================================
        // 11. Actualizar fotografía
        //
        // Aquí queda:
        // estado_fotografia = E
        // fecha_fotografia = CURRENT_TIMESTAMP
        // =====================================================

        int actualizados =
                repository.actualizarFoto(
                        idCierreCartera,
                        cantidadFotoConSaldo,
                        idUsuario
                );

        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible actualizar la cabecera "
                            + "de la fotografía del cierre "
                            + idCierreCartera
                            + "."
            );
        }
    }

    // =========================================================
    // RECUPERAR CIERRE ACTUALIZADO
    // =========================================================

    private CierreMensualDTO recuperarCierreActualizado(
            Integer idCierreCartera
    ) {

        return repository
                .buscarPorId(idCierreCartera)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No fue posible recuperar la cabecera "
                                        + "actualizada del cierre."
                        )
                );
    }

    // =========================================================
    // CERRAR FOTOGRAFÍA EN FIRME
    //
    // Solamente:
    // E -> C
    //
    // No modifica:
    // estado_cierre
    // fecha_finalizacion
    // =========================================================

    public CierreMensualDTO cerrarFotografia(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        CierreMensualDTO cierre =
                repository.buscarPorId(
                        idCierreCartera
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "No existe el cierre de cartera: "
                                        + idCierreCartera
                        )
                );

        String estadoFotografia =
                normalizarEstado(
                        cierre.getEstadoFotografia()
                );

        if (!"E".equals(estadoFotografia)) {

            if ("C".equals(estadoFotografia)) {
                throw new IllegalStateException(
                        "La fotografía del cierre "
                                + idCierreCartera
                                + " ya se encuentra cerrada en firme."
                );
            }

            throw new IllegalStateException(
                    "La fotografía solamente puede cerrarse "
                            + "cuando se encuentra En proceso."
            );
        }

        int cantidadFoto =
                fotoRepository.contarCreditosFoto(
                        idCierreCartera
                );

        if (cantidadFoto <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene fotografía de créditos."
            );
        }

        int cantidadResultados =
                fotoRepository.contarResultadosBase(
                        idCierreCartera
                );

        if (cantidadResultados <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene base de cálculos."
            );
        }

        int cantidadFotoConSaldo =
                fotoRepository.contarCreditosFotoConSaldo(
                        idCierreCartera
                );

        if (cantidadFotoConSaldo != cantidadResultados) {

            throw new IllegalStateException(
                    "No es posible cerrar la fotografía. "
                            + "Créditos fotografiados con saldo: "
                            + cantidadFotoConSaldo
                            + ". Registros de resultados: "
                            + cantidadResultados
                            + "."
            );
        }

        int actualizados =
                repository.finalizar(
                        idCierreCartera,
                        idUsuario
                );

        if (actualizados != 1) {

            throw new IllegalStateException(
                    "No fue posible cerrar la fotografía del cierre "
                            + idCierreCartera
                            + "."
            );
        }

        return recuperarCierreActualizado(
                idCierreCartera
        );
    }

    // =========================================================
    // CONSULTAR SI EXISTE FOTO
    // =========================================================

    @Transactional(readOnly = true)
    public boolean existeFoto(
            Integer idCierreCartera
    ) {

        validarIdCierre(idCierreCartera);

        buscarPorId(idCierreCartera);

        return fotoRepository.existeFoto(
                idCierreCartera
        );
    }

    // =========================================================
    // PREPARAR BASE DE CÁLCULOS DE CIERRE HISTÓRICO
    // =========================================================

    public int prepararBaseCalculosHistorico(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.buscarPorId(
                idCierreCartera
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "No existe el cierre de cartera: "
                                + idCierreCartera
                )
        );

        int cantidadFoto =
                fotoRepository.contarCreditosFotoConSaldo(
                        idCierreCartera
                );

        if (cantidadFoto <= 0) {

            throw new IllegalStateException(
                    "El cierre "
                            + idCierreCartera
                            + " no tiene fotografía de créditos."
            );
        }

        fotoRepository.crearResultadosBase(
                idCierreCartera,
                idUsuario
        );

        int cantidadResultados =
                fotoRepository.contarResultadosBase(
                        idCierreCartera
                );

        if (cantidadFoto != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia en la base de cálculos del cierre "
                            + idCierreCartera
                            + ". Créditos fotografiados: "
                            + cantidadFoto
                            + ". Resultados: "
                            + cantidadResultados
                            + "."
            );
        }

        return cantidadResultados;
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdCierre(
            Integer idCierreCartera
    ) {

        if (idCierreCartera == null
                || idCierreCartera <= 0) {

            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }
    }

    private void validarFechaCorte(
            LocalDate fechaCorte
    ) {

        if (fechaCorte == null) {

            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }
    }

    private String normalizarEstado(
            String estado
    ) {

        return estado == null
                ? ""
                : estado.trim().toUpperCase();
    }
}