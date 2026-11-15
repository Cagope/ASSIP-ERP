
import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, Subject, takeUntil } from 'rxjs';
import {
  ConsultaCreditosAsociado,
  ConsultaCreditosBuscadorComponent
} from '../../consulta-creditos/buscador/consulta-creditos-buscador.component';
import { ConsultaCreditoResumen } from '../../consulta-creditos/consulta-creditos.models';
import { AdministracionSegurosApi } from './administracion-seguros.api';
import {
  AdministracionSegurosConfiguracion,
  AdministracionSegurosSeguro
} from './administracion-seguros.models';

/** Administración de las condiciones particulares del seguro de un crédito. */
@Component({
  selector: 'app-administracion-seguros',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ConsultaCreditosBuscadorComponent
  ],
  templateUrl: './administracion-seguros.component.html',
  styleUrls: ['./administracion-seguros.component.scss']
})
export class AdministracionSegurosComponent implements OnDestroy {
  private readonly destruir$ = new Subject<void>();

  idAgencia: number | null = null;
  asociadoSeleccionado: ConsultaCreditosAsociado | null = null;
  creditos: ConsultaCreditoResumen[] = [];
  creditoSeleccionado: ConsultaCreditoResumen | null = null;
  seguros: AdministracionSegurosSeguro[] = [];
  seguroSeleccionado: AdministracionSegurosSeguro | null = null;
  configuracion: AdministracionSegurosConfiguracion = this.configuracionVacia();

  activo = true;
  cargando = false;
  guardando = false;
  error: string | null = null;
  aviso: string | null = null;

  constructor(private readonly api: AdministracionSegurosApi) {}

