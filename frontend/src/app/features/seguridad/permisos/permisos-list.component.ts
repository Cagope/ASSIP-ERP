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
import { finalize } from 'rxjs';

import { PermisosApi } from './permisos.api';
import { Permiso } from './permisos.models';

@Component({
  selector: 'app-permisos-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './permisos-list.component.html',
  styleUrl: './permisos-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PermisosListComponent implements OnInit {

  private readonly permisosApi = inject(PermisosApi);

  readonly permisos = signal<Permiso[]>([]);

  readonly permisoSeleccionado = signal<Permiso | null>(null);

  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly cambiandoEstado = signal(false);

  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);

  readonly modoNuevo = signal(false);

  codigo = '';
  descripcion = '';

  readonly totalActivos = computed(
    () => this.permisos()
      .filter(permiso => permiso.activo)
      .length
  );

  ngOnInit(): void {
    this.cargarPermisos();
  }

  // =========================================================
  // CARGA
  // =========================================================

  cargarPermisos(): void {

    this.cargando.set(true);
    this.error.set(null);

    this.permisosApi
      .listar()
      .pipe(
        finalize(() => this.cargando.set(false))
      )
      .subscribe({
        next: permisos => {

          this.permisos.set(
            [...permisos].sort((a, b) =>
              a.codigo.localeCompare(b.codigo)
            )
          );
        },
        error: () => {
          this.error.set(
            'No fue posible consultar el catálogo de permisos.'
          );
        }
      });
  }

  // =========================================================
  // SELECCIÓN
  // =========================================================

  seleccionarPermiso(permiso: Permiso): void {

    this.modoNuevo.set(false);
    this.permisoSeleccionado.set(permiso);

    this.codigo = permiso.codigo;
    this.descripcion = permiso.descripcion ?? '';

    this.error.set(null);
    this.mensaje.set(null);
  }

  nuevoPermiso(): void {

    this.modoNuevo.set(true);
    this.permisoSeleccionado.set(null);

    this.codigo = '';
    this.descripcion = '';

    this.error.set(null);
    this.mensaje.set(null);
  }

  cancelar(): void {

    this.modoNuevo.set(false);
    this.permisoSeleccionado.set(null);

    this.codigo = '';
    this.descripcion = '';

    this.error.set(null);
    this.mensaje.set(null);
  }

  // =========================================================
  // GUARDAR
  // =========================================================

  guardar(): void {

    const codigo = this.codigo
      .trim()
      .toUpperCase();

    const descripcion =
      this.descripcion.trim();

    if (!codigo) {
      this.error.set(
        'El código del permiso es obligatorio.'
      );
      return;
    }

    this.error.set(null);
    this.mensaje.set(null);
    this.guardando.set(true);

    if (this.modoNuevo()) {

      const nuevo: Permiso = {
        idPermiso: 0,
        codigo,
        descripcion: descripcion || null,
        activo: true
      };

      this.permisosApi
        .crear(nuevo)
        .pipe(
          finalize(() => this.guardando.set(false))
        )
        .subscribe({
          next: permiso => {

            this.mensaje.set(
              'Permiso creado correctamente.'
            );

            this.modoNuevo.set(false);
            this.permisoSeleccionado.set(permiso);

            this.codigo = permiso.codigo;
            this.descripcion =
              permiso.descripcion ?? '';

            this.cargarPermisos();
          },
          error: err => {
            this.error.set(
              err?.error?.error
                ?? 'No fue posible crear el permiso.'
            );
          }
        });

      return;
    }

    const actual =
      this.permisoSeleccionado();

    if (!actual) {
      return;
    }

    const actualizado: Permiso = {
      ...actual,
      codigo,
      descripcion: descripcion || null
    };

    this.permisosApi
      .actualizar(
        actual.idPermiso,
        actualizado
      )
      .pipe(
        finalize(() => this.guardando.set(false))
      )
      .subscribe({
        next: permiso => {

          this.permisoSeleccionado.set(permiso);

          this.codigo = permiso.codigo;
          this.descripcion =
            permiso.descripcion ?? '';

          this.mensaje.set(
            'Permiso actualizado correctamente.'
          );

          this.cargarPermisos();
        },
        error: err => {
          this.error.set(
            err?.error?.error
              ?? 'No fue posible actualizar el permiso.'
          );
        }
      });
  }

  // =========================================================
  // ESTADO
  // =========================================================

  cambiarEstado(): void {

    const permiso =
      this.permisoSeleccionado();

    if (!permiso) {
      return;
    }

    const nuevoEstado = !permiso.activo;

    this.cambiandoEstado.set(true);
    this.error.set(null);
    this.mensaje.set(null);

    this.permisosApi
      .cambiarEstado(
        permiso.idPermiso,
        nuevoEstado
      )
      .pipe(
        finalize(() =>
          this.cambiandoEstado.set(false)
        )
      )
      .subscribe({
        next: actualizado => {

          this.permisoSeleccionado.set(
            actualizado
          );

          this.codigo = actualizado.codigo;
          this.descripcion =
            actualizado.descripcion ?? '';

          this.mensaje.set(
            nuevoEstado
              ? 'Permiso activado correctamente.'
              : 'Permiso inactivado correctamente.'
          );

          this.cargarPermisos();
        },
        error: err => {
          this.error.set(
            err?.error?.error
              ?? 'No fue posible cambiar el estado del permiso.'
          );
        }
      });
  }

  // =========================================================
  // APOYO
  // =========================================================

  esSeleccionado(permiso: Permiso): boolean {
    return this.permisoSeleccionado()
      ?.idPermiso === permiso.idPermiso;
  }
}
