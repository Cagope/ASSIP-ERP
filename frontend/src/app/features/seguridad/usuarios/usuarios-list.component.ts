// src/app/features/seguridad/usuarios/usuarios-list.component.ts

import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  UsuariosApi
} from './usuarios.api';

import {
  Usuario
} from './usuarios.models';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

@Component({
  selector: 'app-usuarios-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderActionsComponent
  ],
  templateUrl: './usuarios-list.component.html',
  styleUrls: ['./usuarios-list.component.scss'],
})
export class UsuariosListComponent implements OnInit {

  private readonly api =
    inject(UsuariosApi);

  private readonly router =
    inject(Router);

  usuarios =
    signal<Usuario[]>([]);

  cargando =
    signal<boolean>(false);

  ngOnInit(): void {
    this.cargar();
  }

  /**
   * Carga el listado administrativo de usuarios.
   */
  cargar(): void {

    this.cargando.set(true);

    this.api.listar().subscribe({

      next: usuarios => {

        this.usuarios.set(
          usuarios
        );

        this.cargando.set(false);
      },

      error: err => {

        console.error(
          'Error cargando usuarios:',
          err
        );

        this.cargando.set(false);
      }
    });
  }

  /**
   * Abre la creación de un usuario.
   */
  nuevo(): void {

    this.router.navigate([
      '/seguridad/usuarios/nuevo'
    ]);
  }

  /**
   * Abre la edición del usuario seleccionado.
   */
  editar(
    idUsuario: number | undefined
  ): void {

    if (idUsuario == null) {

      console.error(
        'El usuario no tiene identificador.'
      );

      return;
    }

    this.router.navigate([
      '/seguridad/usuarios',
      idUsuario
    ]);
  }

  /**
   * Construye el texto de roles que se muestra
   * en el listado.
   */
  nombresRoles(
    usuario: Usuario
  ): string {

    if (!usuario.roles?.length) {
      return 'Sin rol';
    }

    return usuario.roles
      .map(rol => rol.nombreRol)
      .join(', ');
  }
}
