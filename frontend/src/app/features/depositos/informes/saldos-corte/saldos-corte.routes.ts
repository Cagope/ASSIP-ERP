import { Routes } from '@angular/router';

// 📊 Componente standalone del informe
import { SaldosCorteListComponent } from './saldos-corte-list.component';

/**
 * 📌 Rutas — Informe: Saldos a una fecha de corte
 * ------------------------------------------------------------
 * Pantalla simple: filtros, resumen y tabla de resultados.
 */
export const SALDOS_CORTE_ROUTES: Routes = [
  {
    path: '',
    component: SaldosCorteListComponent,
  }
];
