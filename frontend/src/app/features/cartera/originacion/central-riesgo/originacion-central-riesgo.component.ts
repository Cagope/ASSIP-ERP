import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  forkJoin
} from 'rxjs';

import {
  OriginacionSolicitudStateService
} from '../solicitud/originacion-solicitud-state.service';

import {
  CentralRiesgoCatalogo,
  SolicitudCentralRiesgo,
  SolicitudCentralRiesgoDetalle,
  SolicitudCentralRiesgoGuardarRequest
} from './originacion-central-riesgo.models';

import {
  OriginacionCentralRiesgoApi
} from './originacion-central-riesgo.api';

import {
  NumericFormatDirective
} from '../../../../shared/utils/numeric-format.directive';

@Component({
  selector: 'app-originacion-central-riesgo',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './originacion-central-riesgo.component.html',
  styleUrl: './originacion-central-riesgo.component.scss'
})
export class OriginacionCentralRiesgoComponent implements OnInit {

  // =========================================================
  // CONTEXTO
  // =========================================================

  idSolicitudCredito: number | null = null;

  cargando = false;
  cargandoDetalle = false;
  guardando = false;
  cargandoCatalogos = false;

  mensajeError = '';
  mensajeExito = '';


  // =========================================================
  // DEUDORES / INFORMACIÓN REGISTRADA
  // =========================================================

  registros: SolicitudCentralRiesgo[] = [];

  registroSeleccionado:
    SolicitudCentralRiesgo | null = null;

  detalleActual:
    SolicitudCentralRiesgoDetalle | null = null;


  // =========================================================
  // CATÁLOGO
  // =========================================================

  centralesRiesgo:
    CentralRiesgoCatalogo[] = [];


  // =========================================================
  // FORMULARIO
  // =========================================================

  formulario:
    SolicitudCentralRiesgoGuardarRequest =
      this.crearFormularioVacio();


  // =========================================================
  // CONSTRUCTOR
  // =========================================================

  constructor(
    private readonly router: Router,
    private readonly api:
      OriginacionCentralRiesgoApi,
    private readonly state:
      OriginacionSolicitudStateService
  ) {}


  // =========================================================
  // INICIO
  // =========================================================

  ngOnInit(): void {

    this.idSolicitudCredito =
      this.state.getIdSolicitudCredito();

    if (!this.idSolicitudCredito) {

      this.mensajeError =
        'No fue posible identificar la solicitud de crédito.';

      return;
    }

    this.cargarPantalla();
  }


  // =========================================================
  // CARGA INICIAL
  // =========================================================

  private cargarPantalla(): void {

    if (!this.idSolicitudCredito) {
      return;
    }

    this.cargando = true;
    this.cargandoCatalogos = true;

    this.limpiarMensajes();

    forkJoin({

      registros:
        this.api.listarPorSolicitud(
          this.idSolicitudCredito
        ),

      centrales:
        this.api.listarCentralesRiesgo()

    }).subscribe({

      next: ({
        registros,
        centrales
      }) => {

        this.registros =
          registros ?? [];

        this.centralesRiesgo =
          centrales ?? [];

        this.cargando = false;
        this.cargandoCatalogos = false;

        if (this.registros.length > 0) {

          this.seleccionarRegistro(
            this.registros[0]
          );
        }
      },

      error: (error) => {

        this.cargando = false;
        this.cargandoCatalogos = false;

        this.mensajeError =
          this.obtenerMensajeError(
            error,
            'No fue posible cargar la información de central de riesgo.'
          );
      }
    });
  }


  // =========================================================
  // SELECCIONAR DEUDOR / REGISTRO
  // =========================================================

  seleccionarRegistro(
    registro: SolicitudCentralRiesgo
  ): void {

    if (this.guardando) {
      return;
    }

    this.registroSeleccionado =
      registro;

    this.detalleActual =
      null;

    this.limpiarMensajes();

    /*
     * Si ya existe registro de central de riesgo,
     * cargamos su detalle.
     */
    if (registro.idSolicitudDeudorCentral) {

      this.cargarDetalle(
        registro.idSolicitudDeudorCentral
      );

      return;
    }

    /*
     * Deudor sin información registrada.
     */
    this.prepararNuevoRegistro(
      registro
    );
  }


  // =========================================================
  // CARGAR DETALLE
  // =========================================================

