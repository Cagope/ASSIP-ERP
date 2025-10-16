package co.assip.erp.general.domain;

import co.assip.erp.hojavida.domain.DatosPersonales;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "directivos", schema = "general")
public class Directivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_directivo")
    private Integer idDirectivo;

    // FK a hoja_vida.datos_personales.id_datos_personal
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_datos_personal", nullable = false)
    private DatosPersonales persona;

    // FK a catalogos.tipos_directivos.codigo_tipo_directivo
    @Column(name = "codigo_tipo_directivo", length = 2, nullable = false)
    private String codigoTipoDirectivo;

    // '1' = Principal, '2' = Suplente
    @Column(name = "calidad_directivo", length = 1, nullable = false)
    private String calidadDirectivo;

    // '1' = Nombrado, '2' = Retirado, '3' = Excluido
    @Column(name = "estado_directivo", length = 1, nullable = false)
    private String estadoDirectivo;

    @Column(name = "acta_asamblea", length = 10, nullable = false)
    private String actaAsamblea;

    @Column(name = "fecha_asamblea")
    private LocalDate fechaAsamblea;

    @Column(name = "resolucion_ses", length = 10, nullable = false)
    private String resolucionSes;

    @Column(name = "fecha_resolucion")
    private LocalDate fechaResolucion;

    @Column(name = "fecha_retiro")
    private LocalDate fechaRetiro;

    @Column(name = "periodos_vigencia")
    private Integer periodosVigencia;

    // Auditoría (obligatoria según tu DDL)
    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime fechaActualizacion;
}
