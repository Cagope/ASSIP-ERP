import { Routes } from '@angular/router';

import { permisoGuard } from '../../../core/auth/permiso.guard';

import { FormularioIntegralWizardComponent } from './formulario-integral-wizard.component';

/**
 * 🧾 Rutas del módulo Formulario Integral — Hoja de Vida
 * ------------------------------------------------------------
 * Flujo unificado para registrar o actualizar todos los datos
 * de una persona (Datos Personales, Ubicaciones, Laborales, etc.)
 */
export const FORMULARIO_INTEGRAL_ROUTES: Routes = [
  {
    path: '',
    component: FormularioIntegralWizardComponent,
    canActivate: [permisoGuard],
    data: {
      permiso: 'HOJAVIDA_FORMULARIO_INTEGRAL_EDIT'
    }
  }
];