  private cargarDetalle(
    idSolicitudDeudorCentral: number
  ): void {

    this.cargandoDetalle = true;

    this.api.buscarPorId(
      idSolicitudDeudorCentral
    ).subscribe({

      next: (detalle) => {

        this.detalleActual =
          detalle;

        this.cargarFormularioDesdeDetalle(
          detalle
        );

        this.cargandoDetalle = false;
      },

      error: (error) => {

        this.cargandoDetalle = false;

        this.mensajeError =
          this.obtenerMensajeError(
            error,
            'No fue posible consultar el detalle de la central de riesgo.'
          );
      }
    });
  }


  // =========================================================
  // NUEVO REGISTRO
  // =========================================================

  private prepararNuevoRegistro(
    registro: SolicitudCentralRiesgo
  ): void {

    this.formulario =
      this.crearFormularioVacio(
        registro.idSolicitudDeudor
      );
  }


  // =========================================================
  // CARGAR FORMULARIO
  // =========================================================

  private cargarFormularioDesdeDetalle(
    detalle: SolicitudCentralRiesgoDetalle
  ): void {

    this.formulario = {

      idSolicitudDeudor:
        detalle.idSolicitudDeudor,

      idCentralRiesgo:
        detalle.idCentralRiesgo,

      fechaConsulta:
        detalle.fechaConsulta ?? '',

      valorInicialObligaciones:
        this.numero(
          detalle.valorInicialObligaciones
        ),

      saldoActualObligaciones:
        this.numero(
          detalle.saldoActualObligaciones
        ),

      valorCuotasMensuales:
        this.numero(
          detalle.valorCuotasMensuales
        ),

      cantidadCalificacionA:
        this.entero(
          detalle.cantidadCalificacionA
        ),

      cantidadCalificacionB:
        this.entero(
          detalle.cantidadCalificacionB
        ),

      cantidadCalificacionC:
        this.entero(
          detalle.cantidadCalificacionC
        ),

      cantidadCalificacionD:
        this.entero(
          detalle.cantidadCalificacionD
        ),

      cantidadCalificacionE:
        this.entero(
          detalle.cantidadCalificacionE
        ),

      cantidadCalificacionK:
        this.entero(
          detalle.cantidadCalificacionK
        ),

      cantidadReestructuraciones:
        this.entero(
          detalle.cantidadReestructuraciones
        ),

      cantidadRefinanciaciones:
        this.entero(
          detalle.cantidadRefinanciaciones
        ),

      cantidadCuentasEmbargadas:
        this.entero(
          detalle.cantidadCuentasEmbargadas
        ),

      puntajeCentral:
        detalle.calificacionCentral ===
        'SIN_HISTORIAL'
          ? null
          : this.numeroNullable(
              detalle.puntajeCentral
            ),

      calificacionCentral:
        detalle.calificacionCentral ?? '',

      calificacionCualitativa:
        detalle.calificacionCentral ===
        'SIN_HISTORIAL'
          ? 'SIN HISTORIAL'
          : detalle.calificacionCualitativa ?? '',

      observacion:
        detalle.observacion ?? ''
    };
  }


  // =========================================================
  // GUARDAR
  // =========================================================

  guardar(): void {

    this.limpiarMensajes();

    const errorValidacion =
      this.validarFormulario();

    if (errorValidacion) {

      this.mensajeError =
        errorValidacion;

      return;
    }

    const request =
      this.construirRequest();

    this.guardando = true;

    this.api.guardar(
      request
    ).subscribe({

      next: (detalle) => {

        this.guardando = false;

        this.detalleActual =
          detalle;

        this.cargarFormularioDesdeDetalle(
          detalle
        );

        this.actualizarRegistroLocal(
          detalle
        );

        this.mensajeExito =
          'La información de la central de riesgo fue guardada correctamente.';
      },

      error: (error) => {

        this.guardando = false;

        this.mensajeError =
          this.obtenerMensajeError(
            error,
            'No fue posible guardar la información de la central de riesgo.'
          );
      }
    });
  }


  // =========================================================
  // CONSTRUIR REQUEST
  // =========================================================

