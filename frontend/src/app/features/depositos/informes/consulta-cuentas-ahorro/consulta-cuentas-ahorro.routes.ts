import { Routes } from '@angular/router';

// ✔ Tus componentes reales (NO inventados)
import { CuentasAhorroListComponent }
  from './consulta-cuentas-ahorro-list.component';

import { CuentasAhorroDetalleComponent }
  from './consulta-cuentas-ahorro-detalle.component';

export const CONSULTA_CUENTAS_AHORRO_ROUTES: Routes = [
  { path: '', component: CuentasAhorroListComponent },
  { path: ':id/detalle', component: CuentasAhorroDetalleComponent }
];
