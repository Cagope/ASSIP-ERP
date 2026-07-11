package co.assip.erp.hojavida.bienesinmueblesavaluos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bienes_inmuebles_avaluos", schema = "hoja_vida")
public class BienInmuebleAvaluo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien_inmueble_avaluo")
    private Long idBienInmuebleAvaluo;

    @Column(name = "id_bien", nullable = false)
    private Long idBien;

    @Column(name = "fecha_avaluo", nullable = false)
    private LocalDate fechaAvaluo;

    @Column(name = "vigencia_anios", nullable = false)
    private Integer vigenciaAnios;

    @Column(name = "fecha_vencimiento_avaluo")
    private LocalDate fechaVencimientoAvaluo;

    @Column(name = "valor_avaluo_comercial", nullable = false, precision = 30, scale = 2)
    private BigDecimal valorAvaluoComercial;

    @Column(name = "valor_avaluo_catastral", nullable = false, precision = 30, scale = 2)
    private BigDecimal valorAvaluoCatastral;

    @Column(name = "entidad_avaluadora", length = 150)
    private String entidadAvaluadora;

    @Column(name = "numero_informe", length = 50)
    private String numeroInforme;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_edicion")
    private Integer fkSeguridadEdicion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}