  private construirRequest():
    SolicitudCentralRiesgoGuardarRequest {

    return {

      idSolicitudDeudor:
        this.formulario.idSolicitudDeudor,

      idCentralRiesgo:
        Number(
          this.formulario.idCentralRiesgo
        ),

      fechaConsulta:
        this.formulario.fechaConsulta,

      valorInicialObligaciones:
        this.numero(
          this.formulario
            .valorInicialObligaciones
        ),

      saldoActualObligaciones:
        this.numero(
          this.formulario
            .saldoActualObligaciones
        ),

      valorCuotasMensuales:
        this.numero(
          this.formulario
            .valorCuotasMensuales
        ),

      cantidadCalificacionA:
        this.entero(
          this.formulario
            .cantidadCalificacionA
        ),

      cantidadCalificacionB:
        this.entero(
          this.formulario
            .cantidadCalificacionB
        ),

      cantidadCalificacionC:
        this.entero(
          this.formulario
            .cantidadCalificacionC
        ),

      cantidadCalificacionD:
        this.entero(
          this.formulario
            .cantidadCalificacionD
        ),

      cantidadCalificacionE:
        this.entero(
          this.formulario
            .cantidadCalificacionE
        ),

      cantidadCalificacionK:
        this.entero(
          this.formulario
            .cantidadCalificacionK
        ),

      cantidadReestructuraciones:
        this.entero(
          this.formulario
            .cantidadReestructuraciones
        ),

      cantidadRefinanciaciones:
        this.entero(
          this.formulario
            .cantidadRefinanciaciones
        ),

      cantidadCuentasEmbargadas:
        this.entero(
          this.formulario
            .cantidadCuentasEmbargadas
        ),

      puntajeCentral:
        this.formulario.calificacionCentral ===
        'SIN_HISTORIAL'
          ? null
          : this.numeroNullable(
              this.formulario.puntajeCentral
            ),

      calificacionCentral:
        this.textoONull(
          this.formulario
            .calificacionCentral
        ),

      calificacionCualitativa:
        this.formulario.calificacionCentral ===
        'SIN_HISTORIAL'
          ? 'SIN HISTORIAL'
          : this.textoONull(
              this.formulario
                .calificacionCualitativa
            ),

      observacion:
        this.textoONull(
          this.formulario.observacion
        )
    };
  }


  // =========================================================
  // ESTADO CONSULTA CENTRAL
  // =========================================================

  cambiarEstadoConsulta(
    estado: string | null
  ): void {

    if (estado === 'SIN_HISTORIAL') {

      this.formulario.puntajeCentral =
        null;

      this.formulario.calificacionCualitativa =
        'SIN HISTORIAL';

      return;
    }

    if (
      estado === 'CON_HISTORIAL' &&
      this.formulario.calificacionCualitativa ===
      'SIN HISTORIAL'
    ) {

      this.formulario.calificacionCualitativa =
        '';
    }
  }


  // =========================================================
  // VALIDACIONES
  // =========================================================

