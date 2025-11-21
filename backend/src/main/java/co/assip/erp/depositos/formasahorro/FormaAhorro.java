package co.assip.erp.depositos.formasahorro;

import co.assip.erp.shared.domain.BaseAudit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "formas_ahorro", schema = "depositos")
public class FormaAhorro extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_forma_ahorro")
    private Integer id;

    @Column(name = "codigo_forma", length = 2, nullable = false, unique = true)
    private String codigoForma;

    @Column(name = "nombre_forma", length = 50, nullable = false)
    private String nombreForma;

    @Column(name = "consecutivo_forma")
    private Integer consecutivoForma;

    @Column(name = "tipo_captacion_forma", length = 1)
    private String tipoCaptacion;

    @Column(name = "tiempo_liquidacion")
    private Integer tiempoLiquidacion;

    @Column(name = "cuenta_forma_corto")
    private Integer cuentaFormaCorto;

    @Column(name = "cuenta_forma_largo")
    private Integer cuentaFormaLargo;

    @Column(name = "cuenta_gasto")
    private Integer cuentaGasto;

    @Column(name = "cuenta_cxp_forma")
    private Integer cuentaCxpForma;

    @Column(name = "cuenta_gmf_forma")
    private Integer cuentaGmfForma;

    @Column(name = "tipo_interes_forma")
    private Integer tipoInteresForma;

    @Column(name = "fecha_ultima_liquidacion")
    private java.sql.Date fechaUltimaLiquidacion;

    @Column(name = "autorizado_forma")
    private Boolean autorizadoForma;

    @Column(name = "documento_forma", length = 1)
    private String documentoForma;

    @Column(name = "periodo_gracia")
    private Integer periodoGracia;

    @Column(name = "valor_minimo")
    private Double valorMinimo;

    @Column(name = "tasa_interes_forma")
    private Double tasaInteresForma;
}
