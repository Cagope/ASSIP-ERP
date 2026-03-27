import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  LiquidacionApi,
  LiquidacionPreviewContratoDTO,
  LiquidacionPreviewExcelDTO,
  LiquidacionRequestDTO
} from '../liquidacion.api';

import {
  PeriodosNominaApi,
  PeriodoNominaListDTO
} from '../../periodos-nomina/periodos-nomina.api';

import { SessionService } from '../../../../core/auth/session.service';
import { HeaderActionsComponent } from '../../../../shared/header-actions/header-actions.component';
import { EmpleadosApi, EmpleadoListDTO } from '../../empleados/empleados.api';

// ✅ Personas: para resolver idDatosPersonal -> documento + nombre
import { PersonasApi } from '../../../../shared/personas/personas.api';
import * as XLSX from 'xlsx';

@Component({
  selector: 'app-liquidacion-preview',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './liquidacion-preview.component.html',
  styleUrls: ['./liquidacion-preview.component.scss'],
})
export class LiquidacionPreviewComponent implements OnInit {

  // =========================================================
  // FORMULARIO
  // =========================================================
  form: FormGroup;

  // =========================================================
  // ESTADO
  // =========================================================
  cargando = false;
  error: string | null = null;

  // =========================================================
  // CATÁLOGOS
  // =========================================================
  periodos: PeriodoNominaListDTO[] = [];

  // =========================================================
  // RESULTADO BACKEND
  // =========================================================
  preview: LiquidacionPreviewContratoDTO[] = [];

  // =========================================================
  // RESUMEN GLOBAL
  // =========================================================
  totalContratos = 0;
  totalDevengados = 0;
  totalDeducciones = 0;
  totalNeto = 0;

  // =========================================================
  // EMPLEADOS (CACHE PARA PINTAR NOMBRE)
  // =========================================================
  empleados: EmpleadoListDTO[] = [];

  // =========================================================
  // PERÍODO OPERATIVO RESUELTO
  // =========================================================
  periodoLabel: string = 'Automático · Primer período ABIERTO';

  // idEmpleado -> "documento — APELLIDOS NOMBRES"
  private empleadosById = new Map<number, string>();

  // ✅ idEmpleado -> idDatosPersonal (porque empleados NO trae nombres)
  private empleadoToDatos = new Map<number, number>();

  constructor(
    private fb: FormBuilder,
    private api: LiquidacionApi,
    public session: SessionService,
    private periodosApi: PeriodosNominaApi,
    private empleadosApi: EmpleadosApi,
    private personasApi: PersonasApi
  ) {

    this.form = this.fb.group({
      fkAgencia: [null, Validators.required],
    });
  }

  // =========================================================
  // INIT
  // =========================================================
  ngOnInit(): void {

    this.cargarPeriodos();
    this.cargarEmpleados();

    // 🔥 CUANDO CAMBIA AGENCIA → LIMPIAR CONTEXTO + RECARGAR EMPLEADOS (por agencia)
    this.form.get('fkAgencia')!.valueChanges.subscribe((idAgencia: number | null) => {
      this.limpiarPreview();
      this.cargarEmpleados(); // recarga y filtra por agencia seleccionada
    });

  }

  // =========================================================
  // CARGAR PERÍODOS
  // =========================================================
  private cargarPeriodos(): void {
    this.periodosApi.listar().subscribe({
      next: d => this.periodos = d ?? [],
      error: () => this.periodos = []
    });
  }

  // =========================================================
  // LIMPIAR PREVIEW
  // =========================================================
  private limpiarPreview(): void {
    this.preview = [];
    this.totalContratos = 0;
    this.totalDevengados = 0;
    this.totalDeducciones = 0;
    this.totalNeto = 0;
    this.error = null;

    this.periodoLabel = 'Automático · Primer período ABIERTO'; // 👈 NUEVO
  }