  private validarFormulario():
    string | null {

    if (!this.formulario.idSolicitudDeudor) {

      return 'Debe seleccionar un deudor.';
    }

    if (!this.formulario.idCentralRiesgo) {

      return 'Debe seleccionar la central de riesgo.';
    }

    if (!this.formulario.fechaConsulta) {

      return 'La fecha de consulta es obligatoria.';
    }

    const fechaConsulta =
      new Date(
        `${this.formulario.fechaConsulta}T00:00:00`
      );

    const hoy =
      new Date();

    hoy.setHours(
      0,
      0,
      0,
      0
    );

    if (
      fechaConsulta.getTime() >
      hoy.getTime()
    ) {

      return 'La fecha de consulta no puede ser futura.';
    }


    // ---------------------------------------------------------
    // Resultado central
    // ---------------------------------------------------------

    if (
      !this.formulario
        .calificacionCentral
    ) {

      return 'Debe seleccionar el estado de la consulta.';
    }

    if (
      this.formulario.calificacionCentral ===
      'CON_HISTORIAL'
    ) {

      if (
        this.formulario.puntajeCentral == null ||
        this.formulario.puntajeCentral ===
        ('' as unknown)
      ) {

        return 'Debe registrar el puntaje de la central cuando existe historial.';
      }

      const calificacionCualitativa =
        this.formulario
          .calificacionCualitativa
          ?.trim();

      if (!calificacionCualitativa) {

        return 'Debe seleccionar la calificación cualitativa de la central de riesgo.';
      }

      const calificacionesValidas = [
        'CON HISTORIAL - OK',
        'CON PERMANENCIAS',
        'CON REPORTES'
      ];

      if (
        !calificacionesValidas.includes(
          calificacionCualitativa
        )
      ) {

        return 'La calificación cualitativa seleccionada no es válida.';
      }
    }


    // ---------------------------------------------------------
    // Valores monetarios
    // ---------------------------------------------------------

    const monetarios: Array<{
      valor: number | null;
      nombre: string;
    }> = [

      {
        valor:
          this.formulario
            .valorInicialObligaciones,
        nombre:
          'Valor inicial de obligaciones'
      },

      {
        valor:
          this.formulario
            .saldoActualObligaciones,
        nombre:
          'Saldo actual de obligaciones'
      },

      {
        valor:
          this.formulario
            .valorCuotasMensuales,
        nombre:
          'Valor de cuotas mensuales'
      },

      {
        valor:
          this.formulario
            .puntajeCentral,
        nombre:
          'Puntaje de la central'
      }
    ];

    for (
      const campo of monetarios
    ) {

      if (
        campo.valor != null &&
        Number(campo.valor) < 0
      ) {

        return `${campo.nombre} no puede ser negativo.`;
      }
    }


    // ---------------------------------------------------------
    // Cantidades
    // ---------------------------------------------------------

    const cantidades: Array<{
      valor: number | null;
      nombre: string;
    }> = [

      {
        valor:
          this.formulario
            .cantidadCalificacionA,
        nombre:
          'Cantidad calificación A'
      },

      {
        valor:
          this.formulario
            .cantidadCalificacionB,
        nombre:
          'Cantidad calificación B'
      },

      {
        valor:
          this.formulario
            .cantidadCalificacionC,
        nombre:
          'Cantidad calificación C'
      },

      {
        valor:
          this.formulario
            .cantidadCalificacionD,
        nombre:
          'Cantidad calificación D'
      },

      {
        valor:
          this.formulario
            .cantidadCalificacionE,
        nombre:
          'Cantidad calificación E'
      },

      {
        valor:
          this.formulario
            .cantidadCalificacionK,
        nombre:
          'Cantidad calificación K'
      },

      {
        valor:
          this.formulario
            .cantidadReestructuraciones,
        nombre:
          'Cantidad de reestructuraciones'
      },

      {
        valor:
          this.formulario
            .cantidadRefinanciaciones,
        nombre:
          'Cantidad de refinanciaciones'
      },

      {
        valor:
          this.formulario
            .cantidadCuentasEmbargadas,
        nombre:
          'Cantidad de cuentas embargadas'
      }
    ];

    for (
      const campo of cantidades
    ) {

      if (
        campo.valor != null &&
        (
          Number(campo.valor) < 0 ||
          !Number.isInteger(
            Number(campo.valor)
          )
        )
      ) {

        return `${campo.nombre} debe ser un número entero no negativo.`;
      }
    }

    return null;
  }


  // =========================================================
  // ACTUALIZAR RESUMEN LOCAL
  // =========================================================

  private actualizarRegistroLocal(
    detalle: SolicitudCentralRiesgoDetalle
  ): void {

    const registro:
      SolicitudCentralRiesgo = {

      idSolicitudDeudorCentral:
        detalle.idSolicitudDeudorCentral,

      idSolicitudDeudor:
        detalle.idSolicitudDeudor,

      idSolicitudCredito:
        detalle.idSolicitudCredito,

      idDatosPersonal:
        detalle.idDatosPersonal,

      tipoDocumento:
        detalle.tipoDocumento,

      documento:
        detalle.documento,

      nombreCompleto:
        detalle.nombreCompleto,

      tipoDeudor:
        detalle.tipoDeudor,

      ordenDeudor:
        detalle.ordenDeudor,

      idCentralRiesgo:
        detalle.idCentralRiesgo,

      codigoCentral:
        detalle.codigoCentral,

      nombreCentral:
        detalle.nombreCentral,

      fechaConsulta:
        detalle.fechaConsulta,

      fechaFotografia:
        detalle.fechaFotografia,

      saldoActualObligaciones:
        detalle.saldoActualObligaciones,

      valorCuotasMensuales:
        detalle.valorCuotasMensuales,

      puntajeCentral:
        detalle.puntajeCentral,

      calificacionCentral:
        detalle.calificacionCentral,

      calificacionCualitativa:
        detalle.calificacionCualitativa,

      activo:
        detalle.activo
    };

    const indice =
      this.registros.findIndex(
        item =>
          item.idSolicitudDeudor ===
          detalle.idSolicitudDeudor
      );

    if (indice >= 0) {

      this.registros =
        this.registros.map(
          (item, index) =>
            index === indice
              ? registro
              : item
        );

    } else {

      this.registros = [
        ...this.registros,
        registro
      ];
    }

    this.registroSeleccionado =
      registro;
  }


