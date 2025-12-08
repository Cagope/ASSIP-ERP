// src/app/features/seguridad/usuarios/usuarios.api.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Usuario } from './usuario.model';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UsuariosApi {

  private http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/seguridad/usuarios`;

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.base);
  }

  buscar(username: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.base}/${username}`);
  }

  // ⭐ NUEVO ⭐
  buscarPorId(id: number): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.base}/id/${id}`);
  }

  guardar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.base, usuario);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  listarAgencias(idUsuario: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/${idUsuario}/agencias`);
  }

  asignarAgencias(idUsuario: number, agencias: number[]): Observable<void> {
    return this.http.post<void>(`${this.base}/${idUsuario}/agencias`, agencias);
  }
}
