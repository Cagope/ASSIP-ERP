package co.assip.erp.sarlaft.domain;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 🧩 Entidad: ReglaSarlaft
 * ------------------------------------------------------------
 * Representa una regla configurable del sistema SARLAFT.
 * Estas reglas son evaluadas por el motor (AlertaEngine) y
 * pueden activar alertas de tipo ROJO, AMARILLO o VERDE.
 */
@Getter
@Setter
@Entity
@Table(name = "reglas_sarlaft", schema = "general")
public class ReglaSarlaft extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regla")
    private Long idRegla;

    /** Código único de la regla (ej: DATOS_DESACTUALIZADOS). */
    @Column(name = "codigo", nullable = false, length = 50, unique = true)
    private String codigo;

    /** Descripción detallada de la regla. */
    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** Valor numérico configurable (días, edad, límites, etc.). */
    @Column(name = "valor_numerico")
    private Double valorNumerico;

    /** Valor de texto configurable (códigos, parámetros especiales). */
    @Column(name = "valor_texto", length = 200)
    private String valorTexto;

    /** Severidad asignada por el Oficial (VERDE, AMARILLO, ROJO). */
    @Column(name = "severidad", nullable = false, length = 10)
    private String severidad;

    /** Módulo al que aplica la regla (opcional). */
    @Column(name = "codigo_modulo", length = 3)
    private String codigoModulo;

    /** Estado de la regla (activa / inactiva). */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

}
