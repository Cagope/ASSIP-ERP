import {
  Injectable,
  signal
} from '@angular/core';

import {
  AuthAgencia,
  AuthRol
} from './auth.api';

export interface AgenciaSesion {
  idAgencia: number | null;
  codigoAgencia: string | null;
  nombreAgencia: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  private readonly tokenKey =
    'assip_token';

  private readonly userKey =
    'assip_user';

  private readonly rolesKey =
    'assip_roles';

  private readonly agenciasKey =
    'assip_agencias';

  private readonly agenciaActivaKey =
    'assip_agencia_activa';

  private readonly permisosKey =
    'assip_permisos';

  tokenSig =
    signal<string | null>(null);

  userSig =
    signal<string | null>(null);

  rolesSig =
    signal<AuthRol[]>([]);

  agenciasSig =
    signal<AgenciaSesion[]>([]);

  agenciaActivaSig =
    signal<AgenciaSesion | null>(null);

  permisosSig =
    signal<string[]>([]);

  constructor() {

    this.restaurarSesion();
  }

  /**
   * Restaura el contexto almacenado localmente.
   */
  private restaurarSesion(): void {

    const storedToken =
      localStorage.getItem(
        this.tokenKey
      );

    const storedUser =
      localStorage.getItem(
        this.userKey
      );

    const storedRoles =
      localStorage.getItem(
        this.rolesKey
      );

    const storedAgencias =
      localStorage.getItem(
        this.agenciasKey
      );

    const storedAgenciaActiva =
      localStorage.getItem(
        this.agenciaActivaKey
      );

    const storedPermisos =
      localStorage.getItem(
        this.permisosKey
      );

    if (storedToken) {
      this.tokenSig.set(storedToken);
    }

    if (storedUser) {
      this.userSig.set(storedUser);
    }

    if (storedRoles) {

      try {

        this.rolesSig.set(
          JSON.parse(storedRoles)
        );

      } catch {

        this.rolesSig.set([]);
      }
    }

    if (storedAgencias) {

      try {

        const agencias =
          JSON.parse(storedAgencias)
            .map((agencia: AuthAgencia) =>
              this.normalizarAgencia(
                agencia
              )
            );

        this.agenciasSig.set(
          agencias
        );

      } catch {

        this.agenciasSig.set([]);
      }
    }

    if (storedAgenciaActiva) {

      try {

        this.agenciaActivaSig.set(
          this.normalizarAgencia(
            JSON.parse(
              storedAgenciaActiva
            )
          )
        );

      } catch {

        this.agenciaActivaSig.set(null);
      }
    }

    if (storedPermisos) {

      try {

        this.permisosSig.set(
          JSON.parse(storedPermisos)
        );

      } catch {

        this.permisosSig.set([]);
      }
    }
  }

  /**
   * Normaliza las agencias recibidas del backend.
   *
   * Durante la transición admite snake_case
   * y camelCase.
   */
  private normalizarAgencia(
    agencia: AuthAgencia | AgenciaSesion | null
  ): AgenciaSesion {

    if (!agencia) {

      return {
        idAgencia: null,
        codigoAgencia: null,
        nombreAgencia: null
      };
    }

    const valor =
      agencia as AuthAgencia;

    return {
      idAgencia:
        valor.idAgencia
        ?? valor.id_agencia
        ?? null,

      codigoAgencia:
        valor.codigoAgencia
        ?? valor.codigo_agencia
        ?? null,

      nombreAgencia:
        valor.nombreAgencia
        ?? valor.nombre_agencia
        ?? null
    };
  }

  // ============================================================
  // TOKEN
  // ============================================================

  setToken(
    token: string
  ): void {

    localStorage.setItem(
      this.tokenKey,
      token
    );

    this.tokenSig.set(
      token
    );
  }

  getToken(): string | null {

    return this.tokenSig()
      ?? localStorage.getItem(
        this.tokenKey
      );
  }

  // ============================================================
  // USUARIO
  // ============================================================

  setUser(
    username: string
  ): void {

    localStorage.setItem(
      this.userKey,
      username
    );

    this.userSig.set(
      username
    );
  }

  getUser(): string | null {

    return this.userSig()
      ?? localStorage.getItem(
        this.userKey
      );
  }

  // ============================================================
  // ROLES
  // ============================================================

  setRoles(
    roles: AuthRol[]
  ): void {

    const normalizados =
      roles ?? [];

    localStorage.setItem(
      this.rolesKey,
      JSON.stringify(normalizados)
    );

    this.rolesSig.set(
      normalizados
    );
  }

