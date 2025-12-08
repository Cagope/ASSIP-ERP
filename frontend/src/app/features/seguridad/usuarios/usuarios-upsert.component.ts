// ============================================================
// src/app/features/seguridad/usuarios/usuarios-upsert.component.ts
// ARCHIVO CORREGIDO — PREVIENE idRol = null o 0
// ============================================================

import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UsuariosApi } from './usuarios.api';
import { Usuario } from './usuario.model';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { AgenciasApi, Agencia } from '../../general/agencias/agencia.api';
import { RolesApi } from '../roles/roles.api';

@Component({
  selector: 'app-usuarios-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],

  templateUrl: './usuarios-upsert.component.html',
  styleUrls: ['./usuarios-upsert.component.scss'],
})
export class UsuariosUpsertComponent implements OnInit {

  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private api = inject(UsuariosApi);
  private agenciasApi = inject(AgenciasApi);
  private rolesApi = inject(RolesApi);
  private router = inject(Router);

  idUsuario = signal<number | null>(null);
  cargando = signal<boolean>(false);

  agenciasDisponibles = signal<Agencia[]>([]);
  agenciasUsuario = signal<number[]>([]);
  rolesDisponibles = signal<any[]>([]);

  form = this.fb.group({
    username: ['', Validators.required],
    password: [''],
    nombreCompleto: ['', Validators.required],
    email: [''],
    activo: [true],
    idAgenciaPrincipal: [],
    idRol: [null, Validators.required]   // 🔥 obligatorio
  });

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');

    if (param && param !== 'nuevo') {
      this.idUsuario.set(Number(param));
    }

    this.cargarRoles();
    this.cargarAgenciasDisponibles();

    if (this.idUsuario()) {
      this.cargarUsuario(this.idUsuario()!);
    }
  }

  // ------------------------------------------------------------
  // Cargar roles
  // ------------------------------------------------------------
  cargarRoles() {
    this.rolesApi.listar().subscribe({
      next: (res) => this.rolesDisponibles.set(res),
      error: (err) => console.error("Error cargando roles:", err)
    });
  }

  // ------------------------------------------------------------
  // Cargar agencias
  // ------------------------------------------------------------
  cargarAgenciasDisponibles() {
    this.agenciasApi.listar().subscribe({
      next: (res) => this.agenciasDisponibles.set(res),
      error: () => console.error("Error cargando agencias")
    });
  }

  // ------------------------------------------------------------
  // Cargar usuario
  // ------------------------------------------------------------
  cargarUsuario(idUsuario: number) {
    this.cargando.set(true);

    this.api.buscarPorId(idUsuario).subscribe({
      next: (usu: any) => {

        const usuario = {
          idUsuario: usu.idUsuario ?? usu.idusuario,
          username: usu.username,
          nombreCompleto: usu.nombreCompleto ?? usu.nombrecompleto,
          email: usu.email,
          activo: usu.activo,
          idAgenciaPrincipal: usu.idAgenciaPrincipal ?? usu.idagenciaprincipal,
          idRol: usu.idRol ?? usu.idrol ?? null   // 🔥 PREVENCIÓN CRÍTICA
        };

        // Si el backend devolvió idRol = 0 → LO LIMPIAMOS
        if (usuario.idRol === 0) usuario.idRol = null;

        this.form.patchValue(usuario);

        this.cargarAgenciasUsuario(usuario.idUsuario);
      },
      complete: () => this.cargando.set(false)
    });
  }

  // ------------------------------------------------------------
  // Cargar agencias asignadas
  // ------------------------------------------------------------
  cargarAgenciasUsuario(idUsuario: number) {
    this.api.listarAgencias(idUsuario).subscribe(res => {
      this.agenciasUsuario.set(res.map(x => x.id_agencia));
    });
  }

  toggleAgencia(idAgencia: number, event: any) {
    const lista = [...this.agenciasUsuario()];
    const checked = event.target.checked;

    if (checked && !lista.includes(idAgencia)) lista.push(idAgencia);
    if (!checked) lista.splice(lista.indexOf(idAgencia), 1);

    this.agenciasUsuario.set(lista);
  }

  // ------------------------------------------------------------
  // Guardar
  // ------------------------------------------------------------
  guardar() {
    if (this.form.invalid) {
      alert("Debe completar todos los campos requeridos, incluido el rol.");
      return;
    }

    const data: Usuario = this.form.getRawValue();

    console.log("DATA ENVIADA:", data);

    // 🔥 PREVENIR QUE idRol QUEDÉ 0 O NULL
    if (!data.idRol || Number(data.idRol) === 0) {
      alert("Debe seleccionar un rol válido.");
      return;
    }

    data.idRol = Number(data.idRol);  // 🔥 Asegurar que viaja como número

    if (this.idUsuario()) {
      data.idUsuario = this.idUsuario()!;
    }

    this.api.guardar(data).subscribe(res => {
      if (this.agenciasUsuario().length > 0) {
        this.api.asignarAgencias(res.idUsuario!, this.agenciasUsuario())
          .subscribe(() => this.volver());
      } else {
        this.volver();
      }
    });
  }

  volver() {
    this.router.navigate(['/seguridad/usuarios']);
  }
}
