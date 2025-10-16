import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ZonasApi, Zona } from './zonas.api';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-zonas-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './zonas-list.component.html',
  styleUrls: ['./zonas-list.component.scss']
})
export class ZonasListComponent implements OnInit {
  private api = inject(ZonasApi);
  private router = inject(Router);

  q = signal<string>('');
  loading = signal<boolean>(false);
  zonas = signal<Zona[]>([]);
  page = signal<number>(0);
  size = signal<number>(20);
  total = signal<number>(0);

  ngOnInit() {
    this.load();
  }

  load(page: number = 0) {
    this.loading.set(true);
    this.page.set(page);
    this.api.list({ q: this.q(), page: this.page(), size: this.size(), sort: 'nombreZona,asc' })
      .subscribe({
        next: (res) => {
          this.zonas.set(res.content);
          this.total.set(res.totalElements);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
  }

  buscar() {
    this.load(0);
  }

  editar(id: number) {
    this.router.navigate(['/general/zonas', id, 'edit']);
  }

  eliminar(id: number) {
    if (!confirm('¿Eliminar esta zona?')) return;
    this.api.delete(id).subscribe({
      next: () => this.load(this.page()),
      error: (err) => alert(err?.error?.message ?? 'Error al eliminar')
    });
  }

  // === Ir a la previsualización/impresión sin layout ===
  imprimir() {
    this.router.navigate(['/general/zonas/print'], {
      queryParams: { q: this.q() || null }
    });
  }
}
