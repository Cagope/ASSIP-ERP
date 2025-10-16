import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { SubZonasApi, SubZona } from './sub-zonas.api';
import { ZonasApi, Zona } from '../zonas/zonas.api';

@Component({
  selector: 'app-sub-zonas-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './sub-zonas-list.component.html',
  styleUrls: ['./sub-zonas-list.component.scss']
})
export class SubZonasListComponent implements OnInit {
  private api = inject(SubZonasApi);
  private zonasApi = inject(ZonasApi);
  private router = inject(Router);

  q = signal<string>('');
  loading = signal<boolean>(false);
  subZonas = signal<SubZona[]>([]);
  zonas = signal<Zona[]>([]);
  idZonaFilter: number | null = null;

  ngOnInit(): void {
    // cargar zonas para filtro
    this.zonasApi.list({ page: 0, size: 500, sort: 'nombreZona,asc' })
      .subscribe(res => this.zonas.set(res.content));
    this.load();
  }

  load(page: number = 0) {
    this.loading.set(true);
    this.api.list({ idZona: this.idZonaFilter ?? undefined, q: this.q(), page, size: 50, sort: 'nombreSubZona,asc' })
      .subscribe({
        next: (res) => {
          this.subZonas.set(res.content);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
  }

  buscar() {
    this.load(0);
  }

  editar(id: number) {
    this.router.navigate(['/general/sub-zonas', id, 'edit']);
  }

  eliminar(id: number) {
    if (!confirm('¿Eliminar esta sub zona?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(0),
      error: (err) => alert(err?.error?.message ?? 'Error al eliminar')
    });
  }

  imprimir() {
    this.router.navigate(['/general/sub-zonas/print'], {
      queryParams: { q: this.q() || null, idZona: this.idZonaFilter ?? null }
    });
  }
}
