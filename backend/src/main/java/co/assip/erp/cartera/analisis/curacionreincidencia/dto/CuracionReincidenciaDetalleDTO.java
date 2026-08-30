package co.assip.erp.cartera.analisis.curacionreincidencia.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CuracionReincidenciaDetalleDTO {

    private Long idCarteraCredito;
    private Long numeroEpisodio;

    private Integer idAgencia;
    private String codigoAgencia;
    private String nombreAgencia;

    private Integer idLineaCredito;
    private String codigoLineaCredito;
    private String nombreLineaCredito;

    private String pagareCartera;

    private Long idDatosPersonal;
    private String tipoDocumento;
    private String documento;
    private String nombreCompleto;

    private LocalDate periodoInicio;
    private LocalDate fechaInicio;

    private String edadEntrada;
    private String maximaEdadAlcanzada;

    private BigDecimal saldoInicio;
    private Integer diasMoraInicio;

    private Boolean curado;
    private LocalDate fechaCura;
    private Integer mesesHastaCura;
    private BigDecimal saldoCura;

    private Boolean seguimiento6mCompleto;
    private Boolean reincidente6m;

    private LocalDate fechaReincidencia;
    private Integer mesesHastaReincidencia;

    private String edadReincidencia;
    private BigDecimal saldoReincidencia;
    private Integer diasMoraReincidencia;

    private LocalDate fechaUltimoCorteObservado;
    private String edadActual;
    private BigDecimal saldoActual;
    private Integer diasMoraActual;

    private String telefono;
    private String celular;
    private String correo;
}
