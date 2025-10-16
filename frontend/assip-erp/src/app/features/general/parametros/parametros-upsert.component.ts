import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ParametrosApi, ParametroCreate } from './parametros.api';
import { AgenciasApi } from '../agencias/agencias.api';

type AgenciaLite = {
  idAgencia: number;
  nombreAgencia?: string | null;
  siglaAgencia?: string | null;
  codigoAgencia?: string | null;
};

@Component({
  selector: 'app-parametros-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './parametros-upsert.component.html',
  styleUrls: ['./parametros-upsert.component.scss']
})
export class ParametrosUpsertComponent implements OnInit {
  private api = inject(ParametrosApi);
  private agenciasApi = inject(AgenciasApi);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  id = signal<number | null>(null);
  loading = signal<boolean>(false);
  guardando = signal<boolean>(false);
  errorMsg = signal<string | null>(null);

  agencias = signal<AgenciaLite[]>([]);
  valorDisplay = signal<string>('0,00'); // máscara para el input de Valor

  form = this.fb.nonNullable.group({
    idAgencia: [null as number | null, [Validators.required]],
    codigoParametro: [0 as number | null, [Validators.required, Validators.min(0)]],
    nombreParametro: ['', [Validators.required, Validators.maxLength(100)]],
    valorParametro: [0 as number | null, [Validators.required]],
    tipoValor: [true as boolean | null, [Validators.required]],
  });

  ngOnInit(): void {
    // Cargar agencias (tu API devuelve array, no Page)
    this.agenciasApi.list('').subscribe((res: any) => {
      this.agencias.set(res as AgenciaLite[]);
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id.set(Number(idParam));
      // Modo edición: bloquear de una vez (evita flicker y warnings)
      this.form.controls.idAgencia.disable({ emitEvent: false });
      this.form.controls.codigoParametro.disable({ emitEvent: false });
      this.cargar();
    } else {
      // Modo creación: asegurar habilitados
      this.form.controls.idAgencia.enable({ emitEvent: false });
      this.form.controls.codigoParametro.enable({ emitEvent: false });
      const v = this.form.controls.valorParametro.value ?? 0;
      this.valorDisplay.set(this.formatNumberEs(v));
    }
  }

  private cargar() {
    if (!this.id()) return;
    this.loading.set(true);
    this.api.get(this.id()!).subscribe({
      next: (p) => {
        this.form.patchValue({
          idAgencia: p.idAgencia ?? null,
          codigoParametro: p.codigoParametro ?? 0,
          nombreParametro: p.nombreParametro ?? '',
          valorParametro: p.valorParametro ?? 0,
          tipoValor: p.tipoValor ?? true,
        });

        // Ya están deshabilitados (se mantiene)
        const v = this.form.controls.valorParametro.value ?? 0;
        this.valorDisplay.set(this.formatNumberEs(v));

        this.loading.set(false);
      },
      error: (err) => {
        this.errorMsg.set(err?.error?.message ?? 'Error cargando el parámetro');
        this.loading.set(false);
      }
    });
  }

  // ===== Formato numérico (Valor) =====
  onValorInput(e: Event) {
    const el = e.target as HTMLInputElement;
    this.valorDisplay.set(el.value);
  }

  onValorBlur() {
    const parsed = this.parseEsToNumber(this.valorDisplay());
    const val = Number.isFinite(parsed) ? parsed : 0;
    this.form.controls.valorParametro.setValue(val);
    this.valorDisplay.set(this.formatNumberEs(val));
  }

  private formatNumberEs(n: number): string {
    try {
      return (n ?? 0).toLocaleString('es-ES', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });
    } catch {
      return '0,00';
    }
  }

  private parseEsToNumber(s: string): number {
    if (!s) return 0;
    const normalized = s.replace(/\./g, '').replace(',', '.');
    const n = Number(normalized);
    return Number.isFinite(n) ? Math.round(n * 100) / 100 : NaN;
  }

  // ===== Guardar / Navegación =====
  guardar() {
    this.errorMsg.set(null);

    // Sincroniza valor formateado -> número antes de validar
    this.onValorBlur();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: ParametroCreate = this.form.getRawValue();
    this.guardando.set(true);

    const goList = () => {
      this.guardando.set(false);
      this.router.navigate(['/general/parametros']);
    };

    if (this.id()) {
      this.api.update(this.id()!, payload).subscribe({
        next: goList,
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'Error actualizando');
          this.guardando.set(false);
        }
      });
    } else {
      this.api.create(payload).subscribe({
        next: goList,
        error: (err) => {
          this.errorMsg.set(err?.error?.message ?? 'Error creando');
          this.guardando.set(false);
        }
      });
    }
  }

  cancelar() {
    this.router.navigate(['/general/parametros']);
  }
}
