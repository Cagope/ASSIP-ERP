import { Routes } from '@angular/router';

import { ACTIVOS_ROUTES } from './activos/activos.routes';
import { LOCALIZACIONES_ROUTES } from './localizaciones/localizaciones.routes';
import { BLOQUES_ROUTES } from './bloques/bloques.routes';
import { IngresoActivosComponent } from './ingreso/ingreso-activos.component';

// 🔽 DEPRECIACIÓN (proceso)
import { depreciacionRoutes } from './depreciacion/depreciacion.routes';

// 🔽 INFORMES
import { maestroActivosRoutes } from './informes/maestro-activos/maestro-activos.routes';
import { movimientosActivosRoutes } from './informes/movimientos-activos/movimientos-activos.routes';
import { resumenMovimientosRoutes } from './informes/resumen-movimientos/resumen-movimientos.routes';
import { depreciacionInformeRoutes } from './informes/depreciacion/depreciacion-informe.routes';

// 🔽 NUEVO: KARDEX POR ACTIVO
import { kardexActivoRoutes } from './informes/kardex-activo/kardex-activo.routes';

export const ACTIVOS_FIJOS_ROUTES: Routes = [
  // ===============================
  // 🏷 CRUD PRINCIPAL
  // ===============================
  {
    path: 'activos',
    children: ACTIVOS_ROUTES
  },
  {
    path: 'ingreso',
    component: IngresoActivosComponent
  },
  {
    path: 'localizaciones',
    children: LOCALIZACIONES_ROUTES
  },
  {
    path: 'bloques',
    children: BLOQUES_ROUTES
  },

  // ===============================
  // ⚙️ PROCESOS
  // ===============================
  {
    path: 'depreciacion',
    children: depreciacionRoutes
  },

  // ===============================
  // 📊 INFORMES
  // ===============================
  {
    path: 'informes/maestro-activos',
    children: maestroActivosRoutes
  },
  {
    path: 'informes/movimientos-activos',
    children: movimientosActivosRoutes
  },
  {
    path: 'informes/resumen-movimientos',
    children: resumenMovimientosRoutes
  },
  {
    path: 'informes/depreciacion',
    children: depreciacionInformeRoutes
  },
  {
    path: 'informes/kardex-activo',
    children: kardexActivoRoutes
  }
];
