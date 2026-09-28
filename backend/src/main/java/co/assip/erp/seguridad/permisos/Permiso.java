package co.assip.erp.seguridad.permisos;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa una capacidad funcional disponible en el ERP.
 *
 * Cada permiso se identifica mediante un código único, por ejemplo:
 * ROLES_VIEW, ROLES_EDIT o HOJAVIDA_VIEW.
 *
 * La asignación de permisos a roles no se almacena en esta entidad;
 * se administra mediante la relación seguridad.rol_permisos.
 */
@Entity
@Table(name = "permisos", schema = "seguridad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    /**
     * Identificador único del permiso.
     */
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "permisos_id_permiso_seq"
    )
    @SequenceGenerator(
            name = "permisos_id_permiso_seq",
            sequenceName = "seguridad.permisos_id_permiso_seq",
            allocationSize = 1
    )
    @Column(name = "id_permiso")
    private Integer idPermiso;

    /**
     * Código único utilizado por backend y frontend
     * para verificar una capacidad funcional.
     */
    @Column(
            name = "codigo",
            nullable = false,
            unique = true,
            length = 100
    )
    private String codigo;

    /**
     * Descripción funcional del permiso.
     */
    @Column(name = "descripcion")
    private String descripcion;

    /**
     * Indica si el permiso se encuentra disponible para uso.
     *
     * Un permiso inactivo se conserva para mantener la trazabilidad
     * de las configuraciones históricas de seguridad.
     */
    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}