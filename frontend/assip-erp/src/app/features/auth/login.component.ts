import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../core/auth/auth.service';
import { SessionService } from '../../core/auth/session.service';

const LS_USER_KEY  = 'assip_user';
const LS_PASS_KEY  = 'assip_pass';   // ⚠️ temporal

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private session = inject(SessionService);
  private router = inject(Router);

  loading = signal(false);
  errorMsg = signal<string | null>(null);
  showPassword = signal(false);
  remember = signal(false);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    try {
      const savedUser = localStorage.getItem(LS_USER_KEY);
      const savedPass = localStorage.getItem(LS_PASS_KEY);
      if (savedUser) { this.form.patchValue({ username: savedUser }); this.remember.set(true); }
      if (savedPass) { this.form.patchValue({ password: savedPass }); this.remember.set(true); }
    } catch {}
  }

  toggleShowPassword() { this.showPassword.update(v => !v); }

  onRememberChange(checked: boolean) {
    this.remember.set(checked);
    if (!checked) {
      try { localStorage.removeItem(LS_USER_KEY); localStorage.removeItem(LS_PASS_KEY); } catch {}
    }
  }

  private persistRememberIfNeeded() {
    try {
      if (this.remember()) {
        localStorage.setItem(LS_USER_KEY, this.f.username.value);
		localStorage.setItem(LS_PASS_KEY, this.f.password.value);
      } else {
        localStorage.removeItem(LS_USER_KEY);
        localStorage.removeItem(LS_PASS_KEY);
      }
    } catch {}
  }

  submit() {
    console.log('[LOGIN] submit() llamado');
    this.errorMsg.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      console.warn('[LOGIN] Form inválido', this.form.value);
      return;
    }

    this.loading.set(true);
    console.log('[LOGIN] intentando POST /auth/login ...');

    this.auth.login({
      username: this.f.username.value,
      password: this.f.password.value,
    }).subscribe({
      next: (res: { token: string }) => {
        console.log('[LOGIN] OK, token recibido');
        this.session.setToken(res.token);
        this.persistRememberIfNeeded();

        // 1) Cargar /me y SOLO después navegar al hijo concreto del layout
        this.session.loadMe().subscribe({
          next: (me) => {
            this.loading.set(false);
            this.session.setMeLocal(me);
            console.log('[LOGIN] /me OK', me);

            if (me?.debeCambiarPassword) {
              this.router.navigate(['/auth/cambiar-password']);
            } else {
              this.router.navigateByUrl('/'); // ⬅️ ir directo al hijo
              console.log('[LOGIN] navegando a /general/agencias ...');
            }
          },
          error: (e: any) => {
            this.loading.set(false);
            console.warn('[LOGIN] /me ERROR', e);
            // Si /me falla, intentamos igual al hijo (el guard al menos verá token)
            this.router.navigateByUrl('/');
          }
        });
      },
      error: (e: HttpErrorResponse) => {
        this.loading.set(false);
        const apiMsg =
          (e.error?.errors?.[0]?.message as string | undefined) ??
          (e.error?.message as string | undefined) ??
          (e.status === 0 ? 'No hay conexión con el servidor.' :
           e.status === 401 ? 'Credenciales inválidas.' :
           'Error al iniciar sesión.');
        this.errorMsg.set(apiMsg);
        console.error('[LOGIN] ERROR', e);
      }
    });
  }
}
