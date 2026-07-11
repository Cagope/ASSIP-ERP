package co.assip.erp.hojavida.bienesinversiones;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class BienInversion {

    private Long idBienPersona;
    private Long idDatosPersonal;
    private BigDecimal porcentajePropiedad;

    private Long idBien;
    private Long idTipoBien;
    private String codigoTipoBien;
    private String nombreTipoBien;
    private String descripcionGeneral;
    private BigDecimal valorComercial;
    private BigDecimal valorGravamen;

    private Long idBienInversion;

    private Long idTipoInversion;
    private String nombreTipoInversion;

    private String entidad;
    private String numeroTitulo;
    private LocalDate fechaInversion;
    private LocalDate fechaVencimiento;

    private BigDecimal valorNominal;
    private BigDecimal valorActual;
    private BigDecimal tasaRendimiento;

    private Long idTipoGravamen;
    private String nombreGravamen;

    private String observaciones;

    private String tipoDocumento;
    private String nombreTipoDocumento;
    private String documento;
    private String nombreCompleto;

    private BigDecimal valorNetoBien;
    private BigDecimal valorPropiedadAsociado;
    private BigDecimal valorGravamenAsociado;
    private BigDecimal valorNetoAsociado;

    private Integer fkSeguridadCreacion;
    private LocalDateTime fechaCreacion;

    private Integer fkSeguridadEdicion;
    private LocalDateTime fechaEdicion;
}