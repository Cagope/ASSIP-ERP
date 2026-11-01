import { Routes } from '@angular/router';
import { permisoGuard } from '../../../../core/auth/permiso.guard';

/**
 * Rutas de los informes especiales de Contabilidad.
 * Cada informe tiene su propio componente y comparte el permiso funcional.
 */
export const INFORMES_ESPECIALES_ROUTES: Routes = [
  {
    path: 'libro-diario-resumido',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW' },
    loadComponent: () =>
      import('./libro-diario-resumido/libro-diario-resumido.component')
        .then(m => m.LibroDiarioResumidoComponent)
  },
  {
    path: 'secuencial-comprobantes',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW' },
    loadComponent: () =>
      import('./secuencial-comprobantes/secuencial-comprobantes.component')
        .then(m => m.SecuencialComprobantesComponent)
  },
  {
    path: 'resumen-diario-detallado',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW' },
    loadComponent: () =>
      import('./resumen-diario-detallado/resumen-diario-detallado.component')
        .then(m => m.ResumenDiarioDetalladoComponent)
  },
  {
    path: 'verificar-partida-doble',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW' },
    loadComponent: () =>
      import('./verificar-partida-doble/verificar-partida-doble.component')
        .then(m => m.VerificarPartidaDobleComponent)
  },
  {
    path: 'comprobante-resumen',
    canActivate: [permisoGuard],
    data: { permiso: 'CONTABILIDAD_INFORMES_ESPECIALES_VIEW' },
    loadComponent: () =>
      import('./comprobante-resumen/comprobante-resumen.component')
        .then(m => m.ComprobanteResumenComponent)
  }
];