  // =========================================================
  // ▶️ PREVIEW
  // =========================================================
  ejecutarPreview(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.value;

    const dto: LiquidacionRequestDTO = {
      fkAgencia: raw.fkAgencia
    };

    this.cargando = true;
    this.error = null;
    this.preview = [];

    this.api.previewPeriodo(dto).subscribe({
      next: (data: LiquidacionPreviewContratoDTO[]) => {
        this.preview = data ?? [];
        this.calcularTotales();
        this.actualizarPeriodoLabel(); // 👈 NUEVO

        // ✅ por si cambió algo, intentamos resolver nombres de empleados del preview
        this.precargarEmpleadosDelPreview();

        this.cargando = false;
      },
      error: (err: unknown) => {
        this.error =
          (err as any)?.error?.message ??
          'Error al generar preview de liquidación';
        this.cargando = false;
      }
    });
  }

  // =========================================================
  // 🧮 TOTALES
  // =========================================================
  private calcularTotales(): void {

    let dev = 0;
    let ded = 0;
    let net = 0;

    for (const c of this.preview) {
      dev += Number(c.totales?.totalDevengados ?? 0);
      ded += Number(c.totales?.totalDeducciones ?? 0);
      net += Number(c.totales?.netoPagar ?? 0);
    }

    this.totalContratos = this.preview.length;
    this.totalDevengados = dev;
    this.totalDeducciones = ded;
    this.totalNeto = net;
  }

  // =========================================================
  // 🗓️ PERÍODO OPERATIVO (LABEL UI)
  // =========================================================
  private actualizarPeriodoLabel(): void {

    if (!this.preview || this.preview.length === 0) {
      this.periodoLabel = 'Automático · Primer período ABIERTO';
      return;
    }

    const p = this.preview[0]?.periodo;
    if (!p) {
      this.periodoLabel = 'Automático · Primer período ABIERTO';
      return;
    }

    const mes = String(p.mes).padStart(2, '0');
    const q = p.numeroPeriodo != null ? `Q${p.numeroPeriodo}` : '';
    const tipo = p.tipoPeriodo ? `(${p.tipoPeriodo})` : '';

    this.periodoLabel =
      `Automático · ${p.anio}-${mes} ${q} ${tipo}`.replace(/\s+/g, ' ').trim();
  }

  // =========================================================
  // 📤 EXPORTAR EXCEL
  // =========================================================
  exportarExcel(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.value;

    const dto: LiquidacionRequestDTO = {
      fkAgencia: raw.fkAgencia
    };

    this.cargando = true;

    this.api.previewPeriodoExcel(dto).subscribe({
      next: (rows: LiquidacionPreviewExcelDTO[]) => {
        this.cargando = false;
        this.exportarAExcel(rows);
      },
      error: (err) => {
        this.cargando = false;
        console.error('Error exportando Excel', err);
        alert(
          (err as any)?.error?.message ??
          'Error exportando Excel'
        );
      }
    });
  }

  private exportarAExcel(rows: LiquidacionPreviewExcelDTO[]): void {

    if (!rows || rows.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    // 🟢 Hoja Excel
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(rows);

    // 🟢 Libro
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Preview Liquidación');

    const idAgencia = this.form.get('fkAgencia')?.value ?? 'agencia';
    const idPeriodo = 'auto';

    // 🟢 Descargar
    XLSX.writeFile(
      wb,
      `preview_liquidacion_ag${idAgencia}_per${idPeriodo}.xlsx`
    );
  }

  // =========================================================
  // ▶️ EJECUTAR LIQUIDACIÓN
  // =========================================================
  ejecutarLiquidacion(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.preview.length === 0) {
      this.error = 'Debe generar el preview antes de ejecutar la liquidación';
      return;
    }

    const confirmar = confirm(
      '¿Está seguro de ejecutar definitivamente la liquidación del período seleccionado?'
    );

    if (!confirmar) return;

    const raw = this.form.value;

    const dto: LiquidacionRequestDTO = {
      fkAgencia: raw.fkAgencia
    };

    this.cargando = true;
    this.error = null;

    this.api.ejecutarLiquidacion(dto).subscribe({
      next: () => {
        this.cargando = false;
        alert('Liquidación ejecutada correctamente');
        this.limpiarPreview();
      },
      error: (err: unknown) => {
        this.error =
          (err as any)?.error?.message ??
          'Error al ejecutar la liquidación';
        this.cargando = false;
      }
    });
  }

