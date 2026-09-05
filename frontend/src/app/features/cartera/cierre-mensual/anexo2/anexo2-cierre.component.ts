import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  CierreMensualCartera,
  CierreMensualCarteraApi
} from '../cierre-mensual-cartera.api';

import {
  Anexo2CierreApi,
  DetalleAnexo2,
  MoraAnexo2,
  ResultadoPreparacionAnexo2,
  ResumenAnexo2,
  TrabajoAnexo2
} from './anexo2-cierre.api';

import {
  forkJoin,
  map
} from 'rxjs';

import {
  Anexo2CierreExporterService,
  DatosExportacionAnexo2
} from './anexo2-cierre-exporter.service';

@Component({
  selector: 'app-anexo2-cierre',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './anexo2-cierre.component.html',

  styleUrls: [
    './anexo2-cierre.component.scss'
  ]
})
export class Anexo2CierreComponent implements OnInit {

  // =========================================================
  // API
  // =========================================================

  private readonly cierreApi =
    inject(CierreMensualCarteraApi);

  private readonly anexo2Api =
    inject(Anexo2CierreApi);

  private readonly exporter =
    inject(Anexo2CierreExporterService);

  // =========================================================
  // CIERRES
  // =========================================================

  cierres:
    CierreMensualCartera[] = [];

  idCierreSeleccionado:
    number | null = null;

  cierreSeleccionado:
    CierreMensualCartera | null = null;


  // =========================================================
  // MODELOS PE
  // =========================================================

  modelos:
    number[] = [];

  idModeloSeleccionado:
    number | null = null;


  // =========================================================
  // INFORMACIÓN CONSULTADA
  // =========================================================

  detalle:
    DetalleAnexo2[] = [];

  trabajo:
    TrabajoAnexo2[] = [];

  mora:
    MoraAnexo2[] = [];

  resumen:
    ResumenAnexo2[] = [];


  // =========================================================
  // RESULTADO DEL PROCESAMIENTO
  // =========================================================

  resultado:
    ResultadoPreparacionAnexo2 | null = null;


  // =========================================================
  // ESTADO DE PANTALLA
  // =========================================================

  cargando = false;

  procesando = false;

  consultando = false;

  cerrando = false;

  mensaje = '';

  error = '';

  exportandoExcel = false;

  // =========================================================
  // INIT
  // =========================================================

  ngOnInit(): void {

    this.cargarCierres();

  }


  // =========================================================
  // CARGAR CIERRES
  // =========================================================

