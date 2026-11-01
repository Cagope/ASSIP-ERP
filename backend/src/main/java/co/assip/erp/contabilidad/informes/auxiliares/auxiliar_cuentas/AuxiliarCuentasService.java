
package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas;

import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.AuxiliarCuentasRepository.CuentaAuxiliar;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.AuxiliarCuentasRepository.MovimientoAuxiliar;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.AuxiliarCuentasRepository.SaldoAnterior;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto.AuxiliarCuentaDTO;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto.AuxiliarCuentaMovimientoDTO;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto.AuxiliarCuentasFiltroDTO;
import co.assip.erp.contabilidad.informes.auxiliares.auxiliar_cuentas.dto.AuxiliarCuentasResponseDTO;
import co.assip.erp.seguridad.autorizacion.AccessValidator;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import co.assip.erp.seguridad.usuarios.Usuario;
import co.assip.erp.seguridad.usuarios.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye el auxiliar contable por cuentas y período.
 * Calcula saldos y limita la trazabilidad según los permisos efectivos.
 */
@Service
public class AuxiliarCuentasService {
    private static final String PERMISO_AUDITORIA =
            "CONTABILIDAD_AUXILIAR_CUENTAS_AUDIT_VIEW";
    private final AuxiliarCuentasRepository repository;
    private final UsuarioSesionService usuarioSesionService;
    private final UsuarioRepository usuarioRepository;
    private final AccessValidator accessValidator;

    public AuxiliarCuentasService(
            AuxiliarCuentasRepository repository,
            UsuarioSesionService usuarioSesionService,
            UsuarioRepository usuarioRepository,
            AccessValidator accessValidator
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
        this.usuarioRepository = usuarioRepository;
        this.accessValidator = accessValidator;
    }

    /**
     * Genera el auxiliar por agencia, cuentas y período.
     * La autorización de consulta y agencia se verifica en el controlador.
     * Excluye la trazabilidad cuando el usuario carece del permiso de auditoría.
     */
    @Transactional(readOnly = true)
    public AuxiliarCuentasResponseDTO generar(AuxiliarCuentasFiltroDTO filtros) {
        validarFiltros(filtros);
        boolean puedeVerAuditoria = puedeVerAuditoria();
        Integer idAgencia = filtros.idAgencia();
        String inicial = filtros.cuentaInicial().trim();
        String fin = filtros.cuentaFinal().trim();
        LocalDate fechaInicial = filtros.fechaInicial();
        LocalDate fechaFinal = filtros.fechaFinal();
        List<CuentaAuxiliar> cuentas = repository.listarCuentas(
                idAgencia,
                inicial,
                fin
        );
        List<AuxiliarCuentaDTO> resultado = new ArrayList<>();
        for (CuentaAuxiliar cuenta : cuentas) {
            SaldoAnterior anterior = repository.consultarSaldoAnterior(
                    idAgencia,
                    cuenta.idCatalogoCuenta(),
                    fechaInicial
            );
            BigDecimal saldoInicial = calcularSaldo(
                    cuenta.naturalezaCuenta(),
                    anterior.debitos(),
                    anterior.creditos()
            );
            List<MovimientoAuxiliar> movimientos = repository.consultarMovimientos(
                    idAgencia,
                    cuenta.idCatalogoCuenta(),
                    fechaInicial,
                    fechaFinal
            );
            if (movimientos.isEmpty()
                    && saldoInicial.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            BigDecimal saldoAcumulado = saldoInicial;
            BigDecimal totalDebitos = BigDecimal.ZERO;
            BigDecimal totalCreditos = BigDecimal.ZERO;
            List<AuxiliarCuentaMovimientoDTO> detalle = new ArrayList<>();
            for (MovimientoAuxiliar movimiento : movimientos) {
                BigDecimal debito = movimiento.valorDebito();
                BigDecimal credito = movimiento.valorCredito();
                totalDebitos = totalDebitos.add(debito);
                totalCreditos = totalCreditos.add(credito);
                saldoAcumulado = saldoAcumulado.add(
                        calcularSaldo(
                                cuenta.naturalezaCuenta(),
                                debito,
                                credito
                        )
                );
                detalle.add(new AuxiliarCuentaMovimientoDTO(
                        movimiento.idAuxiliarContable(),
                        movimiento.fechaAuxiliar(),
                        movimiento.tipoComprobante(),
                        movimiento.numeroComprobante(),
                        movimiento.detalleMovimiento(),
                        puedeVerAuditoria ? movimiento.estadoMovimiento() : null,
                        puedeVerAuditoria ? movimiento.idDatosPersonal() : null,
                        movimiento.documentoTercero(),
                        movimiento.nombreTercero(),
                        debito,
                        credito,
                        puedeVerAuditoria ? movimiento.valorBase() : null,
                        saldoAcumulado,
                        puedeVerAuditoria ? movimiento.fkSeguridadCreacion() : null,
                        puedeVerAuditoria ? movimiento.usernameCreacion() : null,
                        puedeVerAuditoria ? movimiento.nombreUsuarioCreacion() : null,
                        puedeVerAuditoria ? movimiento.fechaCreacion() : null,
                        puedeVerAuditoria ? movimiento.fkSeguridadEdicion() : null,
                        puedeVerAuditoria ? movimiento.usernameEdicion() : null,
                        puedeVerAuditoria ? movimiento.nombreUsuarioEdicion() : null,
                        puedeVerAuditoria ? movimiento.fechaEdicion() : null,
                        puedeVerAuditoria ? movimiento.origenModulo() : null,
                        puedeVerAuditoria ? movimiento.documentoOrigen() : null,
                        puedeVerAuditoria ? movimiento.documentoSoporte() : null
                ));
            }
            resultado.add(new AuxiliarCuentaDTO(
                    cuenta.idCatalogoCuenta(),
                    cuenta.codigoCuenta(),
                    cuenta.nombreCuenta(),
                    cuenta.naturalezaCuenta(),
                    saldoInicial,
                    detalle,
                    totalDebitos,
                    totalCreditos,
                    saldoAcumulado
            ));
        }
        return new AuxiliarCuentasResponseDTO(
                idAgencia,
                inicial,
                fin,
                fechaInicial,
                fechaFinal,
                resultado
        );
    }

    /**
     * Consulta el permiso de auditoría del usuario activo y autenticado.
     * Considera todos sus roles vigentes mediante AccessValidator.
     */
    private boolean puedeVerAuditoria() {
        Integer idUsuario = usuarioSesionService.idUsuario();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new SecurityException(
                        "El usuario autenticado no está activo."
                ));
        return accessValidator.tienePermiso(usuario, PERMISO_AUDITORIA);
    }

