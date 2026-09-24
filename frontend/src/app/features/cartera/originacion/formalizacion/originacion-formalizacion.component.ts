import { CommonModule } from '@angular/common';

import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import { FormsModule } from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import { HttpErrorResponse } from '@angular/common/http';

import { forkJoin } from 'rxjs';

import {
  OriginacionFormalizacionApi,
  ResultadoValidacionFormalizacion
} from './originacion-formalizacion.api';

import {
  SolicitudFormalizacionDetalle,
  SolicitudFormalizacionGuardarRequest
} from './originacion-formalizacion.models';

import {
  FormaPago,
  ModalidadInteres,
  TipoCuota
} from '../solicitud/originacion-solicitud.models';


@Component({
  selector: 'app-originacion-formalizacion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './originacion-formalizacion.component.html',
  styleUrl: './originacion-formalizacion.component.scss'
})
export class OriginacionFormalizacionComponent implements OnInit {

  private readonly api = inject(OriginacionFormalizacionApi);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);


  // =========================================================
  // CONSULTA
  // =========================================================

  idBusqueda: number | null = null;

  detalle: SolicitudFormalizacionDetalle | null = null;


  // =========================================================
  // FORMULARIO
  // =========================================================

  formulario: SolicitudFormalizacionGuardarRequest =
    this.formularioVacio();


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  formasPago: FormaPago[] = [];

  modalidadesInteres: ModalidadInteres[] = [];

  tiposCuota: TipoCuota[] = [];


  // =========================================================
  // ESTADOS DE OPERACIÓN
  // =========================================================

  cargando = false;

  validando = false;

  guardando = false;

  generandoPagare = false;

  finalizando = false;


  // =========================================================
  // MENSAJES
  // =========================================================

  error = '';

  mensaje = '';


  // =========================================================
  // RESULTADO DE VALIDACIÓN
  // =========================================================

  resultadoValidacion:
    ResultadoValidacionFormalizacion | null = null;

  private formularioValidado: string | null = null;


  // =========================================================
  // ESTADO GENERAL
  // =========================================================

  get procesando(): boolean {

    return this.cargando
      || this.validando
      || this.guardando
      || this.generandoPagare
      || this.finalizando;
  }


  // =========================================================
  // PAGARÉ GENERADO
  // =========================================================

  get tienePagare(): boolean {

    return this.detalle?.idCarteraCredito != null;
  }


  get pagareGenerado(): boolean {

    return this.tienePagare;
  }


  // =========================================================
  // FORMALIZACIÓN EDITABLE
  //
  // Después de generar el pagaré no se permite modificar
  // las condiciones que constituyeron el crédito.
  // =========================================================

  get editable(): boolean {

    const d = this.detalle;

    return d != null
      && d.idSolicitudProceso === 4
      && d.idSolicitudResultado === 1
      && !d.fechaFinFormalizacion
      && d.idCarteraCredito == null;
  }


  // =========================================================
  // FORMALIZACIÓN ABIERTA
  //
  // Permite finalizar cuando el crédito ya está generado.
  // =========================================================

  get formalizacionAbierta(): boolean {

    const d = this.detalle;

    return d != null
      && d.idSolicitudProceso === 4
      && d.idSolicitudResultado === 1
      && !d.fechaFinFormalizacion;
  }


  // =========================================================
  // CONDICIONES GUARDADAS
  // =========================================================

  get condicionesGuardadas(): boolean {

    const d = this.detalle;

    return d != null
      && d.valorFormalizado != null
      && d.plazoFormalizado != null
      && d.tasaNominalFormalizada != null
      && d.tasaEfectivaAnualFormalizada != null
      && d.valorCuotaFormalizada != null
      && d.condicionesModificadas != null;
  }


  // =========================================================
  // MODALIDAD SELECCIONADA
  // =========================================================

  get modalidadSeleccionada(): ModalidadInteres | undefined {

    return this.modalidadesInteres.find(m =>

      m.periodoCodigo ===
        this.formulario.periodoCodigoInteresFormalizado

      && m.tipoModalidad ===
        this.formulario.tipoModalidadInteresFormalizado

    );
  }


  // =========================================================
  // CAMBIOS FRENTE A CONDICIONES ORIGINALES
  // =========================================================

  get cambiosPendientes(): boolean {

    const d = this.detalle;

    const f = this.formulario;

    if (!d) {
      return false;
    }

    return d.valorSolicitado !== f.valorFormalizado

      || d.plazoSolicitado !== f.plazoFormalizado

      || d.codigoFormaPago?.trim() !==
        f.codigoFormaPagoFormalizada

      || d.periodoCodigoInteres?.trim() !==
        f.periodoCodigoInteresFormalizado

      || d.tipoModalidadInteres?.trim() !==
        f.tipoModalidadInteresFormalizado

      || d.amortizacionCapital !==
        f.amortizacionCapitalFormalizada

      || d.codigoTipoCuota?.trim() !==
        f.codigoTipoCuotaFormalizada

      || d.mesesGraciaCapital !==
        f.mesesGraciaCapitalFormalizados

      || d.mesesGraciaInteres !==
        f.mesesGraciaInteresFormalizados

      || d.tasaColocacionAplicada !==
        f.tasaNominalFormalizada;
  }


  // =========================================================
  // CAMBIOS SIN GUARDAR
  // =========================================================

  get hayEdicionSinGuardar(): boolean {

    const d = this.detalle;

    const f = this.formulario;

    if (!d) {
      return false;
    }

    return d.valorFormalizado !== f.valorFormalizado

      || d.plazoFormalizado !== f.plazoFormalizado

      || d.codigoFormaPagoFormalizada !==
        f.codigoFormaPagoFormalizada

      || d.periodoCodigoInteresFormalizado !==
        f.periodoCodigoInteresFormalizado

      || d.tipoModalidadInteresFormalizado !==
        f.tipoModalidadInteresFormalizado

      || d.amortizacionCapitalFormalizada !==
        f.amortizacionCapitalFormalizada

      || d.codigoTipoCuotaFormalizada !==
        f.codigoTipoCuotaFormalizada

      || d.mesesGraciaCapitalFormalizados !==
        f.mesesGraciaCapitalFormalizados

      || d.mesesGraciaInteresFormalizados !==
        f.mesesGraciaInteresFormalizados

      || d.tasaNominalFormalizada !==
        f.tasaNominalFormalizada;
  }


  // =========================================================
  // VALIDACIÓN VIGENTE
  //
  // Una validación deja de ser válida si cambia cualquiera
  // de las condiciones digitadas.
  // =========================================================

  get validacionVigente(): boolean {

    return this.resultadoValidacion?.puedeContinuar === true

      && this.formularioValidado ===
        this.firmaFormulario(this.formulario);
  }


  // =========================================================
  // PERMISOS DE ACCIONES
  // =========================================================

  get puedeValidar(): boolean {

    return this.editable
      && !this.procesando;
  }


  get puedeGuardar(): boolean {

    return this.editable
      && !this.procesando
      && this.validacionVigente;
  }


  get puedeGenerarPagare(): boolean {

    return this.editable
      && !this.procesando
      && this.condicionesGuardadas
      && !this.hayEdicionSinGuardar;
  }


  get puedeFinalizar(): boolean {

    return this.formalizacionAbierta
      && !this.procesando
      && this.condicionesGuardadas
      && this.tienePagare;
  }


  // =========================================================
  // INICIALIZACIÓN
  // =========================================================

  ngOnInit(): void {

    this.cargarCatalogos();

    this.route.paramMap.subscribe(params => {

      const id = Number(
        params.get('idSolicitudCredito')
      );

      if (
        Number.isInteger(id)
        && id > 0
      ) {

        this.idBusqueda = id;

        this.cargar(id);

      } else {

        this.idBusqueda = null;

        this.detalle = null;

        this.formulario = this.formularioVacio();

        this.limpiarValidacion();
      }

    });
  }


  // =========================================================
  // CATÁLOGOS
  // =========================================================

  private cargarCatalogos(): void {

    forkJoin({

      formasPago:
        this.api.listarFormasPago(),

      modalidadesInteres:
        this.api.listarModalidadesInteres(),

      tiposCuota:
        this.api.listarTiposCuota()

    }).subscribe({

      next: catalogos => {

        this.formasPago =
          catalogos.formasPago ?? [];

        this.modalidadesInteres =
          catalogos.modalidadesInteres ?? [];

        this.tiposCuota =
          catalogos.tiposCuota ?? [];
      },

      error: err => {

        this.error =
          this.mensajeError(err);
      }

    });
  }


  // =========================================================
  // BUSCAR SOLICITUD
  // =========================================================

  buscar(): void {

    if (this.procesando) {
      return;
    }

    const id = Number(
      this.idBusqueda
    );

    if (
      !Number.isInteger(id)
      || id <= 0
    ) {

      this.error =
        'Ingrese un identificador de solicitud válido.';

      return;
    }

    void this.router.navigate([

      '/cartera/originacion/formalizacion',

      id

    ]);
  }


  // =========================================================
  // CONSULTAR FORMALIZACIÓN
  // =========================================================

  cargar(id: number): void {

    if (this.procesando) {
      return;
    }

    this.cargando = true;

    this.error = '';

    this.mensaje = '';

    this.detalle = null;

    this.formulario =
      this.formularioVacio();

    this.limpiarValidacion();


    this.api.consultar(id).subscribe({

      next: detalle => {

        this.aplicarDetalle(detalle);

        this.cargando = false;
      },

      error: err => {

        this.cargando = false;

        this.error =
          this.mensajeError(err);
      }

    });
  }


  // =========================================================
  // VALIDAR CONDICIONES
  //
  // No guarda condiciones financieras.
  // No genera pagaré.
  // =========================================================

  validar(): void {

    if (
      !this.editable
      || this.procesando
      || !this.detalle
    ) {
      return;
    }

    this.error = '';

    this.mensaje = '';

    this.limpiarValidacion();


    if (!this.validarFormulario()) {
      return;
    }


    const id =
      this.detalle.idSolicitudCredito;

    const request = {
      ...this.formulario
    };

    const firma =
      this.firmaFormulario(request);


    this.validando = true;


    this.api.validar(
      id,
      request
    ).subscribe({

      next: resultado => {

        this.validando = false;

        this.resultadoValidacion =
          resultado;

        if (
          resultado.puedeContinuar
          && firma ===
            this.firmaFormulario(this.formulario)
        ) {

          this.formularioValidado =
            firma;

          this.mensaje =
            'Las condiciones de formalización fueron validadas correctamente.';

        } else if (
          resultado.puedeContinuar
        ) {

          this.formularioValidado =
            null;

          this.mensaje =
            'La validación terminó, pero las condiciones cambiaron. Valide nuevamente.';

        } else {

          this.formularioValidado =
            null;

          this.error =
            resultado.bloqueos?.length

              ? resultado.bloqueos.join('\n')

              : 'La formalización presenta bloqueos que impiden continuar.';
        }

      },

      error: err => {

        this.validando = false;

        this.limpiarValidacion();

        this.error =
          this.mensajeError(err);
      }

    });
  }


  // =========================================================
  // GUARDAR CONDICIONES DEFINITIVAS
  //
  // El backend vuelve a validar.
  // =========================================================

  guardar(): void {

    if (
      !this.editable
      || this.procesando
      || !this.detalle
    ) {
      return;
    }


    this.error = '';

    this.mensaje = '';


    if (!this.validarFormulario()) {
      return;
    }


    if (!this.validacionVigente) {

      this.error =
        'Valide las condiciones actuales antes de guardar.';

      return;
    }


    const id =
      this.detalle.idSolicitudCredito;

    const request = {
      ...this.formulario
    };


    this.guardando = true;


    this.api.guardar(
      id,
      request
    ).subscribe({

      next: detalle => {

        this.guardando = false;

        this.aplicarDetalle(detalle);

        this.mensaje =
          'Condiciones definitivas guardadas correctamente.';
      },

      error: err => {

        this.guardando = false;

        this.limpiarValidacion();

        this.error =
          this.mensajeError(err);
      }

    });
  }


  // =========================================================
  // GENERAR PAGARÉ
  //
  // El backend:
  // - Revalida las condiciones guardadas.
  // - Obtiene el consecutivo 605.
  // - Verifica duplicados por agencia.
  // - Constituye el crédito en estado P.
  // - Vincula el crédito con la solicitud.
  // =========================================================

  generarPagare(): void {

    if (
      !this.detalle
      || this.procesando
      || !this.editable
    ) {
      return;
    }


    this.error = '';

    this.mensaje = '';


    if (!this.condicionesGuardadas) {

      this.error =
        'Guarde las condiciones definitivas antes de generar el pagaré.';

      return;
    }


    if (this.hayEdicionSinGuardar) {

      this.error =
        'Existen cambios sin guardar. Guarde las condiciones definitivas antes de generar el pagaré.';

      return;
    }


    const confirmado =
      window.confirm(

        '¿Generar el pagaré y constituir el crédito en estado pendiente? '
        + 'Esta operación asignará el número de pagaré de la agencia '
        + 'y bloqueará la modificación de las condiciones formalizadas.'

      );


    if (!confirmado) {
      return;
    }


    const id =
      this.detalle.idSolicitudCredito;


    this.generandoPagare = true;


    this.api.generarPagare(id).subscribe({

      next: detalle => {

        this.generandoPagare = false;

        this.aplicarDetalle(detalle);

        this.mensaje =
          'Pagaré generado correctamente. '
          + 'El crédito quedó constituido en estado pendiente.';
      },

      error: err => {

        this.generandoPagare = false;

        this.error =
          this.mensajeError(err);
      }

    });
  }


  // =========================================================
  // FINALIZAR FORMALIZACIÓN
  //
  // Requiere un crédito vinculado en estado P.
  // =========================================================

  finalizar(): void {

    if (
      !this.detalle
      || this.procesando
      || !this.formalizacionAbierta
    ) {
      return;
    }


    this.error = '';

    this.mensaje = '';


    if (!this.condicionesGuardadas) {

      this.error =
        'La solicitud no tiene condiciones definitivas completas.';

      return;
    }


    if (!this.tienePagare) {

      this.error =
        'Debe generar el pagaré antes de finalizar la formalización.';

      return;
    }


    const confirmado =
      window.confirm(

        '¿Finalizar formalización y enviar la solicitud '
        + 'al proceso de desembolso?'

      );


    if (!confirmado) {
      return;
    }


    this.finalizando = true;


    this.api.finalizar(
      this.detalle.idSolicitudCredito
    ).subscribe({

      next: detalle => {

        this.finalizando = false;

        this.aplicarDetalle(detalle);

        this.mensaje =
          'Formalización finalizada. '
          + 'La solicitud pasó al proceso de desembolso.';
      },

      error: err => {

        this.finalizando = false;

        this.error =
          this.mensajeError(err);
      }

    });
  }


  // =========================================================
  // VOLVER
  // =========================================================

  volver(): void {

    if (this.procesando) {
      return;
    }

    void this.router.navigate([
      '/cartera/originacion'
    ]);
  }


  // =========================================================
  // VALIDACIÓN BÁSICA DEL FORMULARIO
  //
  // Las reglas financieras y de riesgo son responsabilidad
  // del backend.
  // =========================================================

  private validarFormulario(): boolean {

    const f = this.formulario;


    if (

      !Number.isFinite(f.valorFormalizado)

      || f.valorFormalizado <= 0

      || !Number.isInteger(f.plazoFormalizado)

      || f.plazoFormalizado <= 0

      || !Number.isInteger(
        f.amortizacionCapitalFormalizada
      )

      || f.amortizacionCapitalFormalizada <= 0

      || f.amortizacionCapitalFormalizada >
        f.plazoFormalizado

      || !Number.isFinite(
        f.tasaNominalFormalizada
      )

      || f.tasaNominalFormalizada < 0

      || !f.codigoFormaPagoFormalizada

      || !f.periodoCodigoInteresFormalizado

      || !f.tipoModalidadInteresFormalizado

      || !f.codigoTipoCuotaFormalizada

      || !Number.isInteger(
        f.mesesGraciaCapitalFormalizados
      )

      || f.mesesGraciaCapitalFormalizados < 0

      || !Number.isInteger(
        f.mesesGraciaInteresFormalizados
      )

      || f.mesesGraciaInteresFormalizados < 0

    ) {

      this.error =
        'Complete las condiciones definitivas con valores válidos.';

      return false;
    }


    if (

      f.codigoTipoCuotaFormalizada === '1'

      && this.modalidadSeleccionada

      && this.modalidadSeleccionada.periodoMeses !==
        f.amortizacionCapitalFormalizada

    ) {

      this.error =
        'Para cuota fija, la periodicidad de intereses '
        + 'debe coincidir con la amortización de capital.';

      return false;
    }


    return true;
  }


  // =========================================================
  // APLICAR RESPUESTA DEL BACKEND
  // =========================================================

  private aplicarDetalle(
    detalle: SolicitudFormalizacionDetalle
  ): void {

    this.detalle = detalle;

    this.formulario =
      this.desdeDetalle(detalle);

    this.limpiarValidacion();
  }


  // =========================================================
  // CONSTRUIR FORMULARIO DESDE EL DETALLE
  // =========================================================

  private desdeDetalle(
    d: SolicitudFormalizacionDetalle
  ): SolicitudFormalizacionGuardarRequest {

    return {

      valorFormalizado:
        d.valorFormalizado
        ?? d.valorSolicitado
        ?? 0,

      plazoFormalizado:
        d.plazoFormalizado
        ?? d.plazoSolicitado
        ?? 0,

      codigoFormaPagoFormalizada:
        d.codigoFormaPagoFormalizada
        ?? d.codigoFormaPago
        ?? '',

      periodoCodigoInteresFormalizado:
        d.periodoCodigoInteresFormalizado
        ?? d.periodoCodigoInteres
        ?? '',

      tipoModalidadInteresFormalizado:
        d.tipoModalidadInteresFormalizado
        ?? d.tipoModalidadInteres
        ?? '',

      amortizacionCapitalFormalizada:
        d.amortizacionCapitalFormalizada
        ?? d.amortizacionCapital
        ?? 0,

      codigoTipoCuotaFormalizada:
        d.codigoTipoCuotaFormalizada
        ?? d.codigoTipoCuota
        ?? '',

      mesesGraciaCapitalFormalizados:
        d.mesesGraciaCapitalFormalizados
        ?? d.mesesGraciaCapital
        ?? 0,

      mesesGraciaInteresFormalizados:
        d.mesesGraciaInteresFormalizados
        ?? d.mesesGraciaInteres
        ?? 0,

      tasaNominalFormalizada:
        d.tasaNominalFormalizada
        ?? d.tasaColocacionAplicada
        ?? 0

    };
  }


  // =========================================================
  // FORMULARIO VACÍO
  // =========================================================

  private formularioVacio():
    SolicitudFormalizacionGuardarRequest {

    return {

      valorFormalizado: 0,

      plazoFormalizado: 0,

      codigoFormaPagoFormalizada: '',

      periodoCodigoInteresFormalizado: '',

      tipoModalidadInteresFormalizado: '',

      amortizacionCapitalFormalizada: 0,

      codigoTipoCuotaFormalizada: '',

      mesesGraciaCapitalFormalizados: 0,

      mesesGraciaInteresFormalizados: 0,

      tasaNominalFormalizada: 0

    };
  }


  // =========================================================
  // IDENTIFICAR CONDICIONES VALIDADAS
  // =========================================================

  private firmaFormulario(
    f: SolicitudFormalizacionGuardarRequest
  ): string {

    return JSON.stringify({

      valorFormalizado:
        f.valorFormalizado,

      plazoFormalizado:
        f.plazoFormalizado,

      codigoFormaPagoFormalizada:
        f.codigoFormaPagoFormalizada,

      periodoCodigoInteresFormalizado:
        f.periodoCodigoInteresFormalizado,

      tipoModalidadInteresFormalizado:
        f.tipoModalidadInteresFormalizado,

      amortizacionCapitalFormalizada:
        f.amortizacionCapitalFormalizada,

      codigoTipoCuotaFormalizada:
        f.codigoTipoCuotaFormalizada,

      mesesGraciaCapitalFormalizados:
        f.mesesGraciaCapitalFormalizados,

      mesesGraciaInteresFormalizados:
        f.mesesGraciaInteresFormalizados,

      tasaNominalFormalizada:
        f.tasaNominalFormalizada

    });
  }


  // =========================================================
  // LIMPIAR VALIDACIÓN
  // =========================================================

  private limpiarValidacion(): void {

    this.resultadoValidacion = null;

    this.formularioValidado = null;
  }


  // =========================================================
  // MENSAJES DE ERROR
  // =========================================================

  private mensajeError(
    err: HttpErrorResponse | unknown
  ): string {

    const respuesta = err as {

      error?: {

        message?: string;

        mensaje?: string;

        error?: string;

        bloqueos?: string[];

      };

    };


    if (
      Array.isArray(
        respuesta?.error?.bloqueos
      )

      && respuesta.error.bloqueos.length > 0
    ) {

      return respuesta.error.bloqueos.join('\n');
    }


    const mensaje =

      respuesta?.error?.message

      ?? respuesta?.error?.mensaje

      ?? respuesta?.error?.error;


    return typeof mensaje === 'string'
      && mensaje.trim()

      ? mensaje.trim()

      : 'No fue posible completar la operación de formalización.';
  }

}
