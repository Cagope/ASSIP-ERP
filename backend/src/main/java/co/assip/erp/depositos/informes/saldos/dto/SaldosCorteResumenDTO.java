package co.assip.erp.depositos.informes.saldos.dto;

import lombok.Data;

@Data
public class SaldosCorteResumenDTO {

    private long totalCuentas;

    private double totalDebitos;
    private double totalCreditos;
    private double totalSaldos;

    private double saldoPromedio;

}