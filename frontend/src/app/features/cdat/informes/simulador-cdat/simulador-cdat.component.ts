import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  SimuladorAsociado,
  SimuladorBusquedaFiltros,
  SimuladorCdatModel,
  SimuladorFlujoItem,
  SimuladorResumen
} from './simulador-cdat.models';

import { SimuladorCdatApi } from './simulador-cdat.api';
import { NumericFormatDirective } from '../../../../shared/utils/numeric-format.directive';
import { SimuladorCdatPrintService } from './simulador-cdat-print.service';
import { SimuladorCdatExporterService } from './simulador-cdat-exporter.service';

@Component({
  selector: 'app-simulador-cdat',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './simulador-cdat.component.html',
  styleUrls: ['./simulador-cdat.component.scss']
})
export class SimuladorCdatComponent {

  paso = 1;

  cargandoBusqueda = false;
  errorBusqueda = '';

  agenciaActiva: any = null;

  resultados: SimuladorAsociado[] = [];

  persona: SimuladorAsociado | null = null;
  cuentaAportes: SimuladorAsociado | null = null;

  flujo: SimuladorFlujoItem[] = [];
  resumen: SimuladorResumen | null = null;

  filtros: SimuladorBusquedaFiltros = {
    documento: '',
    nombres: '',
    primer_apellido: '',
    segundo_apellido: ''
  };

  model: SimuladorCdatModel = {
    valorCdat: null,
    tasaNominalAnual: null,
    tasaEfectivaAnual: null,
    fechaApertura: new Date().toISOString().substring(0, 10),
    plazoMeses: null,
    fechaVencimiento: '',
    formaPagoInteres: 'MENSUAL',
    aplicaRetencion: true,
    baseRetencion: null,
    porcentajeRetencion: null
  };

  constructor(
    private api: SimuladorCdatApi,
    private printService: SimuladorCdatPrintService,
    private exporterService: SimuladorCdatExporterService
  ) {
    this.calcularFechaVencimiento();
  }

  buscar(): void {

    this.errorBusqueda = '';
    this.resultados = [];

    if (!this.filtrosActivos()) {
      this.errorBusqueda = 'Ingrese al menos un criterio de búsqueda.';
      return;
    }

    this.cargandoBusqueda = true;

    const agenciaCodigo =
      this.agenciaActiva?.codigo_agencia ||
      this.agenciaActiva?.codigoAgencia ||
      this.agenciaActiva?.codigo ||
      this.agenciaActiva?.id_agencia ||
      this.agenciaActiva?.idAgencia ||
      null;

    const req = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_total',
      filters: {
        documento: this.filtros.documento,
        nombres: this.filtros.nombres,
        primer_apellido: this.filtros.primer_apellido,
        segundo_apellido: this.filtros.segundo_apellido,
        codigo_agencia: agenciaCodigo,
        cuenta_activa: 'A'
      }
    };

    this.api.buscarCuentas(req).subscribe({
      next: (res: any) => {

        const data = res?.data ?? [];

        this.resultados = data.filter((c: any) => {

          const estado =
            String(c.codigo_estado ?? '')
              .trim()
              .toUpperCase();

          return estado === 'A';
        });

        this.resultados = this.resultados.sort((a: any, b: any) =>
          String(a.codigo_forma ?? '').localeCompare(String(b.codigo_forma ?? ''))
        );

        if (!this.resultados.length) {
          this.errorBusqueda = 'No se encontraron cuentas activas.';
        }
      },
      error: err => {
        this.errorBusqueda = err.error?.message || 'Error consultando cuentas.';
      },
      complete: () => {
        this.cargandoBusqueda = false;
      }
    });
  }

  limpiar(): void {

    this.filtros = {
      documento: '',
      nombres: '',
      primer_apellido: '',
      segundo_apellido: ''
    };

    this.resultados = [];
    this.errorBusqueda = '';
  }

  seleccionarPersona(persona: SimuladorAsociado): void {

    this.persona = persona;
    this.cuentaAportes = persona;

    this.flujo = [];
    this.resumen = null;

    this.paso = 2;
  }

  volver(): void {

    this.paso = 1;
    this.flujo = [];
    this.resumen = null;
  }

  volverListado(): void {
    history.back();
  }

  calcularTasaEfectivaAnual(): void {

    const tasaNominal = Number(this.model.tasaNominalAnual || 0);

    if (tasaNominal <= 0) {
      this.model.tasaEfectivaAnual = null;
      return;
    }

    const tasaMensual = tasaNominal / 100 / 12;
    const tasaEfectiva = (Math.pow(1 + tasaMensual, 12) - 1) * 100;

    this.model.tasaEfectivaAnual = Number(tasaEfectiva.toFixed(4));
  }

  calcularFechaVencimiento(): void {

    if (!this.model.fechaApertura || !this.model.plazoMeses) {
      this.model.fechaVencimiento = '';
      return;
    }

    const fecha = new Date(this.model.fechaApertura + 'T00:00:00');

    fecha.setMonth(fecha.getMonth() + Number(this.model.plazoMeses));

    this.model.fechaVencimiento = fecha.toISOString().substring(0, 10);
  }

  generarSimulacion(): void {
    this.generarFlujo();
  }

  generarFlujo(): void {

    this.flujo = [];
    this.resumen = null;

    this.calcularTasaEfectivaAnual();

    if (
      !this.persona ||
      !this.model.valorCdat ||
      !this.model.tasaNominalAnual ||
      !this.model.fechaApertura ||
      !this.model.plazoMeses
    ) {
      return;
    }

    const idAgencia =
      this.persona.id_agencia ||
      this.agenciaActiva?.id_agencia ||
      this.agenciaActiva?.idAgencia;

    if (!idAgencia) {
      this.errorBusqueda = 'No se pudo identificar la agencia para calcular la simulación.';
      return;
    }

    this.api.generarPreview({
      idAgencia,
      valorCdat: Number(this.model.valorCdat),
      tasaNominalAnual: Number(this.model.tasaNominalAnual),
      fechaApertura: this.model.fechaApertura,
      plazoMeses: Number(this.model.plazoMeses),
      formaPagoInteres: this.model.formaPagoInteres,
      aplicaRetencion: this.model.aplicaRetencion
    }).subscribe({
      next: preview => {

        this.model.baseRetencion = preview.baseRetencion;
        this.model.porcentajeRetencion = preview.porcentajeRetencion;
        this.model.fechaVencimiento = preview.fechaVencimiento;

        this.flujo = preview.items || [];

        this.resumen = {
          capital: preview.capital,
          totalInteresBruto: preview.totalInteresBruto,
          totalRetencion: preview.totalRetencion,
          totalInteresNeto: preview.totalInteresNeto,
          totalPagoCliente: preview.totalPagoCliente,
          valorAlVencimiento: preview.valorAlVencimiento
        };
      },
      error: () => {
        this.flujo = [];
        this.resumen = null;
        this.errorBusqueda = 'No fue posible generar la simulación del CDAT.';
      }
    });
  }

  filtrosActivos(): boolean {
    return !!(
      this.filtros.documento?.trim() ||
      this.filtros.nombres?.trim() ||
      this.filtros.primer_apellido?.trim() ||
      this.filtros.segundo_apellido?.trim()
    );
  }

  imprimir(): void {

    this.printService.imprimir(
      this.persona,
      this.model,
      this.flujo,
      this.resumen
    );
  }

  exportarExcel(): void {

    this.exporterService.exportar(
      this.persona,
      this.model,
      this.flujo,
      this.resumen
    );
  }

}
