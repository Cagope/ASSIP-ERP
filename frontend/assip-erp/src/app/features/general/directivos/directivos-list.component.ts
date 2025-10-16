import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DirectivosApi, DirectivoListItemDTO } from './directivos.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';
import { DirectivosExportButtonComponent } from './directivos-export-button.component';

@Component({
  selector: 'app-directivos-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DirectivosExportButtonComponent],
  templateUrl: './directivos-list.component.html',
  styleUrls: ['./directivos-list.component.scss'],
})
export class DirectivosListComponent implements OnInit {
  private api = inject(DirectivosApi);
  private router = inject(Router);
  private catalogos = inject(CatalogosApi);

  q = signal<string>('');
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  data = signal<DirectivoListItemDTO[]>([]);

  // mapa codigo → nombre (tipos directivos)
  tiposMap = signal<Record<string, string>>({});

  ngOnInit(): void {
    // primero catálogos, luego lista
    this.catalogos.tiposDirectivos().subscribe({
      next: (rows: CodigoNombreDTO[]) => {
        const map: Record<string, string> = {};
        (rows || []).forEach(r => map[r.codigo] = r.nombre);
        this.tiposMap.set(map);
        this.load();
      },
      error: () => { this.tiposMap.set({}); this.load(); }
    });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.list(this.q()).subscribe({
      next: (rows) => {
        const arr = Array.isArray(rows) ? rows : (rows as any)?.content ?? [];
        this.data.set(arr);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Error cargando directivos');
        this.loading.set(false);
      },
    });
  }

  buscar(): void { this.load(); }
  crear(): void { this.router.navigate(['/general/directivos/new']); }
  editar(id: number): void { this.router.navigate(['/general/directivos', id, 'edit']); }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este directivo?')) return;
    this.loading.set(true);
    this.api.remove(id).subscribe({
      next: () => this.load(),
      error: () => { this.error.set('No se pudo eliminar'); this.loading.set(false); },
    });
  }

  // Ir a impresión
  imprimir(): void {
    this.router.navigate(['/general/directivos/print'], {
      queryParams: { q: this.q() || null }
    });
  }

  // Helpers
  nomCalidad(v: string) { return v === '1' ? 'Principal' : 'Suplente'; }
  nomEstado(v: string)  { return v === '1' ? 'Nombrado' : v === '2' ? 'Retirado' : 'Excluido'; }
  nomTipo(codigo: string) { return this.tiposMap()[codigo] ?? codigo; }

  /** Devuelve '—' si vacío; si es fecha válida o string ISO, muestra yyyy-MM-dd */
  fmtDate(value: any): string {
    if (value === null || value === undefined || value === '') return '—';
    // Si ya viene en yyyy-MM-dd, lo devolvemos tal cual
    const s = String(value).trim();
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
    const d = new Date(value);
    if (isNaN(d.getTime())) return s || '—';
    return d.toISOString().slice(0, 10); // yyyy-MM-dd
  }
}
