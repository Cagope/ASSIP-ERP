import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators
} from '@angular/forms';

import { SessionService } from '../../../core/auth/session.service';

import {
  DepreciacionApi,
  DepreciacionRequestDTO,
  DepreciacionPreviewResult
} from './depreciacion.api';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

// ✅ Plan de cuentas (resolver ID -> codigo + nombre)
import {
  PlanCuentasApi,
  PlanCuenta
} from '../../contabilidad/plan-cuentas/plan-cuentas.api';

// ✅ Personas (para decodificar tercero)
import {
  PersonasApi,
  PersonaBusquedaDTO
} from '../../../shared/personas/personas.api';

// ✅ EXPORTADOR EXCEL
import { DepreciacionExporter } from './depreciacion.exporter';

@Component({
  standalone: true,
  selector: 'app-depreciacion',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './depreciacion.component.html',
  styleUrls: ['./depreciacion.component.scss']
})
export class DepreciacionComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private fb = inject(FormBuilder);
  private api = inject(DepreciacionApi);
  public session = inject(SessionService);

  private tiposComprobantesApi = inject(TiposComprobantesApi);
  private planCuentasApi = inject(PlanCuentasApi);
  private personasApi = inject(PersonasApi);

  // =========================================================
  // ESTADO
  // =========================================================
  tiposComprobantes: TipoComprobante[] = [];
  consecutivoSugerido: string | null = null;

  previewResult: DepreciacionPreviewResult | null = null;
  loading = false;
  errorMsg: string | null = null;

  // ✅ catálogo cuentas para resolver label
  catalogoCuentas: PlanCuenta[] = [];
  private cuentasById = new Map<number, string>(); // id -> "codigo — nombre"

  // ✅ terceros cache (id -> documento)
  private tercerosById = new Map<number, string>(); // idDatosPersonal -> documento

  // =========================================================
  // FORMULARIO
  // =========================================================
  form = this.fb.nonNullable.group({
    idAgencia: [null as number | null, Validators.required],

    fechaPeriodo: ['', Validators.required],
    fechaContabilizacion: ['', Validators.required],

    tipoComprobante: ['', Validators.required],
    numeroComprobante: ['', Validators.required], // readonly en HTML

    concepto: ['', [Validators.required, Validators.maxLength(200)]]
  });

  // =========================================================
  // CICLO DE VIDA
  // =========================================================
  ngOnInit(): void {

    // cuando cambia agencia
    this.form.get('idAgencia')!.valueChanges.subscribe(idAgencia => {

      // limpiar preview + caches
      this.previewResult = null;
      this.cuentasById.clear();
      this.tercerosById.clear();
      this.catalogoCuentas = [];

      if (!idAgencia) {
        this.resetComprobante();
        this.tiposComprobantes = [];
        return;
      }

      this.cargarTiposComprobante(idAgencia);
      this.cargarPlanCuentas(idAgencia);
    });

    // cuando cambia tipo comprobante → CSC + 1
    this.form.get('tipoComprobante')!.valueChanges.subscribe(tipo => {

      if (!tipo) {
        this.consecutivoSugerido = null;
        this.form.get('numeroComprobante')!.reset();
        return;
      }

      const tc = this.tiposComprobantes.find(
        t => t.tipoComprobante === tipo
      );

      if (!tc) return;

      const siguiente = (tc.cscComprobante ?? 0) + 1;
      const consecutivo = siguiente.toString().padStart(7, '0');

      this.consecutivoSugerido = consecutivo;
      this.form.get('numeroComprobante')!.setValue(consecutivo);
    });

    // ✅ Sugerir concepto automáticamente cuando cambien las fechas
    this.form.get('fechaPeriodo')!.valueChanges.subscribe(() => {
      this.sugerirConcepto();
    });

    this.form.get('fechaContabilizacion')!.valueChanges.subscribe(() => {
      this.sugerirConcepto();
    });

  }

  // =========================================================
  // TIPOS DE COMPROBANTE
  // =========================================================
  private cargarTiposComprobante(idAgencia: number): void {

    this.resetComprobante();

    this.tiposComprobantesApi
      .listarPorAgencia(idAgencia)
      .subscribe(data => {
        this.tiposComprobantes = data || [];
      });
  }

  private resetComprobante(): void {
    this.form.get('tipoComprobante')!.reset();
    this.form.get('numeroComprobante')!.reset();
    this.consecutivoSugerido = null;
  }

  // =========================================================
  // ✅ PLAN CUENTAS (resolver ID -> codigo + nombre)
  // =========================================================
  private cargarPlanCuentas(idAgencia: number): void {

    this.planCuentasApi.listar(idAgencia).subscribe(d => {
      this.catalogoCuentas = d || [];
      this.rebuildCuentasMap();
    });
  }

  private rebuildCuentasMap(): void {
    this.cuentasById.clear();
    for (const c of this.catalogoCuentas) {
      if (c?.id != null) {
        this.cuentasById.set(
          Number(c.id),
          `${c.codigoCuenta} — ${c.nombre}`
        );
      }
    }
  }

  // =========================================================
  // ✅ RESOLVER CUENTA (sin "Cuenta")
  // =========================================================
  cuentaLabel(idCuenta: number | null | undefined): string {
    if (!idCuenta) return '';
    return this.cuentasById.get(idCuenta) ?? String(idCuenta);
  }

  // =========================================================
  // ✅ RESOLVER TERCERO: mostrar DOCUMENTO
  // =========================================================
  terceroDocumento(idTercero: number | null | undefined): string {
    if (!idTercero) return '';
    return this.tercerosById.get(idTercero) ?? String(idTercero);
  }

  /**
   * ✅ Cargar documentos de terceros del preview (por ID exacto)
   */
  private precargarTercerosPreview(): void {

    if (!this.previewResult?.detalle?.length) return;

    const ids = Array.from(
      new Set(
        this.previewResult.detalle
          .map(x => x.idDatosPersonalProveedor)
          .filter((v): v is number => v != null)
      )
    );

    const faltantes = ids.filter(id => !this.tercerosById.has(id));
    if (!faltantes.length) return;

    faltantes.forEach(id => {
      this.personasApi.obtenerPorId(id).subscribe(p => {
        if (p?.documento) {
          this.tercerosById.set(id, p.documento);
        }
      });
    });
  }

  // =========================================================
  // PREVIEW
  // =========================================================
  preview(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (!raw.idAgencia) {
      this.form.get('idAgencia')!.markAsTouched();
      return;
    }

    this.loading = true;
    this.errorMsg = null;
    this.previewResult = null;

    const payload: DepreciacionRequestDTO = {
      idAgencia: raw.idAgencia,
      fechaPeriodo: raw.fechaPeriodo,
      fechaContabilizacion: raw.fechaContabilizacion,
      tipoComprobante: raw.tipoComprobante,
      numeroComprobante: raw.numeroComprobante,
      concepto: raw.concepto
    };

    this.api.preview(payload).subscribe({
      next: res => {
        this.previewResult = res;
        this.loading = false;

        // ✅ terceros (documento)
        this.precargarTercerosPreview();
      },
      error: err => {
        this.errorMsg = err?.error?.message ?? 'Error al ejecutar preview';
        this.loading = false;
      }
    });
  }

  // =========================================================
  // EJECUTAR
  // =========================================================
  ejecutar(): void {

    if (!this.previewResult) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    if (!raw.idAgencia) {
      this.form.get('idAgencia')!.markAsTouched();
      return;
    }

    this.loading = true;
    this.errorMsg = null;

    const payload: DepreciacionRequestDTO = {
      idAgencia: raw.idAgencia,
      fechaPeriodo: raw.fechaPeriodo,
      fechaContabilizacion: raw.fechaContabilizacion,
      tipoComprobante: raw.tipoComprobante,
      numeroComprobante: raw.numeroComprobante,
      concepto: raw.concepto
    };

    this.api.ejecutar(payload).subscribe({
      next: () => {
        this.loading = false;
        alert('Depreciación ejecutada correctamente');
        this.previewResult = null;
      },
      error: err => {
        this.errorMsg = err?.error?.message ?? 'Error al ejecutar depreciación';
        this.loading = false;
      }
    });
  }

  // =========================================================
  // ✅ EXPORTAR EXCEL (REPORTES)
  // =========================================================
  exportarListadoExcel(): void {

    if (!this.previewResult?.detalle?.length) return;

    const raw = this.form.getRawValue();
    const agencia = this.session.getAgencias().find(a => a.idAgencia === raw.idAgencia);

    DepreciacionExporter.exportarListado(this.previewResult.detalle, {
      agenciaNombre: agencia ? `${agencia.codigoAgencia} — ${agencia.nombreAgencia}` : String(raw.idAgencia),
      fechaPeriodo: raw.fechaPeriodo,
      fechaContabilizacion: raw.fechaContabilizacion,
      tipoComprobante: raw.tipoComprobante,
      numeroComprobante: raw.numeroComprobante,
      concepto: raw.concepto
    });
  }

  // =========================================================
  // ✅ EXPORTAR AUXILIARES CONTABLES (IMPORTABLE)
  // =========================================================
  exportarAuxiliaresContablesExcel(): void {

    if (!this.previewResult?.detalle?.length) return;

    const raw = this.form.getRawValue();

    if (!raw.idAgencia) {
      alert('Debe seleccionar agencia');
      return;
    }

    // ✅ idUsuario: depende de tu SessionService
    // Si tienes getUsuario() úsalo, si no, deja fallback
    const idUsuario =
      (this.session as any)?.getUsuario?.()?.idUsuario
      ?? (this.session as any)?.usuario?.idUsuario
      ?? 1;

    // ✅ tercero fallback: si algún activo NO trae proveedor
    // pon el del usuario admin o el tercero genérico contable
    const idTerceroFallback = idUsuario;

    DepreciacionExporter.exportarAuxiliaresContables(
      this.previewResult.detalle,
      {
        idAgencia: raw.idAgencia,
        fechaAuxiliar: raw.fechaContabilizacion,
        tipoComprobante: raw.tipoComprobante,
        numeroComprobante: raw.numeroComprobante,
        concepto: raw.concepto,

        estadoMovimiento: 'A',
        valorBase: 0,

        idUsuario,
        idTerceroFallback
      }
    );
  }

  // =========================================================
  // ✅ CUENTA: separar código y nombre solo para UI
  // =========================================================
  cuentaCodigo(idCuenta: number | null | undefined): string {
    if (!idCuenta) return '';
    const label = this.cuentasById.get(idCuenta); // "codigo — nombre"
    if (!label) return String(idCuenta);

    return label.split(' — ')[0] ?? String(idCuenta);
  }

  cuentaNombre(idCuenta: number | null | undefined): string {
    if (!idCuenta) return '';
    const label = this.cuentasById.get(idCuenta);
    if (!label) return '';

    return label.split(' — ')[1] ?? '';
  }

  // =========================================================
  // ✅ Sugerir concepto
  // =========================================================
  private sugerirConcepto(): void {

    const periodo = this.form.get('fechaPeriodo')?.value;
    const contabilizacion = this.form.get('fechaContabilizacion')?.value;

    if (!periodo || !contabilizacion) return;

    const texto = `Depreciación para la fecha: ${periodo} contabilizados en la fecha: ${contabilizacion}`;

    // ✅ Solo autollenar si está vacío o si aún tiene un texto "anterior" autogenerado
    const actual = this.form.get('concepto')?.value ?? '';

    // Si el usuario ya escribió algo distinto, NO lo piso
    if (actual && !actual.startsWith('Depreciación para la fecha:')) return;

    this.form.get('concepto')?.setValue(texto, { emitEvent: false });
  }

}
