package co.assip.erp.general.parametro;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 📘 Entidad: Parametro
 * ------------------------------------------------------------
 * Representa los parámetros configurables del sistema,
 * asociados a una agencia específica. Cada registro mantiene
 * su código único dentro de la agencia.
 */
@Entity
@Table(name = "parametros", schema = "general",
        uniqueConstraints = @UniqueConstraint(name = "uk_parametros", columnNames = {"id_agencia", "codigo_parametro"}))
@Getter
@Setter
public class Parametro extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Integer idParametro;

    @Column(name = "id_agencia", nullable = false)
    private Integer idAgencia; // FK con general.datos_agencias

    @Column(name = "codigo_parametro", nullable = false)
    private Integer codigoParametro;

    @Column(name = "nombre_parametro", length = 100, nullable = false)
    private String nombreParametro;

    @Column(name = "valor_parametro", precision = 18, scale = 2)
    private BigDecimal valorParametro = BigDecimal.ZERO;

    @Column(name = "tipo_valor", nullable = false)
    private Boolean tipoValor;
}
