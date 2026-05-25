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
import { PersonasApi } from '../../../../shared/personas/personas.api';

import {
  ExcelExportService
} from '../../../../shared/services/excel-export.service';

@Component({
  selector: 'app-liquidacion-preview',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './liquidacion-preview.component.html',
  styleUrls: ['./liquidacion-preview.component.scss']
})
export class LiquidacionPreviewComponent implements OnInit {

  form: FormGroup;

  cargando = false;
  error: string | null = null;

  periodos: PeriodoNominaListDTO[] = [];
  preview: LiquidacionPreviewContratoDTO[] = [];

  totalContratos = 0;
  totalDevengados = 0;
  totalDeducciones = 0;
  totalNeto = 0;

  empleados: EmpleadoListDTO[] = [];
  periodoLabel = 'Automático · Primer período ABIERTO';

  private empleadosById = new Map<number, string>();
  private empleadoToDatos = new Map<number, number>();

  constructor(
    private fb: FormBuilder,
    private api: LiquidacionApi,
    public session: SessionService,
    private periodosApi: PeriodosNominaApi,
    private empleadosApi: EmpleadosApi,
    private personasApi: PersonasApi,
    private excelExport: ExcelExportService
  ) {
    this.form = this.fb.group({
      fkAgencia: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.cargarPeriodos();
    this.cargarEmpleados();

    this.form.get('fkAgencia')!.valueChanges.subscribe(() => {
      this.limpiarPreview();
      this.cargarEmpleados();
    });
  }

  private cargarPeriodos(): void {
    this.periodosApi.listar().subscribe({
      next: d => this.periodos = d ?? [],
      error: () => this.periodos = []
    });
  }

  private limpiarPreview(): void {
    this.preview = [];
    this.totalContratos = 0;
    this.totalDevengados = 0;
    this.totalDeducciones = 0;
    this.totalNeto = 0;
    this.error = null;
    this.periodoLabel = 'Automático · Primer período ABIERTO';
  }

  ejecutarPreview(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dto: LiquidacionRequestDTO = {
      fkAgencia: this.form.value.fkAgencia
    };

    this.cargando = true;
    this.error = null;
    this.preview = [];

    this.api.previewPeriodo(dto).subscribe({
      next: data => {
        this.preview = data ?? [];
        this.calcularTotales();
        this.actualizarPeriodoLabel();
        this.precargarEmpleadosDelPreview();
        this.cargando = false;
      },
      error: err => {
        this.error =
          err?.error?.message ??
          'Error al generar preview de liquidación';
        this.cargando = false;
      }
    });
  }

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
      `Automático · ${p.anio}-${mes} ${q} ${tipo}`
        .replace(/\s+/g, ' ')
        .trim();
  }

  exportarExcel(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dto: LiquidacionRequestDTO = {
      fkAgencia: this.form.value.fkAgencia
    };

    this.cargando = true;

    this.api.previewPeriodoExcel(dto).subscribe({
      next: rows => {
        this.cargando = false;
        this.exportarAExcel(rows);
      },
      error: err => {
        this.cargando = false;
        console.error('Error exportando Excel', err);
        alert(
          err?.error?.message ??
          'Error exportando Excel'
        );
      }
    });
  }

  private exportarAExcel(
    rows: LiquidacionPreviewExcelDTO[]
  ): void {

    if (!rows || rows.length === 0) {
      alert('No hay datos para exportar.');
      return;
    }

    const idAgencia =
      this.form.get('fkAgencia')?.value ?? 'agencia';

    const idPeriodo =
      'auto';

    const columnas =
      Object.keys(rows[0] || {});

    this.excelExport.exportar({

      nombreArchivo:
        `preview_liquidacion_ag${idAgencia}_per${idPeriodo}.xlsx`,

      hojas: [

        {
          nombreHoja:
            'Preview Liquidación',

          titulo:
            'PREVIEW LIQUIDACIÓN NÓMINA',

          filtros: [
            ['Agencia', idAgencia],
            ['Período', idPeriodo]
          ],

          columnas,

          filas: rows.map(row =>
            columnas.map(c => (row as any)[c])
          ),

          anchos:
            columnas.map(() => 24)
        }

      ]

    });
  }

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

    const dto: LiquidacionRequestDTO = {
      fkAgencia: this.form.value.fkAgencia
    };

    this.cargando = true;
    this.error = null;

    this.api.ejecutarLiquidacion(dto).subscribe({
      next: () => {
        this.cargando = false;
        alert('Liquidación ejecutada correctamente');
        this.limpiarPreview();
      },
      error: err => {
        this.error =
          err?.error?.message ??
          'Error al ejecutar la liquidación';
        this.cargando = false;
      }
    });
  }

