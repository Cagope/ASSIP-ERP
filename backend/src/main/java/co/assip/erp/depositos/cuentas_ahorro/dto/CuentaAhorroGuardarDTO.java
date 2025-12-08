package co.assip.erp.depositos.cuentas_ahorro.dto;

import lombok.Data;
import java.util.List;

@Data
public class CuentaAhorroGuardarDTO {

    private Integer idAgencia;
    private Integer idFormaAhorro;
    private Integer idDatosPersonal;

    private String cuentaConjunta; // N / Y / O
    private String accionConjunta; // solo si Y/O
    private String gmfCuentaCuenta;
    private Boolean retencionFuenteCuenta;
    private Integer plazoCuenta;
    private Double cuotaMensualCuenta;
    private Double tasa;

    private List<BeneficiarioDTO> beneficiarios;
    private List<PoderDTO> poderes;

    private Integer usuarioId; // para auditoría
}
