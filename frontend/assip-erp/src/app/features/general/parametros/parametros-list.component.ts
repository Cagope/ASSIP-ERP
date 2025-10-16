import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ParametrosApi, Parametro } from './parametros.api';
// NOTA: No importamos DatosAgencia (no existe en tu API). Usamos un tipo local mínimo:
import { AgenciasApi } from '../agencias/agencias.api';

type AgenciaLite = {
  idAgencia: number;
  nombreAgencia?: string | null;
  siglaAgencia?: string | null;
  codigoAgencia?: string | null;
};

@Component({
  selector: 'app-parametros-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './parametros-list.component.html',
  styleUrls: ['./parametros-list.component.scss']
})
export class ParametrosListComponent implements OnInit {
  private api = inject(ParametrosApi);
  private agenciasApi = inject(AgenciasApi);
  private router = inject(Router);

  q = signal<string>('');
  codigo = signal<number | null>(null);
  idAgenciaFilter: number | null = null;

  loading = signal<boolean>(false);
  parametros = signal<Parametro[]>([]);
  agencias = signal<AgenciaLite[]>([]);

  ngOnInit(): void {
    // Tu AgenciasApi.list espera probablemente un string (q) y devuelve un array.
    // Traemos un listado amplio para el combo (sin paginar).
    this.agenciasApi.list('')
      .subscribe((res: any) => {
        // res es un arreglo de agencias (no un Page)
        this.agencias.set(res as AgenciaLite[]);
      });

    this.load();
  }

  load(page: number = 0) {
    this.loading.set(true);
    this.api.list({
      idAgencia: this.idAgenciaFilter ?? undefined,
      q: this.q() || undefined,
      codigo: this.codigo() ?? undefined,
      page,
      size: 50,
      sort: 'nombreParametro,asc'
    }).subscribe({
      next: (res) => {
        this.parametros.set(res.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  buscar() { this.load(0); }

  editar(id: number) {
    this.router.navigate(['/general/parametros', id, 'edit']);
  }

  eliminar(id: number) {
    if (!confirm('¿Eliminar este parámetro?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(0),
      error: (err) => alert(err?.error?.message ?? 'Error al eliminar')
    });
  }

  imprimir() {
    this.router.navigate(['/general/parametros/print'], {
      queryParams: {
        idAgencia: this.idAgenciaFilter ?? null,
        q: this.q() || null,
        codigo: this.codigo() ?? null
      }
    });
  }
}
