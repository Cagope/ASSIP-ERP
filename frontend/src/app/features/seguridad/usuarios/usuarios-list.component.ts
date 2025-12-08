// src/app/features/seguridad/usuarios/usuarios-list.component.ts

import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { UsuariosApi } from './usuarios.api';
import { Usuario } from './usuario.model';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';

@Component({
  selector: 'app-usuarios-list',
  standalone: true,
  imports: [CommonModule, HeaderActionsComponent],
  templateUrl: './usuarios-list.component.html',
  styleUrls: ['./usuarios-list.component.scss'],
})
export class UsuariosListComponent implements OnInit {

  private api = inject(UsuariosApi);
  private router = inject(Router);

  usuarios = signal<Usuario[]>([]);
  cargando = signal<boolean>(false);

  ngOnInit(): void {
    this.cargar();
  }

  cargar() {
    this.cargando.set(true);

    this.api.listar().subscribe({
      next: (res: any[]) => {

        // 🔥 Normalizar respuesta SIN tocar backend
        const normalizados = res.map(u => ({
          idUsuario: u.idUsuario ?? u.idusuario ?? null,
          username: u.username,
          nombreCompleto: u.nombreCompleto ?? u.nombrecompleto,
          email: u.email,
          activo: u.activo,
          idRol: u.idRol ?? u.idrol,
          nombreRol: u.nombreRol ?? u.nombrerol,
          idAgenciaPrincipal: u.idAgenciaPrincipal ?? u.idagenciaprincipal
        }));

        this.usuarios.set(normalizados);
        this.cargando.set(false);
      },
      error: err => {
        console.error('Error cargando usuarios:', err);
        this.cargando.set(false);
      }
    });
  }

  nuevo() {
    this.router.navigate(['/seguridad/usuarios/nuevo']);
  }

  editar(idUsuario: number | null) {
    if (!idUsuario) {
      console.error('❌ idUsuario viene NULL o undefined');
      return;
    }
    this.router.navigate(['/seguridad/usuarios', idUsuario]);
  }
}