  cargarCierres(): void {

    this.cargando = true;

    this.error = '';

    this.cierreApi
      .listar()
      .subscribe({

        next: (data) => {

          this.cierres =
            data ?? [];

          this.cargando =
            false;

        },

        error: (err) => {

          this.cargando =
            false;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible cargar los cierres mensuales.'
            );

        }

      });

  }


  // =========================================================
  // SELECCIONAR CIERRE
  // =========================================================

  seleccionarCierre(): void {

    this.limpiarMensajes();

    this.limpiarResultadoProceso();

    this.limpiarConsultas();

    this.modelos = [];

    this.idModeloSeleccionado =
      null;

    if (!this.idCierreSeleccionado) {

      this.cierreSeleccionado =
        null;

      return;

    }

    this.cierreSeleccionado =
      this.cierres.find(
        cierre =>
          Number(
            cierre.idCierreCartera
          )
          ===
          Number(
            this.idCierreSeleccionado
          )
      )
      ?? null;

    if (
      this.cierreSeleccionado
      &&
      this.anexo2Consultable()
    ) {

      this.cargarModelos();

    }

  }


  // =========================================================
  // ESTADOS DE ETAPAS
  // =========================================================

  fotografiaEnFirme(): boolean {

    return this.estadoEsC(
      this.cierreSeleccionado
        ?.estadoFotografia
    );

  }


  calculosEnFirme(): boolean {

    return this.estadoEsC(
      this.cierreSeleccionado
        ?.estadoCalculos
    );

  }


  anexo1EnFirme(): boolean {

    return this.estadoEsC(
      this.cierreSeleccionado
        ?.estadoAnexo1
    );

  }


  anexo2Pendiente(): boolean {

    return this.estadoEs(
      this.cierreSeleccionado
        ?.estadoAnexo2,
      'P'
    );

  }


  anexo2EnProceso(): boolean {

    return this.estadoEs(
      this.cierreSeleccionado
        ?.estadoAnexo2,
      'E'
    );

  }


  anexo2EnFirme(): boolean {

    return this.estadoEsC(
      this.cierreSeleccionado
        ?.estadoAnexo2
    );

  }


  dependenciasEnFirme(): boolean {

    return (
      this.fotografiaEnFirme()
      &&
      this.calculosEnFirme()
      &&
      this.anexo1EnFirme()
    );

  }


  anexo2Consultable(): boolean {

    return (
      this.dependenciasEnFirme()
      &&
      (
        this.anexo2EnProceso()
        ||
        this.anexo2EnFirme()
      )
    );

  }


  // =========================================================
  // PUEDE PROCESAR
  // =========================================================

  puedeProcesar(): boolean {

    return (
      !!this.idCierreSeleccionado
      &&
      !!this.cierreSeleccionado
      &&
      this.dependenciasEnFirme()
      &&
      !this.anexo2EnFirme()
      &&
      (
        this.anexo2Pendiente()
        ||
        this.anexo2EnProceso()
      )
      &&
      !this.procesando
      &&
      !this.consultando
      &&
      !this.cerrando
    );

  }


  // =========================================================
  // PUEDE CERRAR EN FIRME
  // =========================================================

  puedeCerrar(): boolean {

    return (
      !!this.idCierreSeleccionado
      &&
      !!this.cierreSeleccionado
      &&
      this.dependenciasEnFirme()
      &&
      this.anexo2EnProceso()
      &&
      this.modelos.length > 0
      &&
      !this.procesando
      &&
      !this.consultando
      &&
      !this.cerrando
    );

  }


  // =========================================================
  // PROCESAR ANEXO 2 / PE
  // =========================================================

  procesar(): void {

    this.limpiarMensajes();

    if (!this.idCierreSeleccionado) {

      this.error =
        'Debe seleccionar un cierre de cartera.';

      return;

    }

    if (!this.cierreSeleccionado) {

      this.error =
        'No fue posible identificar el cierre seleccionado.';

      return;

    }

    if (!this.fotografiaEnFirme()) {

      this.error =
        'La fotografía debe estar cerrada en firme antes de procesar Anexo 2.';

      return;

    }

    if (!this.calculosEnFirme()) {

      this.error =
        'Los cálculos deben estar cerrados en firme antes de procesar Anexo 2.';

      return;

    }

    if (!this.anexo1EnFirme()) {

      this.error =
        'El Anexo 1 debe estar cerrado en firme antes de procesar Anexo 2.';

      return;

    }

    if (this.anexo2EnFirme()) {

      this.error =
        'El Anexo 2 ya se encuentra cerrado en firme.';

      return;

    }

    if (this.procesando) {
      return;
    }

    const esRecalculo =
      this.anexo2EnProceso();

    const confirmar =
      window.confirm(
        (
          esRecalculo
            ? 'Se recalculará Anexo 2 / Pérdida Esperada para el cierre seleccionado'
            : 'Se ejecutará Anexo 2 / Pérdida Esperada para el cierre seleccionado'
        )
        + this.descripcionFechaConfirmacion()
        + '.\n\n'
        + 'El proceso calculará nuevamente:\n'
        + '- Población PE\n'
        + '- Vector histórico de 40 períodos\n'
        + '- Variables de los modelos\n'
        + '- Puntaje y calificación\n'
        + '- PI\n'
        + '- VEA\n'
        + '- PDI\n'
        + '- Pérdida esperada\n'
        + '- Homologación y alineación\n'
        + '- Deterioros PE\n\n'
        + '¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }

    this.procesando =
      true;

    this.resultado =
      null;

    this.limpiarConsultas();

    this.modelos =
      [];

    this.idModeloSeleccionado =
      null;

    this.anexo2Api
      .preparar(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (resultado) => {

          this.procesando =
            false;

          this.resultado =
            resultado ?? null;

          this.actualizarEstadoAnexo2Local(
            'E'
          );

          this.mensaje =
            'Anexo 2 / Pérdida Esperada procesado correctamente.';

          this.cargarModelos();

        },

        error: (err) => {

          this.procesando =
            false;

          this.resultado =
            null;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible procesar Anexo 2 / Pérdida Esperada.'
            );

        }

      });

  }


  // =========================================================
  // CERRAR ANEXO 2 EN FIRME
  // =========================================================

  cerrar(): void {

    this.limpiarMensajes();

    if (!this.idCierreSeleccionado) {

      this.error =
        'Debe seleccionar un cierre de cartera.';

      return;

    }

    if (!this.cierreSeleccionado) {

      this.error =
        'No fue posible identificar el cierre seleccionado.';

      return;

    }

    if (!this.anexo2EnProceso()) {

      this.error =
        'El Anexo 2 debe estar procesado antes de cerrarlo en firme.';

      return;

    }

    if (this.modelos.length === 0) {

      this.error =
        'No existen resultados PE para cerrar Anexo 2 en firme.';

      return;

    }

    if (this.cerrando) {
      return;
    }

    const confirmar =
      window.confirm(
        'Se cerrará Anexo 2 / Pérdida Esperada en firme'
        + this.descripcionFechaConfirmacion()
        + '.\n\n'
        + 'Después del cierre en firme no podrá recalcularse Anexo 2.\n\n'
        + '¿Desea continuar?'
      );

    if (!confirmar) {
      return;
    }

    this.cerrando =
      true;

    this.anexo2Api
      .cerrar(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: () => {

          this.cerrando =
            false;

          this.actualizarEstadoAnexo2Local(
            'C'
          );

          this.mensaje =
            'Anexo 2 / Pérdida Esperada cerrado en firme correctamente.';

        },

        error: (err) => {

          this.cerrando =
            false;

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible cerrar Anexo 2 en firme.'
            );

        }

      });

  }


  // =========================================================
  // CARGAR MODELOS DEL CIERRE
  // =========================================================

  cargarModelos(): void {

    if (!this.idCierreSeleccionado) {

      this.modelos =
        [];

      this.idModeloSeleccionado =
        null;

      return;

    }

    this.consultando =
      true;

    this.anexo2Api
      .obtenerModelos(
        this.idCierreSeleccionado
      )
      .subscribe({

        next: (data) => {

          this.consultando =
            false;

          this.modelos =
            data ?? [];

          if (this.modelos.length > 0) {

            this.idModeloSeleccionado =
              this.modelos[0];

            this.consultarModelo();

          } else {

            this.idModeloSeleccionado =
              null;

            this.limpiarConsultas();

          }

        },

        error: () => {

          this.consultando =
            false;

          this.modelos =
            [];

          this.idModeloSeleccionado =
            null;

        }

      });

  }


  // =========================================================
  // SELECCIONAR MODELO
  // =========================================================

  seleccionarModelo(): void {

    this.limpiarMensajes();

    this.limpiarConsultas();

    if (!this.idModeloSeleccionado) {
      return;
    }

    this.consultarModelo();

  }


  // =========================================================
  // CONSULTAR MODELO
  // =========================================================

  consultarModelo(): void {

    if (
      !this.idCierreSeleccionado
      ||
      !this.idModeloSeleccionado
    ) {

      return;

    }

    this.limpiarConsultas();

    this.consultando =
      true;

    this.cargarResumen();

  }


  // =========================================================
  // CARGAR RESUMEN
  // =========================================================

  private cargarResumen(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }

    this.anexo2Api
      .obtenerResumen(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.resumen =
            data ?? [];

          this.cargarDetalle();

        },

        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar el resumen del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // CARGAR RESULTADO
  // =========================================================

  private cargarDetalle(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }

    this.anexo2Api
      .obtenerDetalle(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.detalle =
            data ?? [];

          this.cargarTrabajo();

        },

        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar el resultado del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // CARGAR HOJA DE TRABAJO
  // =========================================================

  private cargarTrabajo(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }

    this.anexo2Api
      .obtenerTrabajo(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.trabajo =
            data ?? [];

          this.cargarMora();

        },

        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar la hoja de trabajo del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // CARGAR MORA
  // =========================================================

  private cargarMora(): void {

    const parametros =
      this.obtenerParametrosConsulta();

    if (!parametros) {

      this.consultando =
        false;

      return;

    }

    this.anexo2Api
      .obtenerMora(
        parametros.idCierreCartera,
        parametros.idModeloPe
      )
      .subscribe({

        next: (data) => {

          this.mora =
            data ?? [];

          this.consultando =
            false;

        },

        error: (err) => {

          this.finalizarConsultaConError(
            err,
            'No fue posible consultar el histórico de mora del modelo PE.'
          );

        }

      });

  }


  // =========================================================
  // PARÁMETROS DE CONSULTA
  // =========================================================

  private obtenerParametrosConsulta():
    {
      idCierreCartera: number;
      idModeloPe: number;
    }
    | null {

    if (
      !this.idCierreSeleccionado
      ||
      !this.idModeloSeleccionado
    ) {

      return null;

    }

    return {
      idCierreCartera:
        this.idCierreSeleccionado,

      idModeloPe:
        this.idModeloSeleccionado
    };

  }


  // =========================================================
  // FINALIZAR CONSULTA CON ERROR
  // =========================================================

  private finalizarConsultaConError(
    err: any,
    mensajeDefault: string
  ): void {

    this.consultando =
      false;

    this.error =
      this.obtenerMensajeError(
        err,
        mensajeDefault
      );

  }


  // =========================================================
  // TIENE RESULTADO DE PROCESAMIENTO
  // =========================================================

  tieneResultado(): boolean {

    return this.resultado
      !== null;

  }


  // =========================================================
  // TIENE MODELOS
  // =========================================================

  tieneModelos(): boolean {

    return this.modelos.length
      > 0;

  }


  // =========================================================
  // TIENE INFORMACIÓN DEL MODELO
  // =========================================================

  tieneInformacionModelo(): boolean {

    return (
      this.detalle.length > 0
      ||
      this.trabajo.length > 0
      ||
      this.mora.length > 0
      ||
      this.resumen.length > 0
    );

  }


  // =========================================================
  // NOMBRE MODELO SELECCIONADO
  // =========================================================

  nombreModeloSeleccionado(): string {

    if (this.detalle.length > 0) {

      return this.detalle[0]
        .nombreModeloPe
        ?? '';

    }

    if (this.resumen.length > 0) {

      return this.resumen[0]
        .nombreModeloPe
        ?? '';

    }

    if (this.trabajo.length > 0) {

      return this.trabajo[0]
        .nombreModeloPe
        ?? '';

    }

    return this.idModeloSeleccionado
      ? `Modelo ${this.idModeloSeleccionado}`
      : '';

  }


  // =========================================================
  // DESCRIPCIÓN DEL ESTADO
  // =========================================================

  descripcionEstado(
    estado:
      string | null | undefined
  ): string {

    switch (
      (estado ?? '')
        .trim()
        .toUpperCase()
    ) {

      case 'P':
        return 'Pendiente';

      case 'E':
        return 'En proceso';

      case 'C':
        return 'Cerrado';

      default:
        return estado ?? '';

    }

  }


  // =========================================================
  // ACTUALIZAR ESTADO LOCAL ANEXO 2
  // =========================================================

  private actualizarEstadoAnexo2Local(
    estado: string
  ): void {

    if (!this.cierreSeleccionado) {
      return;
    }

    this.cierreSeleccionado.estadoAnexo2 =
      estado;

    const cierreLista =
      this.cierres.find(
        cierre =>
          Number(cierre.idCierreCartera)
          ===
          Number(this.cierreSeleccionado?.idCierreCartera)
      );

    if (cierreLista) {

      cierreLista.estadoAnexo2 =
        estado;

    }

  }


  // =========================================================
  // COMPARACIÓN DE ESTADOS
  // =========================================================

  private estadoEsC(
    estado:
      string | null | undefined
  ): boolean {

    return this.estadoEs(
      estado,
      'C'
    );

  }


  private estadoEs(
    estado:
      string | null | undefined,
    esperado: string
  ): boolean {

    return (
      estado ?? ''
    )
      .trim()
      .toUpperCase()
      === esperado;

  }


  // =========================================================
  // FECHA PARA CONFIRMACIÓN
  // =========================================================

  private descripcionFechaConfirmacion(): string {

    const fecha =
      this.cierreSeleccionado
        ?.fechaCorte;

    if (!fecha) {
      return '';
    }

    return ` (${fecha})`;

  }


  // =========================================================
  // LIMPIAR CONSULTAS
  // =========================================================

  private limpiarConsultas(): void {

    this.detalle =
      [];

    this.trabajo =
      [];

    this.mora =
      [];

    this.resumen =
      [];

  }


  // =========================================================
  // LIMPIAR RESULTADO DEL PROCESO
  // =========================================================

  private limpiarResultadoProceso(): void {

    this.resultado =
      null;

  }


  // =========================================================
  // LIMPIAR MENSAJES
  // =========================================================

  private limpiarMensajes(): void {

    this.mensaje =
      '';

    this.error =
      '';

  }


  // =========================================================
  // MENSAJE DE ERROR
  // =========================================================

  private obtenerMensajeError(
    err: any,
    mensajeDefault: string
  ): string {

    if (
      typeof err?.error
      === 'string'
      &&
      err.error.trim()
    ) {

      return err.error;

    }

    if (
      typeof err?.error?.message
      === 'string'
      &&
      err.error.message.trim()
    ) {

      return err.error.message;

    }

    if (
      typeof err?.message
      === 'string'
      &&
      err.message.trim()
    ) {

      return err.message;

    }

    return mensajeDefault;

  }

  // =========================================================
  // PUEDE GENERAR EXCEL
  // =========================================================

  puedeExportarExcel(): boolean {

    return !!this.cierreSeleccionado
      && this.anexo2Consultable()
      && this.modelos.length > 0
      && !this.cargando
      && !this.procesando
      && !this.consultando
      && !this.cerrando
      && !this.exportandoExcel;

  }


  // =========================================================
  // GENERAR EXCEL
  //
  // Genera un archivo independiente por cada modelo PE.
  //
  // Cada archivo contiene:
  //
  // - RESUMEN
  // - RESULTADO
  // - HOJA_TRABAJO
  // - MORA
  //
  // IMPORTANTE:
  //
  // No depende del modelo seleccionado en pantalla.
  // Exporta todos los modelos existentes en el cierre.
  // =========================================================

  exportarExcel(): void {

    if (
      !this.cierreSeleccionado
      || !this.idCierreSeleccionado
    ) {

      this.error =
        'Debe seleccionar un cierre mensual.';

      return;
    }


    if (!this.anexo2Consultable()) {

      this.error =
        'Anexo 2 debe estar procesado antes de generar el Excel.';

      return;
    }


    if (!this.modelos.length) {

      this.error =
        'No existen modelos PE para exportar.';

      return;
    }


    if (
      this.procesando
      || this.consultando
      || this.cerrando
      || this.exportandoExcel
    ) {
      return;
    }


    const idCierreCartera =
      Number(
        this.idCierreSeleccionado
      );


    const fechaCorte =
      this.cierreSeleccionado.fechaCorte;


    this.exportandoExcel =
      true;

    this.error =
      '';

    this.mensaje =
      'Generando archivos Excel de Anexo 2...';


    // =======================================================
    // CONSULTAR TODOS LOS MODELOS
    // =======================================================

    const consultas =
      this.modelos.map(
        idModeloPe =>

          forkJoin({

            resumen:
              this.anexo2Api.obtenerResumen(
                idCierreCartera,
                idModeloPe
              ),

            detalle:
              this.anexo2Api.obtenerDetalle(
                idCierreCartera,
                idModeloPe
              ),

            trabajo:
              this.anexo2Api.obtenerTrabajo(
                idCierreCartera,
                idModeloPe
              ),

            mora:
              this.anexo2Api.obtenerMora(
                idCierreCartera,
                idModeloPe
              )

          }).pipe(

            map(
              datos => {

                const salida:
                  DatosExportacionAnexo2 = {

                    idModeloPe,

                    fechaCorte,

                    resumen:
                      datos.resumen ?? [],

                    detalle:
                      datos.detalle ?? [],

                    trabajo:
                      datos.trabajo ?? [],

                    mora:
                      datos.mora ?? []

                  };


                return salida;

              }
            )

          )

      );


    // =======================================================
    // ESPERAR TODOS LOS MODELOS
    // =======================================================

    forkJoin(
      consultas
    )
      .subscribe({

        next: (
          datosModelos:
            DatosExportacionAnexo2[]
        ) => {

          // =================================================
          // GENERAR UN XLSX POR MODELO
          // =================================================

          for (
            const datos of datosModelos
          ) {

            this.exporter.exportarModelo(
              datos
            );

          }


          this.exportandoExcel =
            false;


          this.mensaje =
            datosModelos.length === 1
              ? 'Se generó 1 archivo Excel de Anexo 2.'
              : `Se generaron ${datosModelos.length} archivos Excel de Anexo 2.`;

        },


        error: (err) => {

          this.exportandoExcel =
            false;

          this.mensaje =
            '';

          this.error =
            this.obtenerMensajeError(
              err,
              'No fue posible generar los archivos Excel de Anexo 2.'
            );

        }

      });

  }

}
