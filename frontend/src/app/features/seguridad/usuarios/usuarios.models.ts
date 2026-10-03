// src/app/features/seguridad/usuarios/usuarios.models.ts

/**
 * Rol vigente asignado a un usuario.
 */
export interface UsuarioRol {
  idRol: number;
  nombreRol: string;
}


/**
 * Usuario utilizado por la administración de seguridad.
 *
 * Un usuario puede:
 * - estar vinculado con una persona de Hoja de Vida;
 * - tener múltiples roles;
 * - operar sobre múltiples agencias;
 * - tener una agencia principal;
 * - tener una vigencia administrativa.
 */
export interface Usuario {

  idUsuario?: number;

  // =========================================================
  // VÍNCULO CON HOJA DE VIDA
  // =========================================================

  idDatosPersonal: number | null;


  // =========================================================
  // IDENTIFICACIÓN Y ACCESO
  // =========================================================

  username: string | null;

  // =========================================================
  // INFORMACIÓN GENERAL
  // =========================================================

  nombreCompleto: string | null;

  email: string | null;

  activo: boolean | null;

  /**
   * Indica si el usuario todavía debe
   * establecer su contraseña inicial.
   *
   * Es un campo informativo del backend.
   */
  pendienteActivacion?: boolean | null;

  // =========================================================
  // VIGENCIA
  // =========================================================

  fechaInicioVigencia: string | null;

  fechaFinVigencia: string | null;


  // =========================================================
  // ROLES
  // =========================================================

  roles?: UsuarioRol[];


  // =========================================================
  // AGENCIAS
  // =========================================================

  idAgenciaPrincipal: number | null;

  /**
   * Identificadores de las agencias autorizadas.
   *
   * Este campo es utilizado por el frontend para administrar
   * la relación usuario-agencia.
   */
  agencias?: number[];
}
