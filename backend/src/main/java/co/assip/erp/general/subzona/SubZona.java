package co.assip.erp.general.subzona;

import co.assip.erp.general.zona.Zona;
import co.assip.erp.seguridad.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sub_zonas", schema = "general")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SubZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sub_zona")
    private Integer idSubZona;

    // 🔗 Relación con Zona
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona", nullable = false)
    private Zona zona;

    @Column(name = "codigo_sub_zona", length = 3, nullable = false)
    private String codigoSubZona;

    @Column(name = "nombre_sub_zona", length = 100, nullable = false)
    private String nombreSubZona;

    @Column(name = "comentario_sub_zona", length = 100, nullable = false)
    private String comentarioSubZona;

    // 🧾 Auditoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_seguridad_creacion", nullable = false)
    private Usuario usuarioCreacion;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_seguridad_edicion", nullable = false)
    private Usuario usuarioEdicion;

    @UpdateTimestamp
    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;
}
