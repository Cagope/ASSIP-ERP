package co.assip.erp.cajas.salida_cheques.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CajasSalidaChequesRequestDTO {

    private Long idCajaOrigen;
    private LocalDate fechaContable;
    private String numeroDocumento;
    private String observacion;

    @Builder.Default
    private List<Long> idsCheques = new ArrayList<>();
}