  getRoles(): AuthRol[] {

    return this.rolesSig();
  }

  /**
   * Comprueba un rol por su nombre funcional.
   */
  tieneRol(
    nombreRol: string
  ): boolean {

    const buscado =
      nombreRol
        .trim()
        .toUpperCase();

    return this.getRoles()
      .some(
        rol =>
          rol.nombreRol
            ?.trim()
            .toUpperCase()
          === buscado
      );
  }

  /**
   * ADMIN se determina por el rol vigente,
   * nunca por el username.
   */
  esAdmin(): boolean {

    return this.tieneRol(
      'ADMIN'
    );
  }

  // ============================================================
  // PERMISOS
  // ============================================================

  setPermisos(
    permisos: string[]
  ): void {

    const normalizados =
      (permisos ?? [])
        .map(
          permiso =>
            permiso
              .trim()
              .toUpperCase()
        )
        .filter(
          permiso =>
            permiso.length > 0
        );

    localStorage.setItem(
      this.permisosKey,
      JSON.stringify(normalizados)
    );

    this.permisosSig.set(
      normalizados
    );
  }

  getPermisos(): string[] {

    return this.permisosSig();
  }

  /**
   * Verifica un permiso funcional.
   *
   * ADMIN conserva acceso total mientras completamos
   * la matriz definitiva de permisos del ERP.
   */
  tienePermiso(
    codigo: string
  ): boolean {

    if (this.esAdmin()) {
      return true;
    }

    const permiso =
      codigo
        .trim()
        .toUpperCase();

    return this.getPermisos()
      .includes(
        permiso
      );
  }

  // ============================================================
  // AGENCIAS
  // ============================================================

  setAgencias(
    agencias: AuthAgencia[]
  ): void {

    const normalizadas =
      (agencias ?? [])
        .map(
          agencia =>
            this.normalizarAgencia(
              agencia
            )
        );

    localStorage.setItem(
      this.agenciasKey,
      JSON.stringify(normalizadas)
    );

    this.agenciasSig.set(
      normalizadas
    );

    const agenciaActiva =
      this.agenciaActivaSig();

    /*
     * Si no existe agencia activa, selecciona
     * la primera disponible.
     */
    if (!agenciaActiva) {

      this.setAgenciaActiva(
        normalizadas[0] ?? null
      );

      return;
    }

    /*
     * Si la agencia activa dejó de pertenecer
     * al usuario, selecciona otra válida.
     */
    const sigueAutorizada =
      normalizadas.some(
        agencia =>
          agencia.idAgencia
          === agenciaActiva.idAgencia
      );

    if (!sigueAutorizada) {

      this.setAgenciaActiva(
        normalizadas[0] ?? null
      );
    }
  }

  getAgencias(): AgenciaSesion[] {

    return this.agenciasSig();
  }

  // ============================================================
  // AGENCIA ACTIVA
  // ============================================================

  setAgenciaActiva(
    agencia: AuthAgencia | AgenciaSesion | null
  ): void {

    if (!agencia) {

      localStorage.removeItem(
        this.agenciaActivaKey
      );

      this.agenciaActivaSig.set(
        null
      );

      return;
    }

    const normalizada =
      this.normalizarAgencia(
        agencia
      );

    localStorage.setItem(
      this.agenciaActivaKey,
      JSON.stringify(normalizada)
    );

    this.agenciaActivaSig.set(
      normalizada
    );
  }

  getAgenciaActiva(): AgenciaSesion | null {

    return this.agenciaActivaSig();
  }

  // ============================================================
  // SESIÓN
  // ============================================================

  isAuthenticated(): boolean {

    const token =
      this.getToken();

    return !!token
      && token.trim() !== '';
  }

  // ============================================================
  // LOGOUT
  // ============================================================

  logout(): void {

    localStorage.removeItem(
      this.tokenKey
    );

    localStorage.removeItem(
      this.userKey
    );

    localStorage.removeItem(
      this.rolesKey
    );

    localStorage.removeItem(
      this.agenciasKey
    );

    localStorage.removeItem(
      this.agenciaActivaKey
    );

    localStorage.removeItem(
      this.permisosKey
    );

    this.tokenSig.set(null);
    this.userSig.set(null);
    this.rolesSig.set([]);
    this.agenciasSig.set([]);
    this.agenciaActivaSig.set(null);
    this.permisosSig.set([]);
  }

  clear(): void {
    this.logout();
  }
}
