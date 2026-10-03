import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  AuthApi
} from '../auth.api';

import {
  SessionService
} from '../session.service';

@Component({
  selector: 'app-cambiar-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './cambiar-password.component.html',
  styleUrls: ['./cambiar-password.component.scss']
})
export class CambiarPasswordComponent {

  private readonly fb =
    inject(FormBuilder);

  private readonly authApi =
    inject(AuthApi);

  private readonly session =
    inject(SessionService);

  private readonly router =
    inject(Router);

  loading = signal(false);

  errorMsg = signal<string | null>(null);

  showActual = signal(false);

  showNueva = signal(false);

  showConfirmacion = signal(false);

  form = this.fb.nonNullable.group({

    passwordActual: [
      '',
      Validators.required
    ],

    passwordNueva: [
      '',
      Validators.required
    ],

    confirmacion: [
      '',
      Validators.required
    ]

  });

  guardar(): void {

    if (this.loading()) {
      return;
    }

    this.errorMsg.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const {
      passwordActual,
      passwordNueva,
      confirmacion
    } = this.form.getRawValue();

    if (passwordNueva !== confirmacion) {

      this.errorMsg.set(
        'La confirmación de la nueva contraseña no coincide.'
      );

      return;
    }

    if (passwordActual === passwordNueva) {

      this.errorMsg.set(
        'La nueva contraseña debe ser diferente de la actual.'
      );

      return;
    }

    this.loading.set(true);

    // =====================================================
    // CAMBIO VOLUNTARIO DE CONTRASEÑA
    // =====================================================

    this.authApi.cambiarPassword(
      passwordActual,
      passwordNueva
    ).subscribe({

      next: () => {

        this.loading.set(false);

        // Eliminar las contraseñas del formulario.
        this.form.reset();

        // Cerrar la sesión actual.
        this.session.logout();

        // Solicitar un nuevo inicio de sesión.
        this.router.navigate([
          '/login'
        ]);

      },

      error: err => {

        this.loading.set(false);

        this.errorMsg.set(
          err?.error?.error
          ?? err?.error?.message
          ?? 'No fue posible cambiar la contraseña.'
        );

      }

    });
  }

  // =========================================================
  // CANCELAR CAMBIO DE CONTRASEÑA
  // =========================================================

  cancelar(): void {

    if (this.loading()) {
      return;
    }

    this.form.reset();

    this.session.logout();

    this.router.navigate([
      '/login'
    ]);
  }

}
