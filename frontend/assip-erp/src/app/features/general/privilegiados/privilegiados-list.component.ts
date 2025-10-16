import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PrivilegiadosApi, PrivilegiadoListItemDTO } from './privilegiados.api';
import { DirectivosApi, DirectivoListItemDTO } from '../directivos/directivos.api';

@Component({
  selector: 'app-privilegiados-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <div class="card">
    <div class="card-header">
      <h2>Privilegiados</h2>
      <div class="actions">
        <a class="btn primary" [routerLink]="['/general/privilegiados/new']" [queryParams]="{ idDirectivo: idDirectivo() }">Nuevo</a>
        <button class="btn btn--print" (click)="imprimir()" [disabled]="loading()">Imprimir</button>
      </div>
    </div>

    <div class="card-body">
      <div class="toolbar">
        <input
          [ngModel]="q()"
          (ngModelChange)="q.set($event)"
          [ngModelOptions]="{ standalone: true }"
          type="text"
          placeholder="Buscar por documento o nombre…"
          (keyup.enter)="buscar()"
        />
        <button class="btn" (click)="buscar()" [disabled]="loading()">Buscar</button>
      </div>

      <table class="table">
        <thead>
        <tr>
          <th style="width:120px;">Doc. Persona</th>
          <th>Nombre Persona</th>
          <th style="width:180px;">Parentesco</th>
          <th style="width:120px;">Doc. Directivo</th>
          <th>Nombre Directivo</th>
          <th class="th-acciones">Acciones</th>
        </tr>
        </thead>
        <tbody>
        <tr *ngIf="loading()">
          <td colspan="6" class="empty">Cargando…</td>
        </tr>
        <tr *ngIf="!loading() && !data().length && !error()">
          <td colspan="6" class="empty">No hay datos</td>
        </tr>
        <tr *ngIf="error()">
          <td colspan="6" class="empty">{{ error() }}</td>
        </tr>

        <tr *ngFor="let r of data()">
          <td>{{ r.documentoPersona }}</td>
          <td>{{ r.nombrePersona }}</td>
          <td>{{ r.codigoParentesco }}<ng-container *ngIf="r.parentescoNombre"> — {{ r.parentescoNombre }}</ng-container></td>
          <td>{{ r.documentoDirectivo || docDirectivo() || '—' }}</td>
          <td>{{ r.nombreDirectivo || nomDirectivo() || '—' }}</td>
          <td class="acciones">
            <button class="btn sm success" (click)="editar(r.idPrivilegiado)">Editar</button>
            <button class="btn sm danger" (click)="eliminar(r.idPrivilegiado)" [disabled]="loading()">Eliminar</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
  `,
  styles: [`
    .card { background:#fff; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,.06); }
    .card-header { display:flex; justify-content:space-between; align-items:center; padding:12px 16px; border-bottom:1px solid #eee; }
    .card-body { padding:12px 16px; }
    .toolbar { display:flex; gap:8px; margin-bottom:12px; }
    .toolbar input { padding:8px 10px; border:1px solid #ddd; border-radius:6px; flex:1; }
    .table { width:100%; border-collapse: collapse; table-layout: fixed; }
    .table th, .table td { padding:8px 10px; border-bottom:1px solid #eee; text-align:left; vertical-align:top; box-sizing:border-box; word-wrap: break-word; }
    .th-acciones, td.acciones { text-align:center; white-space:nowrap; }
    .empty { text-align:center; color:#777; padding: 12px 0; }
    .btn { padding:6px 10px; border:1px solid #ccc; border-radius:6px; background:#fafafa; cursor:pointer; }
    .btn.primary { background:#0d6efd; color:#fff; border-color:#0d6efd; }
    .btn.danger  { background:#dc3545; color:#fff; border-color:#dc3545; }
    .btn.sm { height: 32px; padding: 0 8px; font-size: 0.875rem; line-height: 32px; }
    .acciones { display:flex; justify-content:center; align-items:center; gap:6px; }
  `]
})
export class PrivilegiadosListComponent implements OnInit {
  private api = inject(PrivilegiadosApi);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private directivosApi = inject(DirectivosApi);

  q = signal<string>('');
  idDirectivo = signal<number | null>(null);
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  data = signal<PrivilegiadoListItemDTO[]>([]);

  // Fallback para mostrar el directivo en todas las filas
  private _docDirectivo = signal<string>('');
  private _nomDirectivo = signal<string>('');
  docDirectivo = () => this._docDirectivo();
  nomDirectivo = () => this._nomDirectivo();

  ngOnInit(): void {
    // 1) query ?idDirectivo=
    const idFromQuery = Number(this.route.snapshot.queryParamMap.get('idDirectivo'));
    // 2) :idDirectivo en path (si tu ruta lo usa)
    const idFromParam = Number(this.route.snapshot.paramMap.get('idDirectivo'));
    // 3) sessionStorage (último usado)
    const idFromSession = Number(sessionStorage.getItem('idDirectivo') || '');

    const pick = (...vals: number[]) => vals.find(v => Number.isFinite(v) && v > 0) ?? null;
    const id = pick(idFromQuery, idFromParam, idFromSession);

    if (!id) {
      this.error.set('Falta idDirectivo en la ruta o en ?idDirectivo=');
      return;
    }

    this.idDirectivo.set(id);
    sessionStorage.setItem('idDirectivo', String(id));

    // Si no estaba en query, escríbelo para futuras recargas/prints/exports
    if (!Number.isFinite(idFromQuery) || idFromQuery <= 0) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { idDirectivo: id },
        queryParamsHandling: 'merge',
        replaceUrl: true
      });
    }

    // Cargar una sola vez el documento/nombre del directivo (fallback por fila)
    this.cargarDirectivoLite(id);

    this.load();
  }

  private cargarDirectivoLite(idDirectivo: number) {
    // Usamos list(q) con el ID como texto y filtramos por coincidencia exacta del id
    this.directivosApi.list(String(idDirectivo)).subscribe({
      next: (res: any) => {
        const rows = Array.isArray(res) ? res : (res?.content ?? []);
        const found = (rows as DirectivoListItemDTO[]).find(r => r.idDirectivo === idDirectivo);
        if (found) {
          this._docDirectivo.set((found.documento ?? '').toString().trim());
          this._nomDirectivo.set((found.nombrePersona ?? '').toString().trim());
        } else {
          this._docDirectivo.set('');
          this._nomDirectivo.set('');
        }
      },
      error: () => {
        this._docDirectivo.set('');
        this._nomDirectivo.set('');
      }
    });
  }

  load(): void {
    if (!this.idDirectivo()) return;
    this.loading.set(true);
    this.error.set(null);
    this.api.list(this.idDirectivo()!, this.q()).subscribe({
      next: (rows) => {
        const arr = Array.isArray(rows) ? rows : (rows as any)?.content ?? [];
        this.data.set(arr);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Error cargando privilegiados');
        this.loading.set(false);
      },
    });
  }

  buscar(): void { this.load(); }
  crear(): void {
    this.router.navigate(['/general/privilegiados/new'], {
      queryParams: { idDirectivo: this.idDirectivo() }
    });
  }
  editar(id: number): void {
    this.router.navigate(['/general/privilegiados', id, 'edit'], {
      queryParams: { idDirectivo: this.idDirectivo() }
    });
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar este privilegiado?')) return;
    this.loading.set(true);
    this.api.remove(id).subscribe({
      next: () => this.load(),
      error: () => { this.error.set('No se pudo eliminar'); this.loading.set(false); },
    });
  }

  imprimir(): void {
    this.router.navigate(['/general/privilegiados/print'], {
      queryParams: { idDirectivo: this.idDirectivo(), q: this.q() || null }
    });
  }
}
