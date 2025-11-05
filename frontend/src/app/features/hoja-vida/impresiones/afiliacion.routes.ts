import { Routes } from '@angular/router';
import { AfiliacionListComponent } from './afiliacion-list/afiliacion-list.component';
import { AfiliacionFormularioComponent } from './afiliacion-formulario/afiliacion-formulario.component';
import { TratamientoDatosComponent } from './tratamiento-datos/tratamiento-datos.component';
import { OrigenFondosComponent } from './origen-fondos/origen-fondos.component';
import { ActualizacionDatosComponent } from './actualizacion-datos/actualizacion-datos.component'; // ✅ Nuevo

export const AFILIACION_ROUTES: Routes = [
  {
    path: 'afiliacion-list',
    component: AfiliacionListComponent,
    title: 'Listado de Afiliaciones'
  },
  {
    path: 'afiliacion-formulario/:id',
    component: AfiliacionFormularioComponent,
    title: 'Formulario de Afiliación'
  },
  {
    path: 'tratamiento-datos/:id',
    component: TratamientoDatosComponent,
    title: 'Tratamiento de Datos Personales'
  },
  {
    path: 'origen-fondos/:id',
    component: OrigenFondosComponent,
    title: 'Declaración de Origen de Fondos'
  },
  {
    path: 'actualizacion-datos/:id', // ✅ Nuevo formato
    component: ActualizacionDatosComponent,
    title: 'Actualización de Datos Persona Naturales'
  }
];