  private cargarEmpleados(): void {
    const idAgenciaSel =
      this.form.get('fkAgencia')?.value as number | null;

    this.empleadosApi.listar().subscribe({
      next: d => {
        const lista = d ?? [];

        const filtrados = idAgenciaSel
          ? lista.filter((e: any) => {
            const ag =
              e?.idAgencia ??
              e?.id_agencia ??
              e?.fkAgencia;

            return ag == null
              ? true
              : Number(ag) === Number(idAgenciaSel);
          })
          : lista;

        this.empleados = filtrados;

        this.empleadosById.clear();
        this.empleadoToDatos.clear();

        for (const e of this.empleados as any[]) {
          const idEmpleado =
            Number(e?.idEmpleado ?? e?.id_empleado);

          const idDatos =
            Number(e?.idDatosPersonal ?? e?.id_datos_personal);

          if (!Number.isFinite(idEmpleado)) continue;
          if (!Number.isFinite(idDatos)) continue;

          this.empleadoToDatos.set(idEmpleado, idDatos);
          this.empleadosById.set(idEmpleado, String(idEmpleado));
        }

        for (const [idEmpleado, idDatosPersonal] of this.empleadoToDatos.entries()) {
          this.personasApi.obtenerPorId(idDatosPersonal).subscribe({
            next: (p: any) => {
              const documento = p?.documento ?? '';
              const nombres = p?.nombres ?? '';
              const pa = p?.primerApellido ?? p?.primer_apellido ?? '';
              const sa = p?.segundoApellido ?? p?.segundo_apellido ?? '';

              const nombre =
                `${pa} ${sa} ${nombres}`
                  .replace(/\s+/g, ' ')
                  .trim();

              const label =
                documento && nombre
                  ? `${documento} — ${nombre}`
                  : (nombre || documento || String(idEmpleado));

              this.empleadosById.set(idEmpleado, label);
            },
            error: () => {
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

  private precargarEmpleadosDelPreview(): void {
    if (!this.preview?.length) return;

    for (const c of this.preview) {
      const idEmpleado =
        Number((c as any)?.contrato?.idEmpleado);

      if (!Number.isFinite(idEmpleado)) continue;

      if (
        this.empleadosById.has(idEmpleado)
        && this.empleadosById.get(idEmpleado) !== String(idEmpleado)
      ) {
        continue;
      }

      const idDatosPersonal =
        this.empleadoToDatos.get(idEmpleado);

      if (!idDatosPersonal) continue;

      this.personasApi.obtenerPorId(idDatosPersonal).subscribe({
        next: (p: any) => {
          const documento = p?.documento ?? '';
          const nombres = p?.nombres ?? '';
          const pa = p?.primerApellido ?? p?.primer_apellido ?? '';
          const sa = p?.segundoApellido ?? p?.segundo_apellido ?? '';

          const nombre =
            `${pa} ${sa} ${nombres}`
              .replace(/\s+/g, ' ')
              .trim();

          const label =
            documento && nombre
              ? `${documento} — ${nombre}`
              : (nombre || documento || String(idEmpleado));

          this.empleadosById.set(idEmpleado, label);
        }
      });
    }
  }

  empleadoLabel(
    idEmpleado: number | null | undefined
  ): string {

    if (!idEmpleado) {
      return '';
    }

    return this.empleadosById.get(Number(idEmpleado))
      ?? String(idEmpleado);
  }
}
