import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-cambiar-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cambiar-password.component.html',
  styleUrls: ['./cambiar-password.component.scss']
})
export class CambiarPasswordComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  errorMsg = signal<string | null>(null);
  okMsg = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    passwordActual: ['', [Validators.required, Validators.minLength(4)]],
    passwordNueva: ['', [Validators.required, Validators.minLength(8)]],
    passwordConfirm: ['', [Validators.required, Validators.minLength(8)]],
  });

  get f() { return this.form.controls; }

  submit() {
    this.errorMsg.set(null);
    this.okMsg.set(null);

    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (this.f.passwordNueva.value !== this.f.passwordConfirm.value) {
      this.errorMsg.set('La confirmación no coincide.');
      return;
    }

    this.loading.set(true);
    this.auth.cambiarPassword({
      passwordActual: this.f.passwordActual.value,
      passwordNueva: this.f.passwordNueva.value
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.okMsg.set('Contraseña cambiada con éxito.');
        // Opcional: redirigir
        // this.router.navigateByUrl('/seguridad/usuarios');
      },
      error: (e: HttpErrorResponse) => {
        this.loading.set(false);
        const apiMsg =
          (e.error?.errors?.[0]?.message as string | undefined) ??
          (e.error?.message as string | undefined);
        this.errorMsg.set(apiMsg ?? 'No fue posible cambiar la contraseña.');
      }
    });
  }
}
