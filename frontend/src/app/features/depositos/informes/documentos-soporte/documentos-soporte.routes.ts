import { Routes } from '@angular/router';

// 📊 Componente standalone del informe
import { DocumentosSoporteListComponent }
  from './documentos-soporte-list.component';

// 🔐 Seguridad
import { permisoGuard } from '../../../../core/auth/permiso.guard';

/**
 * 📌 Rutas — Informe documentos soporte
 * ------------------------------------------------------------
 * Consulta general de documentos soporte
 * por agencia y rango de fechas.
 */
export const DOCUMENTOS_SOPORTE_INFORME_ROUTES: Routes = [
  {
    path: '',
    component: DocumentosSoporteListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'DEPOSITOS_INFORMES_VIEW'
    }
  }
];
