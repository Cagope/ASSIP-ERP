import { CommonModule } from '@angular/common';
import {
  Component,
  OnInit,
  inject
} from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  finalize,
  firstValueFrom
} from 'rxjs';

import {
  GeneralApi
} from '../../../../shared/general/general.api';

import {
  CarteraCatalogosApi
} from '../../../../shared/catalogos/cartera-catalogos.api';

import {
  CarteraCatalogo
} from '../../../../shared/catalogos/cartera-catalogos.models';

import {
  DashboardCarteraApi
} from './dashboard-cartera.api';

import {
  DashboardCarteraBarChartComponent
} from './dashboard-cartera-bar-chart.component';

import {
  DashboardCarteraDonutChartComponent
} from './dashboard-cartera-donut-chart.component';

import {
  DashboardCarteraResponse,
  DashboardCarteraFiltro,
  crearDashboardCarteraFiltroInicial,
  crearDashboardCarteraResponseVacio
} from './dashboard-cartera.models';

@Component({
  selector: 'app-dashboard-cartera',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    DashboardCarteraBarChartComponent,
    DashboardCarteraDonutChartComponent
  ],
  templateUrl: './dashboard-cartera.component.html',
  styleUrls: ['./dashboard-cartera.component.scss']
})
export class DashboardCarteraComponent implements OnInit {

  private readonly api =
    inject(DashboardCarteraApi);

  private readonly generalApi =
    inject(GeneralApi);

  private readonly carteraCatalogosApi =
    inject(CarteraCatalogosApi);

  // =========================================================
  // Catálogos
  // =========================================================

  /**
   * Se usa un nombre diferente de "agencias" porque ya existe
   * el getter agencias(), que representa los resultados del
   * dashboard agrupados por agencia.
   */
  agenciasFiltro: any[] = [];

  lineasCreditoFiltro: CarteraCatalogo[] = [];

  edadesRiesgoFiltro: CarteraCatalogo[] = [];

  clasificacionesCreditoFiltro: CarteraCatalogo[] = [];

  garantiasCreditoFiltro: CarteraCatalogo[] = [];

  estadosCarteraFiltro: CarteraCatalogo[] = [];

  estadosJuridicosFiltro: CarteraCatalogo[] = [];

  formasPagoFiltro: CarteraCatalogo[] = [];

  cargandoAgencias = false;

  cargandoCatalogosCartera = false;

  // =========================================================
  // Estado del dashboard
  // =========================================================

  filtro: DashboardCarteraFiltro =
    crearDashboardCarteraFiltroInicial();

  dashboard: DashboardCarteraResponse =
    crearDashboardCarteraResponseVacio();

  cargando = false;

  error = '';

  errorCatalogos = '';

  // =========================================================
  // Ciclo de vida
  // =========================================================

  ngOnInit(): void {

    void this.cargarAgencias();

    void this.cargarCatalogosCartera();

    this.consultar();

  }

  // =========================================================
  // Catálogo de agencias
  // =========================================================

  async cargarAgencias(): Promise<void> {

    this.cargandoAgencias = true;

    try {

      const respuesta =
        await firstValueFrom(
          this.generalApi.listarAgencias()
        );

      this.agenciasFiltro =
        respuesta ?? [];

    } catch (err) {

      console.error(
        'Error cargando agencias:',
        err
      );

      this.agenciasFiltro = [];

      if (!this.errorCatalogos) {
        this.errorCatalogos =
          'No fue posible cargar el catálogo de agencias.';
      }

    } finally {

      this.cargandoAgencias = false;

    }

  }

  // =========================================================
  // Catálogos de cartera
  // =========================================================

