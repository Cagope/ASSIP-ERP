package co.assip.erp.hojavida.bienesvehiculos;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bienes_vehiculos_seguros", schema = "hoja_vida")
public class BienVehiculoSeguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien_vehiculo_seguro")
    private Long idBienVehiculoSeguro;

    @Column(name = "id_bien", nullable = false)
    private Long idBien;

    @Column(name = "aseguradora", length = 150)
    private String aseguradora;

    @Column(name = "numero_poliza", length = 50)
    private String numeroPoliza;

    @Column(name = "valor_asegurado", nullable = false, precision = 30, scale = 2)
    private BigDecimal valorAsegurado;

    @Column(name = "fecha_inicio_seguro")
    private LocalDate fechaInicioSeguro;

    @Column(name = "fecha_vencimiento_seguro")
    private LocalDate fechaVencimientoSeguro;

    @Column(name = "estado_seguro", nullable = false, length = 1)
    private String estadoSeguro;

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