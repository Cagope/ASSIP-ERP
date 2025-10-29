package co.assip.erp.general.zona;

import co.assip.erp.general.subzona.SubZona;
import co.assip.erp.seguridad.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "zonas", schema = "general")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // ✅ evita error ByteBuddyInterceptor
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private Integer idZona;

    @Column(name = "codigo_zona", length = 3, nullable = false, unique = true)
    private String codigoZona;

    @Column(name = "nombre_zona", length = 100, nullable = false)
    private String nombreZona;

    @Column(name = "comentario_zona", length = 100)
    private String comentarioZona;

    // 🔗 Relación con SubZona
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("zona") // ✅ evita bucles JSON
    private List<SubZona> subZonas;

    // Auditoría
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
