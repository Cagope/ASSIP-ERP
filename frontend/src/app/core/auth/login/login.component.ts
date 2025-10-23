import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApi } from '../auth.api';
import { SessionService } from '../session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authApi = inject(AuthApi);
  private session = inject(SessionService);
  private router = inject(Router);

  loading = signal(false);
  errorMsg = signal<string | null>(null);
  showPassword = signal(false);
  remember = signal(false);

  form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  get f() {
    return this.form.controls;
  }

  constructor() {
    const saved = localStorage.getItem('dev_login_data');
    if (saved) {
      try {
        const { username, password } = JSON.parse(saved);
        this.form.patchValue({ username, password });
        this.remember.set(true);
      } catch {
        localStorage.removeItem('dev_login_data');
      }
    }
  }

  toggleShowPassword() {
    this.showPassword.update(v => !v);
  }

  onRememberChange(checked: boolean) {
    this.remember.set(checked);
    if (!checked) localStorage.removeItem('dev_login_data');
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    const { username, password } = this.form.getRawValue();

    if (this.remember()) {
      localStorage.setItem('dev_login_data', JSON.stringify({ username, password }));
    }

    this.authApi.login(username, password).subscribe({
      next: (result: any) => {
        this.session.setToken(result.token);
        this.session.setUser(result.username || username);
        this.loading.set(false);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.message || 'Error al iniciar sesión');
      },
    });
  }
}
