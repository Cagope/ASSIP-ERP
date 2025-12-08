import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private tokenKey = 'assip_token';
  private userKey = 'assip_user';
  private agenciasKey = 'assip_agencias';
  private agenciaActivaKey = 'assip_agencia_activa';
  private permisosKey = 'assip_permisos';

  tokenSig = signal<string | null>(null);
  userSig = signal<string | null>(null);
  agenciasSig = signal<any[]>([]);
  agenciaActivaSig = signal<any | null>(null);
  permisosSig = signal<string[]>([]);

  constructor() {
    const storedToken = localStorage.getItem(this.tokenKey);
    const storedUser = localStorage.getItem(this.userKey);
    const storedAgencias = localStorage.getItem(this.agenciasKey);
    const storedAgenciaActiva = localStorage.getItem(this.agenciaActivaKey);
    const storedPermisos = localStorage.getItem(this.permisosKey);

    if (storedToken) this.tokenSig.set(storedToken);
    if (storedUser) this.userSig.set(storedUser);

    if (storedAgencias) this.agenciasSig.set(JSON.parse(storedAgencias));
    if (storedAgenciaActiva) this.agenciaActivaSig.set(JSON.parse(storedAgenciaActiva));
    if (storedPermisos) this.permisosSig.set(JSON.parse(storedPermisos));
  }

  // === TOKEN ===
  setToken(token: string) {
    localStorage.setItem(this.tokenKey, token);
    this.tokenSig.set(token);
  }

  getToken(): string | null {
    return this.tokenSig() || localStorage.getItem(this.tokenKey);
  }

  // === USUARIO ===
  setUser(username: string) {
    localStorage.setItem(this.userKey, username);
    this.userSig.set(username);
  }

  getUser(): string | null {
    return this.userSig() || localStorage.getItem(this.userKey);
  }

  // ⭐⭐⭐ AÑADIR ESTE MÉTODO ⭐⭐⭐
  esAdmin(): boolean {
    return this.getUser() === 'admin1';   // SUPERUSUARIO
  }

  // === PERMISOS ===
  setPermisos(permisos: string[]) {
    localStorage.setItem(this.permisosKey, JSON.stringify(permisos));
    this.permisosSig.set(permisos);
  }

  getPermisos(): string[] {
    return this.permisosSig() || [];
  }

  tienePermiso(codigo: string): boolean {
    return this.getPermisos().includes(codigo);
  }

  // === AGENCIAS ===
  setAgencias(agencias: any[]) {
    localStorage.setItem(this.agenciasKey, JSON.stringify(agencias));
    this.agenciasSig.set(agencias);

    if (!this.agenciaActivaSig()) {
      this.setAgenciaActiva(agencias[0] ?? null);
    }
  }

  getAgencias(): any[] {
    return this.agenciasSig() || [];
  }

  // === AGENCIA ACTIVA ===
  setAgenciaActiva(agencia: any | null) {
    if (agencia) {
      localStorage.setItem(this.agenciaActivaKey, JSON.stringify(agencia));
    } else {
      localStorage.removeItem(this.agenciaActivaKey);
    }
    this.agenciaActivaSig.set(agencia);
  }

  getAgenciaActiva() {
    return this.agenciaActivaSig();
  }

  // === SESIÓN ===
  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && token.trim() !== '';
  }

  // === LOGOUT ===
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem(this.agenciasKey);
    localStorage.removeItem(this.agenciaActivaKey);
    localStorage.removeItem(this.permisosKey);

    this.tokenSig.set(null);
    this.userSig.set(null);
    this.agenciasSig.set([]);
    this.agenciaActivaSig.set(null);
    this.permisosSig.set([]);
  }

  clear(): void {
    this.logout();
  }
}
