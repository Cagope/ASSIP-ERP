
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  of,
  switchMap,
  takeUntil,
  tap
} from 'rxjs';

import { environment } from '../../../../../environments/environment';

import {
  ConsultaCreditosAsociado,
  ConsultaCreditosBuscadorComponent
} from '../../consulta-creditos/buscador/consulta-creditos-buscador.component';

import { PagoCuotasV1Api } from '../../pago-cuotas_v1/pago-cuotas_v1.api';

import {
  PagoCuotasCreditoResumen,
  PagoCuotasCreditoDetalle
} from '../../pago-cuotas_v1/pago-cuotas_v1.models';

import {
  ProrrogasNumericFormatDirective
} from '../../prorrogas_v1/prorrogas-numeric-format.directive';

import { CostasJudicialesApi } from './administracion-costas-judiciales.api';

import {
  CostasJudicialesRequest,
  CostasJudicialesVistaPrevia,
  CostasJudicialesResultado
} from './administracion-costas-judiciales.models';

/** Persona obtenida de la búsqueda compartida del ERP. */
interface CostasJudicialesTercero {
  idDatosPersonal: number;
  documento: string;
  nombreCompleto: string;
}

/** Registro y contabilización de costas judiciales por crédito. */
@Component({
  selector: 'app-costas-judiciales',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ConsultaCreditosBuscadorComponent,
    ProrrogasNumericFormatDirective
  ],
  templateUrl: './administracion-costas-judiciales.component.html',
  styleUrls: ['./administracion-costas-judiciales.component.scss']
})
export class CostasJudicialesComponent implements OnDestroy {
  private readonly destruir$ = new Subject<void>();
  private readonly busquedaTerceros$ = new Subject<string>();
  private versionSeleccion = 0;

  asociado: ConsultaCreditosAsociado | null = null;
  creditos: PagoCuotasCreditoResumen[] = [];
  credito: PagoCuotasCreditoDetalle | null = null;
  idCreditoSeleccionado: number | null = null;

  fechaMovimiento = this.fechaActual();
  tipoComprobante = 'NC';
  valor: number | null = null;
  observacion = '';

  textoBusquedaTercero = '';
  terceros: CostasJudicialesTercero[] = [];
  tercero: CostasJudicialesTercero | null = null;
  idDatosPersonalTercero: number | null = null;
  terceroSeleccionado = '';
  buscandoTerceros = false;
  mostrarResultadosTerceros = false;

  vistaPrevia: CostasJudicialesVistaPrevia | null = null;
  resultado: CostasJudicialesResultado | null = null;

  cargando = false;
  error: string | null = null;
  aviso: string | null = null;

  private solicitudPrevisualizada: string | null = null;

  constructor(
    private readonly api: CostasJudicialesApi,
    private readonly pagosApi: PagoCuotasV1Api,
    private readonly http: HttpClient
  ) {
    this.configurarBusquedaTerceros();
  }

