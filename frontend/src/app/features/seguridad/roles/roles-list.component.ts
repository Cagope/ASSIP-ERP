import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { finalize, forkJoin } from 'rxjs';

import { RolesApi } from './roles.api';
import { Rol } from './roles.models';

import { PermisosApi } from '../permisos/permisos.api';
import { Permiso } from '../permisos/permisos.models';

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './roles-list.component.html',
  styleUrl: './roles-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RolesListComponent implements OnInit {

  private readonly rolesApi = inject(RolesApi);
  private readonly permisosApi = inject(PermisosApi);

  readonly roles = signal<Rol[]>([]);
  readonly permisos = signal<Permiso[]>([]);

  readonly rolSeleccionado = signal<Rol | null>(null);

  readonly idsPermisosSeleccionados =
    signal<Set<number>>(new Set<number>());

  readonly cargando = signal(false);
  readonly cargandoPermisos = signal(false);
  readonly guardando = signal(false);

  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);

  readonly cantidadPermisosSeleccionados = computed(
    () => this.idsPermisosSeleccionados().size
  );

  readonly hayRolSeleccionado = computed(
    () => this.rolSeleccionado() !== null
  );

  ngOnInit(): void {
    this.cargarDatos();
  }

  // =========================================================
  // CARGA INICIAL
  // =========================================================

  cargarDatos(): void {

    this.cargando.set(true);
    this.error.set(null);
    this.mensaje.set(null);

    forkJoin({
      roles: this.rolesApi.listar(),
      permisos: this.permisosApi.listar()
    })
      .pipe(
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: ({ roles, permisos }) => {

          this.roles.set(
            [...roles].sort((a, b) =>
              a.nombreRol.localeCompare(b.nombreRol)
            )
          );

          this.permisos.set(
            [...permisos]
              .filter(permiso => permiso.activo !== false)
              .sort((a, b) =>
                a.codigo.localeCompare(b.codigo)
              )
          );
        },
        error: () => {
          this.error.set(
            'No fue posible cargar los roles y permisos.'
          );
        }
      });
  }

  // =========================================================
  // SELECCIÓN DEL ROL
  // =========================================================

  seleccionarRol(rol: Rol): void {

    if (!rol.idRol) {
      return;
    }

    this.rolSeleccionado.set(rol);
    this.idsPermisosSeleccionados.set(new Set<number>());

    this.error.set(null);
    this.mensaje.set(null);

    this.cargarPermisosRol(rol.idRol);
  }

  private cargarPermisosRol(idRol: number): void {

    this.cargandoPermisos.set(true);

    this.rolesApi
      .listarPermisosRol(idRol)
      .pipe(
        finalize(() =>
          this.cargandoPermisos.set(false)
        )
      )
      .subscribe({
        next: permisosRol => {

          this.idsPermisosSeleccionados.set(
            new Set(
              permisosRol.map(
                permiso => permiso.idPermiso
              )
            )
          );
        },
        error: () => {
          this.error.set(
            'No fue posible consultar los permisos del rol.'
          );
        }
      });
  }

  // =========================================================
  // PERMISOS
  // =========================================================

  permisoSeleccionado(idPermiso: number): boolean {
    return this.idsPermisosSeleccionados()
      .has(idPermiso);
  }

  cambiarPermiso(
    idPermiso: number,
    seleccionado: boolean
  ): void {

    const nuevos =
      new Set(this.idsPermisosSeleccionados());

    if (seleccionado) {
      nuevos.add(idPermiso);
    } else {
      nuevos.delete(idPermiso);
    }

    this.idsPermisosSeleccionados.set(nuevos);

    this.mensaje.set(null);
  }

  // =========================================================
  // GUARDAR
  // =========================================================

  guardarPermisos(): void {

    const rol = this.rolSeleccionado();

    if (!rol) {
      return;
    }

    const idsPermisos =
      Array.from(
        this.idsPermisosSeleccionados()
      ).sort((a, b) => a - b);

    this.guardando.set(true);
    this.error.set(null);
    this.mensaje.set(null);

    this.rolesApi
      .actualizarPermisosRol(
        rol.idRol,
        idsPermisos
      )
      .pipe(
        finalize(() =>
          this.guardando.set(false)
        )
      )
      .subscribe({
        next: () => {
          this.mensaje.set(
            'Los permisos del rol fueron actualizados correctamente.'
          );
        },
        error: () => {
          this.error.set(
            'No fue posible actualizar los permisos del rol.'
          );
        }
      });
  }

  // =========================================================
  // APOYO VISUAL
  // =========================================================

  esRolSeleccionado(rol: Rol): boolean {
    return this.rolSeleccionado()?.idRol === rol.idRol;
  }

  trackRol(
    _index: number,
    rol: Rol
  ): number {
    return rol.idRol;
  }

  trackPermiso(
    _index: number,
    permiso: Permiso
  ): number {
    return permiso.idPermiso;
  }
}