    /**
     * Calcula saldos y variaciones según la naturaleza contable.
     * D: débitos menos créditos. C: créditos menos débitos.
     */
    private BigDecimal calcularSaldo(
            String naturaleza,
            BigDecimal debitos,
            BigDecimal creditos
    ) {
        if ("D".equalsIgnoreCase(naturaleza)) {
            return debitos.subtract(creditos);
        }
        if ("C".equalsIgnoreCase(naturaleza)) {
            return creditos.subtract(debitos);
        }
        throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "La cuenta contable tiene una naturaleza no válida."
        );
    }

    /**
     * Valida agencia, códigos de cuentas y período.
     * Rechaza filtros incompletos o rangos inconsistentes.
     */
    private void validarFiltros(AuxiliarCuentasFiltroDTO filtros) {
        if (filtros == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar los filtros del informe."
            );
        }
        if (filtros.idAgencia() == null || filtros.idAgencia() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe seleccionar una agencia válida."
            );
        }
        if (filtros.cuentaInicial() == null || filtros.cuentaInicial().isBlank()
                || filtros.cuentaFinal() == null || filtros.cuentaFinal().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar las cuentas inicial y final."
            );
        }
        String inicial = filtros.cuentaInicial().trim();
        String fin = filtros.cuentaFinal().trim();
        if (!inicial.matches("[0-9]+") || !fin.matches("[0-9]+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Los códigos de cuentas deben ser numéricos."
            );
        }
        if (inicial.compareTo(fin) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cuenta inicial no puede ser posterior a la cuenta final."
            );
        }
        if (filtros.fechaInicial() == null || filtros.fechaFinal() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar las fechas inicial y final."
            );
        }
        if (filtros.fechaInicial().isAfter(filtros.fechaFinal())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }
    }
}