  /** Obtiene la fecha local sin conversiones de zona horaria. */
  private fechaActual(): string {
    const fecha = new Date();
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  /**
   * Consulta terceros mediante la API compartida de Contabilidad.
   * Cancela consultas anteriores para evitar resultados desactualizados.
   */
  private configurarBusquedaTerceros(): void {
    this.busquedaTerceros$.pipe(
      map(texto => texto.trim()),
      debounceTime(250),
      distinctUntilChanged(),
      tap(texto => {
        if (texto.length < 2) {
          this.terceros = [];
          this.buscandoTerceros = false;
          this.mostrarResultadosTerceros = false;
        }
      }),
      switchMap(texto => {
        if (texto.length < 2) {
          return of([] as CostasJudicialesTercero[]);
        }
        return this.http.get<CostasJudicialesTercero[]>(
          `${environment.apiUrl}/shared/personas/buscar`,
          {
            params: new HttpParams().set('q', texto)
          }
        ).pipe(
          catchError(() => {
            this.error = 'No fue posible buscar los terceros.';
            return of([] as CostasJudicialesTercero[]);
          })
        );
      }),
      takeUntil(this.destruir$)
    ).subscribe(terceros => {
      this.terceros = terceros;
      this.buscandoTerceros = false;
      this.mostrarResultadosTerceros =
        this.tercero === null &&
        this.textoBusquedaTercero.trim().length >= 2 &&
        terceros.length > 0;
    });
  }

  /** Consulta los créditos del asociado seleccionado. */
  seleccionarAsociado(asociado: ConsultaCreditosAsociado): void {
    this.asociado = asociado;
    this.creditos = [];
    this.limpiarCredito();
    this.error = null;
    this.aviso = null;
    this.cargando = true;

    const version = this.versionSeleccion;
    this.pagosApi.listarCreditosPorAsociado(
      asociado.idDatosPersonal
    ).pipe(
      takeUntil(this.destruir$)
    ).subscribe({
      next: creditos => {
        if (version !== this.versionSeleccion) return;
        this.creditos = (creditos ?? []).filter(
          credito => credito.codigoEstadoCartera === 'A'
        );
        this.cargando = false;
      },
      error: () => {
        if (version !== this.versionSeleccion) return;
        this.error = 'No fue posible consultar los créditos.';
        this.cargando = false;
      }
    });
  }

  /** Consulta el detalle del crédito seleccionado. */
  seleccionarCredito(id: number | null): void {
    if (this.cargando) return;

    this.limpiarCredito();
    if (id == null) return;

    const version = this.versionSeleccion;
    this.idCreditoSeleccionado = id;
    this.cargando = true;
    this.error = null;

    this.pagosApi.consultarCredito(id).pipe(
      takeUntil(this.destruir$)
    ).subscribe({
      next: credito => {
        if (version !== this.versionSeleccion ||
            this.idCreditoSeleccionado !== id) return;

        this.credito = credito;
        this.observacion =
          `COSTAS JUDICIALES - PAGARÉ ${credito.pagareCartera ?? ''}`;
        this.cargando = false;
      },
      error: () => {
        if (version !== this.versionSeleccion) return;
        this.error = 'No fue posible consultar el crédito.';
        this.idCreditoSeleccionado = null;
        this.cargando = false;
      }
    });
  }

  /**
   * Actualiza la búsqueda y descarta cualquier tercero anterior.
   * Nunca conserva un identificador asociado a un texto modificado.
   */
  buscarTerceros(texto: string): void {
    this.textoBusquedaTercero = texto;
    this.tercero = null;
    this.idDatosPersonalTercero = null;
    this.terceroSeleccionado = '';
    this.terceros = [];
    this.mostrarResultadosTerceros = false;
    this.buscandoTerceros = texto.trim().length >= 2;

    this.datosModificados();
    this.busquedaTerceros$.next(texto);
  }

  /** Selecciona el tercero encontrado por documento o nombre. */
  seleccionarTercero(tercero: CostasJudicialesTercero): void {
    this.tercero = tercero;
    this.idDatosPersonalTercero = tercero.idDatosPersonal;
    this.terceroSeleccionado =
      `${tercero.documento} - ${tercero.nombreCompleto}`;
    this.textoBusquedaTercero = this.terceroSeleccionado;
    this.terceros = [];
    this.mostrarResultadosTerceros = false;
    this.buscandoTerceros = false;

    this.datosModificados();
    this.busquedaTerceros$.next('');
  }

  /** Retira el tercero y restablece la modalidad de agencias. */
  quitarTercero(): void {
    this.tercero = null;
    this.idDatosPersonalTercero = null;
    this.terceroSeleccionado = '';
    this.textoBusquedaTercero = '';
    this.terceros = [];
    this.mostrarResultadosTerceros = false;
    this.buscandoTerceros = false;

    this.datosModificados();
    this.busquedaTerceros$.next('');
  }

  /** Cierra las sugerencias cuando se abandona el buscador. */
  cerrarResultadosTerceros(): void {
    this.mostrarResultadosTerceros = false;
  }

  /** Invalida el comprobante cuando cambia algún dato. */
  datosModificados(): void {
    this.invalidarVistaPrevia();
    this.resultado = null;
    this.error = null;
    this.aviso = null;
  }

  /** Determina la contrapartida según el tercero seleccionado. */
  get modalidadContable(): string {
    return this.idDatosPersonalTercero !== null
      ? 'Cuentas por pagar a tercero'
      : 'Sucursales y agencias';
  }

  /** Construye y valida la solicitud enviada al backend. */
  private construirSolicitud(): CostasJudicialesRequest | null {
    if (this.idCreditoSeleccionado == null || !this.credito) {
      this.error = 'Seleccione un crédito.';
      return null;
    }
    if (!this.fechaMovimiento) {
      this.error = 'Seleccione la fecha del movimiento.';
      return null;
    }
    if (!/^[A-Za-z0-9]{2}$/.test(this.tipoComprobante.trim())) {
      this.error = 'Seleccione un tipo de comprobante válido.';
      return null;
    }
    if (
      this.valor == null ||
      !Number.isFinite(this.valor) ||
      this.valor <= 0
    ) {
      this.error = 'Ingrese un valor positivo.';
      return null;
    }
    if (!this.observacion.trim()) {
      this.error = 'Ingrese la descripción de las costas judiciales.';
      return null;
    }
    return {
      idCarteraCredito: this.idCreditoSeleccionado,
      fechaMovimiento: this.fechaMovimiento,
      tipoComprobante: this.tipoComprobante.trim().toUpperCase(),
      idDatosPersonalTercero: this.idDatosPersonalTercero,
      valor: this.valor,
      observacion: this.observacion.trim()
    };
  }

  /** Previsualiza el comprobante sin registrar movimientos. */
  previsualizar(): void {
    if (this.cargando) return;

    this.error = null;
    this.aviso = null;
    this.invalidarVistaPrevia();

    const solicitud = this.construirSolicitud();
    if (!solicitud) return;

    const firma = JSON.stringify(solicitud);
    const version = this.versionSeleccion;
    this.cargando = true;

    this.api.previsualizar(solicitud).pipe(
      takeUntil(this.destruir$)
    ).subscribe({
      next: vista => {
        if (version !== this.versionSeleccion) return;
        this.vistaPrevia = vista;
        this.solicitudPrevisualizada = firma;
        this.cargando = false;
      },
      error: () => {
        if (version !== this.versionSeleccion) return;
        this.error = 'No fue posible previsualizar el comprobante.';
        this.cargando = false;
      }
    });
  }

  /** Confirma únicamente los datos de la última previsualización. */
  confirmar(): void {
    if (this.cargando || !this.vistaPrevia) return;

    this.error = null;
    const solicitud = this.construirSolicitud();
    if (!solicitud) return;

    if (JSON.stringify(solicitud) !== this.solicitudPrevisualizada) {
      this.invalidarVistaPrevia();
      this.error = 'Los datos cambiaron. Previsualice nuevamente.';
      return;
    }

    const version = this.versionSeleccion;
    this.cargando = true;

    this.api.confirmar(solicitud).pipe(
      takeUntil(this.destruir$)
    ).subscribe({
      next: resultado => {
        if (version !== this.versionSeleccion) return;
        this.resultado = resultado;
        this.invalidarVistaPrevia();
        this.aviso =
          `Costas judiciales registradas. Comprobante ` +
          `${resultado.tipoComprobante} ${resultado.numeroComprobante}`;
        this.cargando = false;
      },
      error: () => {
        if (version !== this.versionSeleccion) return;
        this.error = 'No fue posible confirmar las costas judiciales.';
        this.cargando = false;
      }
    });
  }

  /**
   * Limpia el crédito y sus datos dependientes.
   * Conserva el asociado y su lista para permitir cambiar de crédito.
   */
  limpiarCredito(): void {
    if (this.cargando) return;

    this.versionSeleccion++;
    this.credito = null;
    this.idCreditoSeleccionado = null;
    this.valor = null;
    this.observacion = '';
    this.tercero = null;
    this.idDatosPersonalTercero = null;
    this.terceroSeleccionado = '';
    this.textoBusquedaTercero = '';
    this.terceros = [];
    this.buscandoTerceros = false;
    this.mostrarResultadosTerceros = false;
    this.resultado = null;
    this.error = null;
    this.aviso = null;

    this.busquedaTerceros$.next('');
    this.invalidarVistaPrevia();
  }

  /** Descarta la previsualización anterior. */
  private invalidarVistaPrevia(): void {
    this.vistaPrevia = null;
    this.solicitudPrevisualizada = null;
  }

  /** Limpia el asociado y sus créditos. */
  limpiarAsociado(): void {
    if (this.cargando) return;

    this.limpiarCredito();
    this.asociado = null;
    this.creditos = [];
    this.error = null;
    this.aviso = null;
  }

  /** Libera las suscripciones al destruir el componente. */
  ngOnDestroy(): void {
    this.destruir$.next();
    this.destruir$.complete();
  }
}
