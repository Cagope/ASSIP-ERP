import { Component, inject, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { DatosPersonalesApi, DatosPersonalesListItem, Page } from './datos-personales.api';
import { DatosPersonalesExportButtonComponent } from './datos-personales-export-button.component';

@Component({
  selector: 'app-datos-personales-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, DatosPersonalesExportButtonComponent],
  templateUrl: './datos-personales-list.component.html',
  styleUrls: ['./datos-personales-list.component.scss']
})
export class DatosPersonalesListComponent implements OnInit {
  private api = inject(DatosPersonalesApi);
  private router = inject(Router);

  // Decodificación de tipo de documento
  private readonly TIPOS_DOC_MAP: Record<string, string> = {
    CC: 'Cédula de Ciudadanía',
    TI: 'Tarjeta de Identidad',
    CE: 'Cédula de Extranjería',
    PA: 'Pasaporte',
    RC: 'Registro Civil',
    NIT: 'NIT',
    PEP: 'Permiso Especial',
  };

  q = new FormControl<string>('', { nonNullable: true });
  page: Page<DatosPersonalesListItem> | null = null;
  loading = false;

  ngOnInit(): void {
    this.q.valueChanges.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.load(0));
    this.load(0);
  }

  load(pageNumber: number) {
    this.loading = true;
    // Intento ordenar desde el backend; si no lo respeta, cliente lo garantiza
    this.api.list({ q: this.q.value.trim(), page: pageNumber, size: 20, sort: 'fechaActualizacion,desc' })
      .subscribe({
        next: (res) => {
          const ordered = [...(res.content || [])].sort((a, b) => {
            const ak = this.sortKey(a);
            const bk = this.sortKey(b);
            return bk - ak; // DESC
          });
          this.page = { ...res, content: ordered };
        },
        error: () => {},
        complete: () => this.loading = false
      });
  }

  buscar() {
    this.load(0);
  }

  onCreate() {
    this.router.navigate(['/hoja-vida/datos-personales/new']);
  }

  onEdit(item: DatosPersonalesListItem) {
    this.router.navigate(['/hoja-vida/datos-personales', item.idDatosPersonal, 'edit']);
  }

  onDelete(item: DatosPersonalesListItem) {
    const ok = confirm(`¿Eliminar el registro #${item.idDatosPersonal} (${item.tipoDocumento} ${item.documento})?`);
    if (!ok) return;
    this.loading = true;
    this.api.delete(item.idDatosPersonal).subscribe({
      next: () => this.load(this.page?.number ?? 0),
      error: () => this.load(this.page?.number ?? 0)
    });
  }

  imprimir() {
    const q = this.q.value.trim();
    this.router.navigate(['/hoja-vida/datos-personales/print'], { queryParams: q ? { q } : {} });
  }

  onPageChange(next: number) {
    if (!this.page) return;
    if (next < 0 || next >= this.page.totalPages) return;
    this.load(next);
  }

  // <<< helper para formatear la fecha como YYYY-MM-DD >>>
  toYMD(val: string | Date | null | undefined): string {
    if (!val) return '';
    if (typeof val === 'string') {
      // Si ya viene como 2024-10-13 o 2024-10-13T00:00:00, toma solo YYYY-MM-DD
      const m = /^(\d{4}-\d{2}-\d{2})/.exec(val);
      if (m) return m[1];
      const d = new Date(val);
      return isNaN(d.getTime()) ? '' : d.toISOString().slice(0, 10);
    }
    const d = val instanceof Date ? val : new Date(val as any);
    return isNaN(d.getTime()) ? '' : d.toISOString().slice(0, 10);
  }
  // >>> FIN helper fecha <<<

  // Decodifica tipo de documento
  td(code: string | null | undefined): string {
    if (!code) return '';
    const k = String(code).toUpperCase().trim();
    return this.TIPOS_DOC_MAP[k] ?? k;
  }

  // Orden: fechaActualizacion || fechaActualiza || fechaCreacion || fechaApertura (camel o snake)
  private sortKey(it: DatosPersonalesListItem): number {
    const anyp: any = it as any;
    const raw =
      anyp?.fechaActualizacion ?? anyp?.fecha_actualizacion ??
      anyp?.fechaActualiza     ?? anyp?.fecha_actualiza ??
      anyp?.fechaCreacion      ?? anyp?.fecha_creacion ??
      anyp?.fechaApertura      ?? anyp?.fecha_apertura ?? null;

    const d = this.toDateSafe(raw);
    return d ? d.getTime() : -Infinity;
  }

  private toDateSafe(val: string | Date | null | undefined): Date | null {
    if (!val) return null;
    if (val instanceof Date) return isNaN(val.getTime()) ? null : val;
    const d = new Date(val);
    return isNaN(d.getTime()) ? null : d;
  }
}