  async cargarCatalogosCartera(): Promise<void> {

    this.cargandoCatalogosCartera = true;

    try {

      const [
        lineasCredito,
        edadesRiesgo,
        clasificacionesCredito,
        garantiasCredito,
        estadosCartera,
        estadosJuridicos,
        formasPago
      ] = await Promise.all([

        firstValueFrom(
          this.carteraCatalogosApi
            .listarLineasCredito()
        ),

        firstValueFrom(
          this.carteraCatalogosApi
            .listarEdadesRiesgo()
        ),

        firstValueFrom(
          this.carteraCatalogosApi
            .listarClasificacionesCredito()
        ),

        firstValueFrom(
          this.carteraCatalogosApi
            .listarGarantiasCredito()
        ),

        firstValueFrom(
          this.carteraCatalogosApi
            .listarEstadosCartera()
        ),

        firstValueFrom(
          this.carteraCatalogosApi
            .listarEstadosJuridicos()
        ),

        firstValueFrom(
          this.carteraCatalogosApi
            .listarFormasPago()
        )

      ]);

      this.lineasCreditoFiltro =
        this.soloActivos(lineasCredito);

      this.edadesRiesgoFiltro =
        this.soloActivos(edadesRiesgo);

      this.clasificacionesCreditoFiltro =
        this.soloActivos(
          clasificacionesCredito
        );

      this.garantiasCreditoFiltro =
        this.soloActivos(garantiasCredito);

      this.estadosCarteraFiltro =
        this.soloActivos(estadosCartera);

      this.estadosJuridicosFiltro =
        this.soloActivos(estadosJuridicos);

      this.formasPagoFiltro =
        this.soloActivos(formasPago);

      this.errorCatalogos = '';

    } catch (err) {

      console.error(
        'Error cargando catálogos de cartera:',
        err
      );

      this.limpiarCatalogosCartera();

      this.errorCatalogos =
        'No fue posible cargar los catálogos de cartera.';

    } finally {

      this.cargandoCatalogosCartera = false;

    }

  }

  private soloActivos(
    catalogo: CarteraCatalogo[] | null | undefined
  ): CarteraCatalogo[] {

    return (catalogo ?? [])
      .filter(item => item.activo !== false);

  }

  private limpiarCatalogosCartera(): void {

    this.lineasCreditoFiltro = [];

    this.edadesRiesgoFiltro = [];

    this.clasificacionesCreditoFiltro = [];

    this.garantiasCreditoFiltro = [];

    this.estadosCarteraFiltro = [];

    this.estadosJuridicosFiltro = [];

    this.formasPagoFiltro = [];

  }

  // =========================================================
  // Eventos
  // =========================================================

  consultar(): void {

    this.error = '';
    this.cargando = true;

    this.api
      .consultar(this.filtro)
      .pipe(
        finalize(() =>
          this.cargando = false
        )
      )
      .subscribe({

        next: respuesta => {

          this.dashboard =
            respuesta
            ?? crearDashboardCarteraResponseVacio();

        },

        error: err => {

          console.error(err);

          this.dashboard =
            crearDashboardCarteraResponseVacio();

          this.error =
            err?.error?.message
            ??
            'No fue posible consultar el dashboard de cartera.';

        }

      });

  }

  limpiar(): void {

    this.filtro =
      crearDashboardCarteraFiltroInicial();

    this.consultar();

  }

  onChangeAgencia(): void {

    /*
     * Se consulta únicamente cuando el usuario pulse Buscar.
     * Así se evita ejecutar una petición por cada cambio
     * realizado en los filtros.
     */

  }

  // =========================================================
  // Accesos rápidos
  // =========================================================

  get resumen() {
    return this.dashboard.resumen;
  }

  get riesgos() {
    return this.dashboard.riesgos;
  }

  get moras() {
    return this.dashboard.moras;
  }

  get lineas() {
    return this.dashboard.lineas;
  }

  /**
   * Resultados del dashboard agrupados por agencia.
   * No confundir con agenciasFiltro, que alimenta el combo.
   */
  get agencias() {
    return this.dashboard.agencias;
  }

  get recaudos() {
    return this.dashboard.recaudos;
  }

  get alertas() {
    return this.dashboard.alertas;
  }

  // =========================================================
  // Formatos
  // =========================================================

  formatoMoneda(
    valor: number | null | undefined
  ): string {

    return new Intl.NumberFormat(
      'es-CO',
      {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }
    ).format(
      valor ?? 0
    );

  }

  formatoPorcentaje(
    valor: number | null | undefined
  ): string {

    return `${(valor ?? 0).toFixed(2)} %`;

  }

}