  // =========================================================
  // ESTADO UI
  // =========================================================

  get tieneRegistro(): boolean {

    return !!this.detalleActual
      ?.idSolicitudDeudorCentral;
  }

  get puedeContinuar(): boolean {

    return (
      this.registros.length > 0 &&
      this.registros.every(
        registro =>
          !!registro.idSolicitudDeudorCentral
      )
    );
  }


  get centralSeleccionada():
    CentralRiesgoCatalogo | null {

    const idCentral =
      Number(
        this.formulario
          .idCentralRiesgo
      );

    if (!idCentral) {
      return null;
    }

    return this.centralesRiesgo.find(
      central =>
        central.idCentralRiesgo ===
        idCentral
    ) ?? null;
  }


  get fechaMaximaConsulta(): string {

    const hoy =
      new Date();

    const anio =
      hoy.getFullYear();

    const mes =
      String(
        hoy.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const dia =
      String(
        hoy.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${anio}-${mes}-${dia}`;
  }


  // =========================================================
  // NAVEGACIÓN
  // =========================================================

  volver(): void {

    this.router.navigate([
      '/cartera/originacion/financiero'
    ]);
  }


  continuar(): void {

    this.router.navigate([
      '/cartera/originacion/analisis'
    ]);
  }


  // =========================================================
  // FORMULARIO VACÍO
  // =========================================================

  private crearFormularioVacio(
    idSolicitudDeudor = 0
  ): SolicitudCentralRiesgoGuardarRequest {

    return {

      idSolicitudDeudor,

      idCentralRiesgo: 0,

      fechaConsulta:
        this.fechaMaximaConsulta,

      valorInicialObligaciones: 0,
      saldoActualObligaciones: 0,
      valorCuotasMensuales: 0,

      cantidadCalificacionA: 0,
      cantidadCalificacionB: 0,
      cantidadCalificacionC: 0,
      cantidadCalificacionD: 0,
      cantidadCalificacionE: 0,
      cantidadCalificacionK: 0,

      cantidadReestructuraciones: 0,
      cantidadRefinanciaciones: 0,
      cantidadCuentasEmbargadas: 0,

      puntajeCentral: null,

      calificacionCentral: '',

      calificacionCualitativa: '',

      observacion: ''
    };
  }


  // =========================================================
  // SOPORTE
  // =========================================================

  private numeroNullable(
    valor:
      number |
      null |
      undefined
  ): number | null {

    if (
      valor == null ||
      valor === ('' as unknown)
    ) {

      return null;
    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : null;
  }


  private numero(
    valor:
      number |
      null |
      undefined
  ): number {

    if (
      valor == null ||
      valor === ('' as unknown)
    ) {

      return 0;
    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }


  private entero(
    valor:
      number |
      null |
      undefined
  ): number {

    const numero =
      this.numero(
        valor
      );

    return Number.isFinite(numero)
      ? Math.trunc(numero)
      : 0;
  }


  private textoONull(
    valor:
      string |
      null |
      undefined
  ): string | null {

    if (valor == null) {
      return null;
    }

    const texto =
      valor.trim();

    return texto.length > 0
      ? texto
      : null;
  }


  private limpiarMensajes(): void {

    this.mensajeError = '';
    this.mensajeExito = '';
  }


  private obtenerMensajeError(
    error: any,
    mensajeDefecto: string
  ): string {

    const mensajeBackend =
      error?.error?.message
      ?? error?.error?.mensaje
      ?? error?.error?.error;

    if (
      typeof mensajeBackend ===
        'string'
      &&
      mensajeBackend.trim()
    ) {

      return mensajeBackend.trim();
    }

    if (
      typeof error?.error ===
        'string'
      &&
      error.error.trim()
    ) {

      return error.error.trim();
    }

    return mensajeDefecto;
  }
}
