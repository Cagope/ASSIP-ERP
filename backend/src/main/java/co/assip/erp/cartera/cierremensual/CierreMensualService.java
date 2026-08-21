package co.assip.erp.cartera.cierremensual;

import co.assip.erp.cartera.cierremensual.dto.CierreMensualDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
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

    public CierreMensualService(
            CierreMensualRepository repository,
            CierreMensualFotoRepository fotoRepository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.fotoRepository = fotoRepository;
        this.usuarioSesionService = usuarioSesionService;
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
    // FOTO:
    // - solamente créditos con saldo_actual > 0
    //
    // BASE DE CÁLCULOS:
    // - una fila por cada crédito fotografiado
    //
    // Por tanto:
    // cantidadFoto = cantidadResultados
    // =========================================================

    public CierreMensualDTO ejecutar(
            LocalDate fechaCorte
    ) {

        validarFechaCorte(fechaCorte);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 1. Buscar o crear cabecera
        // =====================================================

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

        // =====================================================
        // 2. Generar foto y base de cálculos
        // =====================================================

        generarFoto(
                idCierreCartera,
                fechaCorte,
                idUsuario
        );

        // =====================================================
        // 3. Devolver cierre actualizado
        // =====================================================

        return recuperarCierreActualizado(
                idCierreCartera
        );
    }

    // =========================================================
    // REGENERAR FOTOGRAFÍA
    //
    // - conserva la cabecera
    // - conserva id_cierre_cartera
    // - solamente estado P
    // - elimina cálculos comunes
    // - elimina foto
    // - vuelve a generar
    // =========================================================

    public CierreMensualDTO regenerar(
            Integer idCierreCartera
    ) {

        validarIdCierre(idCierreCartera);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 1. Recuperar cierre
        // =====================================================

        CierreMensualDTO cierre =
                repository.buscarPorId(idCierreCartera)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No existe el cierre de cartera: "
                                                + idCierreCartera
                                )
                        );

        // =====================================================
        // 2. Validar estado
        // =====================================================

        String estado =
                cierre.getEstadoCierre();

        if (estado == null
                || !"P".equalsIgnoreCase(
                estado.trim()
        )) {

            throw new IllegalStateException(
                    "La fotografía solamente puede regenerarse "
                            + "cuando el cierre se encuentra "
                            + "en estado En proceso."
            );
        }

        // =====================================================
        // 3. Eliminar base de cálculos
        // =====================================================

        fotoRepository.eliminarResultadosBase(
                idCierreCartera
        );

        // =====================================================
        // 4. Eliminar foto
        // =====================================================

        fotoRepository.eliminarFotoCreditos(
                idCierreCartera
        );

        // =====================================================
        // 5. Validar eliminación
        // =====================================================

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

        // =====================================================
        // 6. Generar nuevamente
        // =====================================================

        generarFoto(
                idCierreCartera,
                cierre.getFechaCorte(),
                idUsuario
        );

        // =====================================================
        // 7. Devolver cierre actualizado
        // =====================================================

        return recuperarCierreActualizado(
                idCierreCartera
        );
    }

    // =========================================================
    // GENERAR FOTO
    //
    // Solamente se fotografían créditos con saldo_actual > 0.
    //
    // Por cada crédito fotografiado se crea una fila
    // en cierres_cartera_resultados.
    //
    // Aquí todavía NO se calculan:
    // - días de mora
    // - edades
    // - aportes
    // - garantías
    // - VEA
    // - deterioros
    // =========================================================

    private void generarFoto(
            Integer idCierreCartera,
            LocalDate fechaCorte,
            Integer idUsuario
    ) {

        // =====================================================
        // 1. Generar fotografía de créditos activos
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
        // 2. Crear base de cálculos
        // =====================================================

        int cantidadResultadosCreados =
                fotoRepository.crearResultadosBase(
                        idCierreCartera,
                        idUsuario
                );

        // =====================================================
        // 3. Contar fotografía
        // =====================================================

        int cantidadFoto =
                fotoRepository.contarCreditosFoto(
                        idCierreCartera
                );

        // =====================================================
        // 4. Contar base de cálculos
        // =====================================================

        int cantidadResultados =
                fotoRepository.contarResultadosBase(
                        idCierreCartera
                );

        // =====================================================
        // 5. Validar cantidad insertada en fotografía
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
        // 6. Validar foto contra base de cálculos
        // =====================================================

        if (cantidadFoto != cantidadResultados) {

            throw new IllegalStateException(
                    "Inconsistencia al generar el cierre de cartera. "
                            + "Créditos fotografiados: "
                            + cantidadFoto
                            + ". Registros de cálculos comunes: "
                            + cantidadResultados
                            + "."
            );
        }

        // =====================================================
        // 7. Validar cantidad insertada en resultados
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
        // 8. Calcular saldo total de cartera activa
        // =====================================================

        BigDecimal saldoCarteraMaestro =
                fotoRepository.obtenerSaldoCarteraFoto(
                        idCierreCartera
                );

        // =====================================================
        // 9. Actualizar saldo maestro
        // =====================================================

        repository.actualizarSaldoCarteraMaestro(
                idCierreCartera,
                saldoCarteraMaestro,
                idUsuario
        );

        // =====================================================
        // 10. Actualizar cabecera
        //
        // cantidad_creditos = créditos activos fotografiados
        // =====================================================

        repository.actualizarFoto(
                idCierreCartera,
                cantidadFoto,
                idUsuario
        );
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
                                "Se generó la fotografía, pero no fue "
                                        + "posible recuperar la cabecera "
                                        + "del cierre."
                        )
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

    // =========================================================
    // PREPARAR BASE DE CÁLCULOS DE CIERRE HISTÓRICO
    //
    // - La cabecera ya existe.
    // - La fotografía ya existe.
    // - No modifica la fotografía.
    // - Crea cierres_cartera_resultados.
    // - Se usa para cierres migrados.
    // =========================================================

    public int prepararBaseCalculosHistorico(
            Integer idCierreCartera
    ) {

        validarIdCierre(
                idCierreCartera
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        // =====================================================
        // 1. Validar cierre
        // =====================================================

        repository.buscarPorId(
                idCierreCartera
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "No existe el cierre de cartera: "
                                + idCierreCartera
                )
        );

        // =====================================================
        // 2. Validar fotografía
        // =====================================================

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

        // =====================================================
        // 3. Crear base de cálculos faltante
        // =====================================================

        fotoRepository.crearResultadosBase(
                idCierreCartera,
                idUsuario
        );

        // =====================================================
        // 4. Validar base
        // =====================================================

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

}