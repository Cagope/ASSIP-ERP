import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { AfiliacionListComponent } from './afiliacion-list/afiliacion-list.component';
import { AfiliacionFormularioComponent } from './afiliacion-formulario/afiliacion-formulario.component';
import { TratamientoDatosComponent } from './tratamiento-datos/tratamiento-datos.component';
import { OrigenFondosComponent } from './origen-fondos/origen-fondos.component';
import { ActualizacionDatosComponent } from './actualizacion-datos/actualizacion-datos.component';

const PERMISO_FORMATOS = 'HOJAVIDA_FORMATOS_GENERATE';

export const AFILIACION_ROUTES: Routes = [
  {
    path: 'afiliacion-list',
    component: AfiliacionListComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: PERMISO_FORMATOS
    },
    title: 'Listado de Afiliaciones'
  },
  {
    path: 'afiliacion-formulario/:id',
    component: AfiliacionFormularioComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: PERMISO_FORMATOS
    },
    title: 'Formulario de Afiliación'
  },
  {
    path: 'tratamiento-datos/:id',
    component: TratamientoDatosComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: PERMISO_FORMATOS
    },
    title: 'Tratamiento de Datos Personales'
  },
  {
    path: 'origen-fondos/:id',
    component: OrigenFondosComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: PERMISO_FORMATOS
    },
    title: 'Declaración de Origen de Fondos'
  },
  {
    path: 'actualizacion-datos/:id',
    component: ActualizacionDatosComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: PERMISO_FORMATOS
    },
    title: 'Actualización de Datos Persona Naturales'
  },
  {
    path: 'exoneracion-gmf/:idPersona',
    canActivate: [permisoGuard],
    data: {
      permiso: PERMISO_FORMATOS
    },
    loadComponent: () =>
      import('./exoneracion-gmf/exoneracion-gmf.component')
        .then(c => c.ExoneracionGmfComponent),
    title: 'Carta de Exoneración GMF'
  }
];