  /** Busca los créditos activos con saldo del asociado seleccionado. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    if (this.cargando || this.guardando) return;

    this.asociadoSeleccionado = asociado;
    this.creditos = [];
    this.limpiarCredito();
    this.idAgencia = null;
    this.error = null;
    this.aviso = null;
    this.cargando = true;

    this.api.listarCreditosPorAsociado(asociado.idDatosPersonal)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: creditos => {
          this.creditos = (creditos ?? []).filter(credito =>
            credito.codigoEstadoCartera === 'A' &&
            Number(credito.saldoActual ?? 0) > 0
          );
          this.cargando = false;
        },
        error: (err: unknown) => {
          this.cargando = false;
          this.error = this.mensajeError(
            err,
            'No fue posible consultar los créditos.'
          );
        }
      });
  }

  /** Obtiene la agencia real del crédito y consulta sus seguros. */
  seleccionarCredito(credito: ConsultaCreditoResumen): void {
    if (this.cargando || this.guardando) return;

    const idCredito = credito.idCarteraCredito;

    if (!idCredito) {
      this.error = 'El crédito seleccionado no tiene un identificador válido.';
      return;
    }

    this.limpiarCredito();
    this.creditoSeleccionado = credito;
    this.idAgencia = null;
    this.error = null;
    this.aviso = null;
    this.cargando = true;

    this.api.consultarDetalleCredito(idCredito)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: detalle => {
          if (!detalle.idAgencia) {
            this.cargando = false;
            this.error = 'No se pudo determinar la agencia del crédito.';
            return;
          }

          this.idAgencia = detalle.idAgencia;
          this.cargando = false;
          this.consultarSeguros();
        },
        error: (err: unknown) => {
          this.cargando = false;
          this.error = this.mensajeError(
            err,
            'No fue posible consultar el detalle del crédito.'
          );
        }
      });
  }

  /** Regresa a los créditos del asociado sin repetir su búsqueda. */
  cambiarCredito(): void {
    if (this.cargando || this.guardando) return;

    this.limpiarCredito();
    this.idAgencia = null;
    this.error = null;
    this.aviso = null;
  }

  /** Consulta las configuraciones existentes y prepara la creación cuando no hay seguro. */
  consultarSeguros(): void {
    const idCredito = this.creditoSeleccionado?.idCarteraCredito;
    const idAgencia = this.idAgencia;

    if (!idCredito || !idAgencia || this.cargando || this.guardando) {
      return;
    }

    this.cargando = true;
    this.error = null;

    this.api.consultar(idCredito, idAgencia)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: seguros => {
          this.seguros = seguros ?? [];
          this.seguroSeleccionado = null;
          this.configuracion = this.configuracionVacia();
          this.activo = true;

          if (this.seguros.length === 1) {
            this.cargando = false;
            this.seleccionarSeguro(this.seguros[0]);
            return;
          }

          if (this.seguros.length > 1) {
            this.cargando = false;
            return;
          }

          this.consultarParametroBase(idAgencia);
        },
        error: (err: unknown) => {
          this.cargando = false;
          this.error = this.mensajeError(
            err,
            'No fue posible consultar los seguros.'
          );
        }
      });
  }

  /** Precarga el porcentaje base general cuando el crédito no tiene seguro. */
  private consultarParametroBase(idAgencia: number): void {
    this.api.consultarPorcentajeBaseSeguro(idAgencia)
      .pipe(takeUntil(this.destruir$))
      .subscribe({
        next: porcentaje => {
          const valor = Number(porcentaje);

          if (!Number.isFinite(valor) || valor < 0) {
            this.error = 'El porcentaje base parametrizado no es válido.';
          } else {
            this.configuracion.porcentajeBaseSeguro = valor;
            this.calcularPorcentajeAplicar();
          }

          this.cargando = false;
        },
        error: (err: unknown) => {
          this.cargando = false;
          this.error = this.mensajeError(
            err,
            'No fue posible consultar el porcentaje base del seguro.'
          );
        }
      });
  }

  /** Carga el seguro registrado y permite modificar su porcentaje base y extraprima. */
  seleccionarSeguro(seguro: AdministracionSegurosSeguro): void {
    if (this.guardando) return;

    this.seguroSeleccionado = seguro;

    this.configuracion = {
      porcentajeBaseSeguro: seguro.porcentajeBaseSeguro,
      tipoCobroSeguro: seguro.tipoCobroSeguro,
      fechaInicialCobro: seguro.fechaInicialCobro,
      porcentajeAuxilioEntidad: seguro.porcentajeAuxilioEntidad,
      porcentajeExtraprima: seguro.porcentajeExtraprima,
      porcentajeAplicar: seguro.porcentajeAplicar,
      sobreSaldoActual: seguro.sobreSaldoActual,
      incluyeInteresesCausados: seguro.incluyeInteresesCausados,
      entidadAsumeSeguroBase: seguro.entidadAsumeSeguroBase
    };

    this.activo = seguro.activo;
    this.calcularPorcentajeAplicar();
    this.error = null;
  }

  /**
   * Calcula el porcentaje de extraprima que corresponde al asociado.
   * El backend realiza nuevamente el cálculo antes de guardar.
   */
  calcularPorcentajeAplicar(): void {
    const base = Number(this.configuracion.porcentajeBaseSeguro);
    const extraprima = Number(this.configuracion.porcentajeExtraprima);

    if (
      !Number.isFinite(base) ||
      !Number.isFinite(extraprima) ||
      base < 0 ||
      extraprima < 0
    ) {
      this.configuracion.porcentajeAplicar = 0;
      return;
    }

    this.configuracion.porcentajeAplicar =
      Math.round((base * extraprima / 100 + Number.EPSILON) * 1_000_000)
      / 1_000_000;
  }

  /** Valida los porcentajes y solicita la creación o modificación del seguro. */
  guardar(): void {
    const idCredito = this.creditoSeleccionado?.idCarteraCredito;
    const idAgencia = this.idAgencia;

    if (!idCredito || !idAgencia || this.guardando || this.cargando) {
      return;
    }

    const c = this.configuracion;
    const base = Number(c.porcentajeBaseSeguro);
    const extraprima = Number(c.porcentajeExtraprima);

    if (
      !Number.isFinite(base) ||
      base < 0 ||
      !Number.isFinite(extraprima) ||
      extraprima < 0
    ) {
      this.error = 'Verifique el porcentaje base y la extraprima.';
      return;
    }

    if (!this.seguroSeleccionado && !c.fechaInicialCobro) {
      this.error = 'Debe indicar la fecha inicial de cobro.';
      return;
    }

    if (!this.seguroSeleccionado && base === 0) {
      this.error = 'El porcentaje base debe ser mayor que cero para crear el seguro.';
      return;
    }

    this.calcularPorcentajeAplicar();

    this.guardando = true;
    this.error = null;
    this.aviso = null;

    const esEdicion = this.seguroSeleccionado !== null;

    const solicitud: Observable<number | void> = esEdicion
      ? this.api.editar(
          idCredito,
          this.seguroSeleccionado!.idCreditoSeguro,
          idAgencia,
          {
            configuracion: c,
            activo: this.activo
          }
        )
      : this.api.crear(
          idCredito,
          idAgencia,
          c
        );

    solicitud.pipe(takeUntil(this.destruir$)).subscribe({
      next: () => {
        this.guardando = false;
        this.aviso = esEdicion
          ? 'Las condiciones del seguro se actualizaron correctamente.'
          : 'El seguro se creó correctamente.';

        this.limpiarCredito();
        this.idAgencia = null;
        this.error = null;
      },
      error: (err: unknown) => {
        this.guardando = false;
        this.error = this.mensajeError(
          err,
          'No fue posible guardar el seguro.'
        );
      }
    });
  }

  /** Limpia los datos del crédito y del seguro seleccionados. */
  private limpiarCredito(): void {
    this.creditoSeleccionado = null;
    this.seguros = [];
    this.seguroSeleccionado = null;
    this.configuracion = this.configuracionVacia();
    this.activo = true;
  }

  /** Construye los valores iniciales del seguro sin alterar el contrato del API. */
  private configuracionVacia(): AdministracionSegurosConfiguracion {
    return {
      porcentajeBaseSeguro: 0,
      tipoCobroSeguro: 'S',
      fechaInicialCobro: null,
      porcentajeAuxilioEntidad: 0,
      porcentajeExtraprima: 0,
      porcentajeAplicar: 0,
      sobreSaldoActual: true,
      incluyeInteresesCausados: false,
      entidadAsumeSeguroBase: true
    };
  }

  /** Obtiene el mensaje funcional de error de la respuesta HTTP. */
  private mensajeError(error: any, alternativo: string): string {
    return typeof error?.error?.message === 'string'
      ? error.error.message
      : typeof error?.error === 'string'
        ? error.error
        : alternativo;
  }

  ngOnDestroy(): void {
    this.destruir$.next();
    this.destruir$.complete();
  }
}
