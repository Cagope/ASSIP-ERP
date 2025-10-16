package co.assip.erp.general.domain;

import co.assip.erp.hojavida.domain.DatosPersonales;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "privilegiados", schema = "general")
public class Privilegiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_privilegiado")
    private Integer idPrivilegiado;

    // FK a general.directivos.id_directivo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_directivo", nullable = false)
    private Directivo directivo;

    // FK a hoja_vida.datos_personales.id_datos_personal
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_datos_personal", nullable = false)
    private DatosPersonales persona;

    // FK a catalogos.parentescos.codigo_parentesco (por código)
    @Column(name = "codigo_parentesco", length = 2, nullable = false)
    private String codigoParentesco;

    // Auditoría
    @Column(name = "fk_seguridad_creacion", nullable = false)
    private Integer fkSeguridadCreacion;

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime fechaCreacion;

    @Column(name = "fk_seguridad_actualizacion", nullable = false)
    private Integer fkSeguridadActualizacion;

    @Column(name = "fecha_actualizacion", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime fechaActualizacion;
}