  // =========================================================
  // EMPLEADOS (PARA MOSTRAR NOMBRE EN UI)
  // =========================================================
  private cargarEmpleados(): void {

    const idAgenciaSel = this.form.get('fkAgencia')?.value as number | null;

    this.empleadosApi.listar().subscribe({
      next: (d: EmpleadoListDTO[]) => {

        const lista = d ?? [];

        // ✅ Filtrar por agencia si el DTO trae idAgencia / id_agencia
        const filtrados = idAgenciaSel
          ? lista.filter((e: any) => {
              const ag = e?.idAgencia ?? e?.id_agencia ?? e?.fkAgencia;
              return ag == null ? true : Number(ag) === Number(idAgenciaSel);
            })
          : lista;

        this.empleados = filtrados;

        this.empleadosById.clear();
        this.empleadoToDatos.clear();

        // 1) Mapear idEmpleado -> idDatosPersonal
        for (const e of this.empleados as any[]) {

          const idEmpleado = Number(e?.idEmpleado ?? e?.id_empleado);
          const idDatos = Number(e?.idDatosPersonal ?? e?.id_datos_personal);

          if (!Number.isFinite(idEmpleado)) continue;
          if (!Number.isFinite(idDatos)) continue;

          this.empleadoToDatos.set(idEmpleado, idDatos);

          // placeholder mientras resolvemos persona
          this.empleadosById.set(idEmpleado, String(idEmpleado));
        }

        // 2) Resolver persona por idDatosPersonal
        for (const [idEmpleado, idDatosPersonal] of this.empleadoToDatos.entries()) {

          this.personasApi.obtenerPorId(idDatosPersonal).subscribe({
            next: (p: any) => {

              const documento = p?.documento ?? '';

              const nombres = p?.nombres ?? '';
              const pa = p?.primerApellido ?? p?.primer_apellido ?? '';
              const sa = p?.segundoApellido ?? p?.segundo_apellido ?? '';

              const nombre = `${pa} ${sa} ${nombres}`.replace(/\s+/g, ' ').trim();

              const label =
                documento && nombre
                  ? `${documento} — ${nombre}`
                  : (nombre || documento || String(idEmpleado));

              this.empleadosById.set(idEmpleado, label);
            },
            error: () => {
              // no hacemos nada, queda el fallback
            }
          });
        }
      },
      error: () => {
        this.empleados = [];
        this.empleadosById.clear();
        this.empleadoToDatos.clear();
      }
    });
  }

  // ✅ si el preview trae empleados que no estaban cargados, intentamos resolverlos igual
  private precargarEmpleadosDelPreview(): void {

    if (!this.preview?.length) return;

    for (const c of this.preview) {
      const idEmpleado = Number((c as any)?.contrato?.idEmpleado);
      if (!Number.isFinite(idEmpleado)) continue;

      // ya lo tengo
      if (this.empleadosById.has(idEmpleado) && this.empleadosById.get(idEmpleado) !== String(idEmpleado)) {
        continue;
      }

      const idDatosPersonal = this.empleadoToDatos.get(idEmpleado);
      if (!idDatosPersonal) continue;

      this.personasApi.obtenerPorId(idDatosPersonal).subscribe({
        next: (p: any) => {

          const documento = p?.documento ?? '';

          const nombres = p?.nombres ?? '';
          const pa = p?.primerApellido ?? p?.primer_apellido ?? '';
          const sa = p?.segundoApellido ?? p?.segundo_apellido ?? '';

          const nombre = `${pa} ${sa} ${nombres}`.replace(/\s+/g, ' ').trim();

          const label =
            documento && nombre
              ? `${documento} — ${nombre}`
              : (nombre || documento || String(idEmpleado));

          this.empleadosById.set(idEmpleado, label);
        }
      });
    }
  }

  empleadoLabel(idEmpleado: number | null | undefined): string {
    if (!idEmpleado) return '';
    return this.empleadosById.get(Number(idEmpleado)) ?? String(idEmpleado);
  }
}
