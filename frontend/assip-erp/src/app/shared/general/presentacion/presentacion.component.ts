// src/app/shared/general/presentacion/presentacion.component.ts
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SessionService } from '../../../core/auth/session.service';
import { EmpresasApi, EmpresaDTO } from '../empresas.api';

@Component({
  standalone: true,
  selector: 'app-presentacion',
  imports: [CommonModule],
  template: `
    <div class="wrap">
      <div class="hero">
        <img class="logo" [src]="logoUrl()" alt="Logo empresa" />
        <div class="titles">
          <h1>{{ empresaNombre() }}</h1>
          <div class="sub">
            <ng-container *ngIf="empresaNit()">NIT: {{ empresaNit() }}</ng-container>
            <ng-container *ngIf="empresaTelefono()"> · Tel: {{ empresaTelefono() }}</ng-container>
            <ng-container *ngIf="empresaDireccion()"> · {{ empresaDireccion() }}</ng-container>
          </div>
          <div class="user">Bienvenido(a): <b>{{ usuario() }}</b></div>
        </div>
      </div>

      <div class="hint">
        Selecciona una opción del menú lateral para comenzar.
      </div>
    </div>
  `,
  styles: [`
    .wrap { padding: 20px; }
    .hero { display:flex; align-items:center; gap:16px; background:#fff; border-radius:12px; padding:16px 20px; box-shadow:0 2px 10px rgba(0,0,0,.06); }
    .logo { width:80px; height:80px; object-fit:contain; border-radius:8px; background:#f8fafc; }
    .titles h1 { margin:0 0 6px; font-size:22px; line-height:1.2; }
    .sub { color:#555; margin-bottom:6px; }
    .user { color:#333; }
    .hint { margin-top:14px; color:#666; }
  `]
})
export class PresentacionComponent implements OnInit {
  private session = inject(SessionService);
  private empresasApi = inject(EmpresasApi);

  // Empresa
  empresaNombre = signal<string>('Empresa');
  empresaNit = signal<string>('');
  empresaTelefono = signal<string>('');
  empresaDireccion = signal<string>('');
  logoUrl = signal<string>('assets/LOGO_EMPRESA.png');

  // Usuario
  usuario = signal<string>('Usuario');

  ngOnInit(): void {
    // Usuario
    try {
      const me = this.session.me?.();
      this.usuario.set(this.resolveUserDisplay(me));
    } catch {
      this.usuario.set('Usuario');
    }

    // Empresa principal
    this.empresasApi.getEmpresaPrincipal().subscribe({
      next: (e: EmpresaDTO | null) => {
        if (!e) return;

        // Nombre visible: razón social o sigla
        const nombre = (e.razonSocial ?? e.siglaEmpresa ?? '').toString().trim() || 'Empresa';
        this.empresaNombre.set(nombre);

        // NIT = documento + DV (si aplica)
        const doc = (e.documentoEmpresa ?? '').toString().trim();
        const dv  = (e.digitoVerificacion ?? '').toString().trim();
        const nit = doc ? (dv ? `${doc}-${dv}` : doc) : '';
        if (nit) this.empresaNit.set(nit);

        const tel = (e.telefono ?? '').toString().trim();
        if (tel) this.empresaTelefono.set(tel);

        // Dirección: si no tienes un campo directo, deja vacío
        this.empresaDireccion.set('');

        const logo = (e.logoUrl ?? '').toString().trim();
        if (logo) this.logoUrl.set(logo);
      },
      error: () => { /* defaults */ }
    });
  }

  // === Helpers ===
  private resolveUserDisplay(me: any): string {
    const fullByNombres =
      `${me?.nombres ?? me?.primerNombre ?? ''} ${me?.apellidos ?? me?.primerApellido ?? ''}`.trim();

    const candidates = [
      me?.nombreCompleto,
      fullByNombres,
      me?.name,
      me?.username,
    ]
      .map(v => (typeof v === 'string' ? v.trim() : ''))
      .filter(Boolean);

    return candidates[0] || 'Usuario';
  }
}
