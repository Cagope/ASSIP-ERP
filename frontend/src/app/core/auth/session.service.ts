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

    if (storedAgencias) {
      // Normalizamos al cargar
      const arr = JSON.parse(storedAgencias).map((a: any) => this.normalizarAgencia(a));
      this.agenciasSig.set(arr);
    }

    if (storedAgenciaActiva) {
      const ag = this.normalizarAgencia(JSON.parse(storedAgenciaActiva));
      this.agenciaActivaSig.set(ag);
    }

    if (storedPermisos) this.permisosSig.set(JSON.parse(storedPermisos));
  }

  // ============================================================
  // 🔧 NORMALIZADOR: Convertir snake_case → camelCase
  // ============================================================
  private normalizarAgencia(a: any) {
    if (!a) return null;

    return {
      idAgencia: a.idAgencia ?? a.id_agencia ?? null,
      codigoAgencia: a.codigoAgencia ?? a.codigo_agencia ?? null,
      nombreAgencia: a.nombreAgencia ?? a.nombre_agencia ?? null
    };
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

  // === ADMIN (superusuario hardcoded) ===
  esAdmin(): boolean {
    return this.getUser() === 'admin1';
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

  // ============================================================
  // 🔹 AGENCIAS — SIEMPRE NORMALIZADAS
  // ============================================================
  setAgencias(agencias: any[]) {
    const normalizadas = agencias.map(a => this.normalizarAgencia(a));

    localStorage.setItem(this.agenciasKey, JSON.stringify(normalizadas));
    this.agenciasSig.set(normalizadas);

    if (!this.agenciaActivaSig()) {
      this.setAgenciaActiva(normalizadas[0] ?? null);
    }
  }

  getAgencias(): any[] {
    return this.agenciasSig() || [];
  }

  // ============================================================
  // 🔹 AGENCIA ACTIVA — SIEMPRE NORMALIZADA
  // ============================================================
  setAgenciaActiva(agencia: any | null) {
    const normal = this.normalizarAgencia(agencia);

    if (normal) {
      localStorage.setItem(this.agenciaActivaKey, JSON.stringify(normal));
    } else {
      localStorage.removeItem(this.agenciaActivaKey);
    }

    this.agenciaActivaSig.set(normal);
  }

  getAgenciaActiva() {
    const raw = this.agenciaActivaSig();
    return this.normalizarAgencia(raw);
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
