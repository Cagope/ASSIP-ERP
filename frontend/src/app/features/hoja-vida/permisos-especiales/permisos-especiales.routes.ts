import { Routes } from '@angular/router';
import { PermisosEspecialesListComponent } from './permisos-especiales-list.component';
import { PermisosEspecialesUpsertComponent } from './permisos-especiales-upsert.component';

/**
 * 📞 Rutas del submódulo Permisos Especiales
 * ------------------------------------------------------------
 * Módulo: Hoja de Vida → Permisos Especiales
 * Rutas:
 *  - Listado principal
 *  - Creación
 *  - Edición
 */
export const PERMISOS_ESPECIALES_ROUTES: Routes = [
  { path: '', component: PermisosEspecialesListComponent },
  { path: 'nuevo', component: PermisosEspecialesUpsertComponent },
  { path: ':id/editar', component: PermisosEspecialesUpsertComponent },
];
