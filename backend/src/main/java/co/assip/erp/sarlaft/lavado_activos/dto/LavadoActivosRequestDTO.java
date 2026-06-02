package co.assip.erp.sarlaft.lavado_activos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LavadoActivosRequestDTO {

    private String modulo;
    private String proceso;
    private Long idOrigen;

    private LocalDate fechaTransaccion;
    private LocalDate fechaContable;

    private String tipoTransaccion;
    private BigDecimal valorTransaccion;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Long idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private String codigoProducto;
    private String descripcionProducto;

    private String numeroProducto;
    private String numeroComprobante;

    private String actividadEconomica;

    private String tipoDocumentoRealiza;
    private String documentoRealiza;

    private String primerApellidoRealiza;
    private String segundoApellidoRealiza;

    private String primerNombreRealiza;
    private String segundoNombreRealiza;

    private String direccionRealiza;
    private String telefonoRealiza;

    private String departamentoRealiza;
    private String ciudadRealiza;

    private String nombreBeneficiario;
    private String direccionBeneficiario;
    private String telefonoBeneficiario;
}