package co.assip.erp.gerencia.dashboard_depositos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DashboardDepositosRequestDTO {

    private LocalDate fechaCorte;

}