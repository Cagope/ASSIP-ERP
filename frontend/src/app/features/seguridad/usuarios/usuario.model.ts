// src/app/features/seguridad/usuarios/usuario.model.ts

export interface Usuario {

  idUsuario?: number;

  username: string | null;
  password?: string | null;         // opcional al editar / crear

  nombreCompleto: string | null;
  email: string | null;

  activo: boolean | null;

  // Rol
  idRol: number | null;
  nombreRol?: string | null;        // ← OPCIONAL (solo viene del backend para mostrar)

  // Agencia principal
  idAgenciaPrincipal: number | null;

  // Agencias asignadas (para pantalla de agencias)
  agencias?: number[];
}
