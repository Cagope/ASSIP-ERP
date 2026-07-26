package co.assip.erp.hojavida.bienesinmueblesavaluos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "bienes_inmuebles_avaluos",
        schema = "hoja_vida"
)
public class BienInmuebleAvaluo {

    // =========================================================
    // Identificación
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien_inmueble_avaluo")
    private Long idBienInmuebleAvaluo;

    @Column(name = "id_bien", nullable = false)
    private Long idBien;

    // =========================================================
    // Fechas y vigencia del avalúo
    // =========================================================

    @Column(name = "fecha_avaluo", nullable = false)
    private LocalDate fechaAvaluo;

    @Column(name = "vigencia_anios", nullable = false)
    private Integer vigenciaAnios;

    @Column(name = "fecha_vencimiento_avaluo")
    private LocalDate fechaVencimientoAvaluo;

    // =========================================================
    // Valores generales del avalúo
    // =========================================================

    @Column(
            name = "valor_avaluo_comercial",
            nullable = false,
            precision = 30,
            scale = 2
    )
    private BigDecimal valorAvaluoComercial;

    @Column(
            name = "valor_avaluo_catastral",
            nullable = false,
            precision = 30,
            scale = 2
    )
    private BigDecimal valorAvaluoCatastral;

    // =========================================================
    // Componentes del avalúo
    // =========================================================

    @Column(
            name = "valor_terreno",
            nullable = false,
            precision = 30,
            scale = 2
    )
    private BigDecimal valorTerreno;

    @Column(
            name = "valor_construccion",
            nullable = false,
            precision = 30,
            scale = 2
    )
    private BigDecimal valorConstruccion;

    @Column(
            name = "valor_cultivos",
            nullable = false,
            precision = 30,
            scale = 2
    )
    private BigDecimal valorCultivos;

    @Column(
            name = "valor_otros",
            nullable = false,
            precision = 30,
            scale = 2
    )
    private BigDecimal valorOtros;

    // =========================================================
    // Información del informe
    // =========================================================

    @Column(name = "entidad_avaluadora", length = 150)
    private String entidadAvaluadora;

    @Column(name = "numero_informe", length = 50)
    private String numeroInforme;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    // =========================================================
    // Auditoría
    // =========================================================

    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_edicion")
    private Integer fkSeguridadEdicion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}