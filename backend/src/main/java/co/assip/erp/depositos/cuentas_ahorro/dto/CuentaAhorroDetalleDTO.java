package co.assip.erp.depositos.cuentas_ahorro.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;
import java.math.BigDecimal;

@Data
public class CuentaAhorroDetalleDTO {

    // Datos cuenta
    private Integer idCuentaAhorro;
    private Integer idAgencia;
    private Integer idFormaAhorro;
    private String codigoCuenta;
    private Integer idDatosPersonal;
    private String fechaAperturaCuenta;
    private BigDecimal  saldoInicialCuenta;
    private BigDecimal  saldoActualCuenta;
    private String estadoCuenta;
    private String fechaEstadoCuenta;
    private String gmfCuentaCuenta;
    private String fechaGmfCuenta;
    private Boolean libranzaCuenta;
    private String libranzaTiempoPago;
    private BigDecimal  cuotaMensualCuenta;
    private Boolean retencionFuenteCuenta;
    private Integer plazoCuenta;
    private String fechaFinalCuenta;
    private String cuentaActiva;
    private String cuentaConjunta;
    private String accionConjunta;
    private Double tasa;

    // ⭐ NUEVO — Estado real de la vista
    private String codigoEstado;
    private String nombreEstado;

    // Informativos
    private String nombreTitular;
    private String nombreAgencia;
    private String nombreForma;

    // También lo usa el front
    private String documento;

    // Relaciones 1:N
    private List<BeneficiarioDTO> beneficiarios;
    private List<PoderDTO> poderes;
    private List<CuentaConjuntaDTO> cuentasConjuntas;
}
