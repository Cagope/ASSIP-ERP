package co.assip.erp.cajas.operaciones_pendientes.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CajaOperacionPendienteDTO {

    // =========================================================
    // Movimiento de caja
    // =========================================================
    private Long idMovimientoCaja;
    private Long idProvision;
    private Long idCaja;

    private LocalDate fechaContable;
    private LocalTime horaMovimiento;

    // =========================================================
    // Operación
    // =========================================================
    private Long idOperacionCaja;
    private String codigoOperacion;
    private String nombreOperacion;

    private String naturaleza;
    private String medioPago;
    private BigDecimal valor;

    private String concepto;
    private String estado;

    // =========================================================
    // Origen
    // =========================================================
    private String moduloOrigen;
    private String procesoOrigen;
    private String tablaOrigen;
    private Long idOrigen;
    private String codigoReferencia;

    // =========================================================
    // Comprobante
    // =========================================================
    private String tipoComprobante;
    private String numeroComprobante;

    // =========================================================
    // Titular
    // =========================================================
    private Integer idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;
}