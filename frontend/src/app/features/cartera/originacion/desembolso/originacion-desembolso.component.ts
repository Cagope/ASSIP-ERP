import {
  CommonModule
} from '@angular/common';

import {
  Component,
  OnInit
} from '@angular/core';

import {
  FormArray,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  TipoComprobante,
  TiposComprobantesApi,

} from '../../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import {
  OriginacionDesembolsoApi
} from './originacion-desembolso.api';

import {
  SessionService
} from '../../../../core/auth/session.service';

import {
  DepositoDesembolso,
  EjecutarDesembolsoRequest,
  LiquidacionDesembolso,
  PreparacionDesembolso,
  ResultadoEjecucionDesembolso,
  ResultadoValidacionDistribucion,

} from './originacion-desembolso.models';

import {
  NumericFormatDirective
} from '../../../../shared/utils/numeric-format.directive';

import {
  HttpClient
} from '@angular/common/http';

import {
  finalize,
  forkJoin
} from 'rxjs';

import {
  environment
} from '../../../../../environments/environment';


// =========================================================
// CAJA ABIERTA
// =========================================================

interface CajaAbierta {
  idCaja: number;
  codigoCaja?: string | null;
  descripcionCaja?: string | null;
  nombreCaja?: string | null;

  idAgencia?: number | null;
  idProvision?: number | null;
  fechaContable?: string | null;
  estado?: string | null;
}

interface CuentaAhorroDisponible {
  id_cuenta_ahorro: number;

  id_datos_personal?: number | null;

  codigo_cuenta?: string | null;

  id_forma_ahorro?: number | null;
  idFormaAhorro?: number | null;

  nombre_forma_ahorro?: string | null;
  nombreFormaAhorro?: string | null;

  saldo_actual_cuenta?: number | null;

  codigo_estado?: string | null;
}


// =========================================================
// COMPONENTE
// =========================================================

@Component({
  selector: 'app-originacion-desembolso',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NumericFormatDirective
  ],

  templateUrl:
    './originacion-desembolso.component.html',

  styleUrl:
    './originacion-desembolso.component.scss'
})
export class OriginacionDesembolsoComponent
  implements OnInit {

  // =======================================================
  // IDENTIFICACIÓN
  // =======================================================

  idCarteraCredito!: number;


  // =======================================================
  // DATOS DEL PROCESO
  // =======================================================

  preparacion: PreparacionDesembolso | null = null;

  liquidacion: LiquidacionDesembolso | null = null;

  validacionDistribucion:
    ResultadoValidacionDistribucion | null = null;

  resultado:
    ResultadoEjecucionDesembolso | null = null;


  // =======================================================
  // CATÁLOGOS
  // =======================================================

  cajas: CajaAbierta[] = [];

  tiposComprobantes: TipoComprobante[] = [];

  cuentasAhorroDisponibles: CuentaAhorroDisponible[] = [];

  // =======================================================
  // ESTADO DE PANTALLA
  // =======================================================

  cargando = false;
  calculando = false;
  validando = false;
  ejecutando = false;

  error = '';
  mensaje = '';

  desembolsoEjecutado = false;


  // =======================================================
  // FORMULARIO
  // =======================================================

  form: FormGroup;


  // =======================================================
  // FORMATO
  // =======================================================

  private readonly formatoMoneda =
    new Intl.NumberFormat(
      'es-CO',
      {
        style: 'currency',
        currency: 'COP',
        maximumFractionDigits: 0
      }
    );


    constructor(
      private readonly route: ActivatedRoute,
      private readonly router: Router,
      private readonly fb: FormBuilder,
      private readonly http: HttpClient,
      private readonly api: OriginacionDesembolsoApi,
      private readonly tiposComprobantesApi: TiposComprobantesApi,
      private readonly session: SessionService
    ) {

      this.form = this.fb.group({

        // ---------------------------------------------------
        // FECHAS
        // ---------------------------------------------------

        fechaContable: [
          this.fechaHoy(),
          Validators.required
        ],

        fechaDesembolso: [
          this.fechaHoy(),
          Validators.required
        ],

        // ---------------------------------------------------
        // CAJA
        // ---------------------------------------------------

        idCaja: [
          null,
          Validators.required
        ],

        // ---------------------------------------------------
        // COMPROBANTES
        // ---------------------------------------------------

        tipoComprobanteDesembolso: [
          '',
          Validators.required
        ],

        tipoComprobanteCobros: [
          ''
        ],

        // ---------------------------------------------------
        // CENTRAL DE RIESGO
        // ---------------------------------------------------

        cantidadCentral: [
          0,
          [
            Validators.required,
            Validators.min(0)
          ]
        ],

        // ---------------------------------------------------
        // DISTRIBUCIÓN
        // ---------------------------------------------------

        valorEfectivo: [
          0,
          [
            Validators.required,
            Validators.min(0)
          ]
        ],

        valorCuentaPorPagar: [
          0,
          [
            Validators.required,
            Validators.min(0)
          ]
        ],

        depositos: this.fb.array([])

      });
    }


    get puedeDesembolsar(): boolean {

      return this.session.tienePermiso(
        'CARTERA_ORIGINACION_DESEMBOLSO'
      );
    }


    // =======================================================
    // INICIALIZACIÓN
    // =======================================================

    ngOnInit(): void {
    const id =
      Number(
        this.route.snapshot.paramMap.get(
          'idCarteraCredito'
        )
      );

    if (
      !Number.isInteger(id) ||
      id <= 0
    ) {
      this.error =
        'El crédito seleccionado no es válido.';

      return;
    }

    this.idCarteraCredito = id;

    this.cargar();
  }


  // =======================================================
  // DEPÓSITOS
  // =======================================================

  get depositos(): FormArray {
    return this.form.get(
      'depositos'
    ) as FormArray;
  }


  agregarDeposito(): void {

    if (this.desembolsoEjecutado) {
      return;
    }

    this.depositos.push(
      this.fb.group({
        idCuentaAhorro: [
          null,
          [
            Validators.required,
            Validators.min(1)
          ]
        ],

        valor: [
          0,
          [
            Validators.required,
            Validators.min(0.01)
          ]
        ]
      })
    );

    this.invalidarDistribucion();
  }


  eliminarDeposito(
    index: number
  ): void {

    if (this.desembolsoEjecutado) {
      return;
    }

    if (
      index < 0 ||
      index >= this.depositos.length
    ) {
      return;
    }

    this.depositos.removeAt(index);

    this.invalidarDistribucion();
  }


  // =======================================================
  // CARGA INICIAL
  // =======================================================

  cargar(): void {

    this.error = '';
    this.mensaje = '';
    this.cargando = true;

    this.api
      .preparar(
        this.idCarteraCredito
      )
      .pipe(
        finalize(
          () => {
            this.cargando = false;
          }
        )
      )
      .subscribe({

        next: (
          preparacion:
            PreparacionDesembolso
        ) => {

          this.preparacion =
            preparacion;

          this.form.patchValue({
            cantidadCentral:
              preparacion
                .cantidadCentralSugerida
                ?? 0
          });

          this.cargarCatalogos(
            preparacion.idAgencia
          );

          this.cargarCuentasAhorro(
            preparacion
          );

          this.calcular();
        },

        error: (
          error: unknown
        ) => {
          this.error =
            this.obtenerMensajeError(
              error
            );
        }

      });
  }

  // =======================================================
  // CATÁLOGOS
  // =======================================================

  private cargarCatalogos(
    idAgencia: number
  ): void {

    const fechaContable =
      this.texto(
        this.form.get(
          'fechaContable'
        )?.value
      );

    if (!fechaContable) {
      return;
    }

    forkJoin({

      tipos:
        this.tiposComprobantesApi
          .listarPorAgencia(
            idAgencia
          ),

      cajas:
        this.http.get<CajaAbierta[]>(
          `${environment.apiUrl}/cajas/provisiones/abiertas`,
          {
            params: {
              idAgencia:
                String(idAgencia),

              fechaContable
            }
          }
        )

    }).subscribe({

      next: ({
        tipos,
        cajas
      }) => {

        this.tiposComprobantes =
          (tipos ?? [])
            .filter(
              item =>
                item.comprobanteActivo
            );

        this.cajas =
          cajas ?? [];

        this.validarSeleccionComprobantes();
        this.validarSeleccionCaja();
      },

      error: (
        error: unknown
      ) => {

        this.cajas = [];

        this.error =
          this.obtenerMensajeError(
            error
          );
      }

    });
  }

  // =======================================================
  // CAMBIO FECHA CONTABLE
  // =======================================================

  cambiarFechaContable(): void {

    if (
      !this.preparacion ||
      this.desembolsoEjecutado
    ) {
      return;
    }

    this.form.patchValue({
      idCaja: null
    });

    this.cajas = [];

    this.invalidarDistribucion();

    this.cargarCatalogos(
      this.preparacion.idAgencia
    );
  }

  // =======================================================
  // CÁLCULO
  // =======================================================

  calcular(): void {

    if (!this.puedeDesembolsar) {

      this.error =
        'El usuario no tiene permiso para procesar desembolsos.';

      return;
    }

    if (
      !this.preparacion ||
      this.desembolsoEjecutado
    ) {
      return;
    }

    const cantidadCentral =
      this.numero(
        this.form.get(
          'cantidadCentral'
        )?.value
      );

    if (cantidadCentral < 0) {
      this.error =
        'La cantidad de consultas de central de riesgo no puede ser negativa.';

      return;
    }

    this.error = '';
    this.mensaje = '';
    this.calculando = true;

    this.invalidarDistribucion();

    this.api
      .calcular(
        this.idCarteraCredito,
        cantidadCentral
      )
      .pipe(
        finalize(
          () => {
            this.calculando = false;
          }
        )
      )
      .subscribe({

        next: (
          liquidacion:
            LiquidacionDesembolso
        ) => {

          this.liquidacion =
            liquidacion;

          this.actualizarValidadorComprobanteCobros();
        },

        error: (
          error: unknown
        ) => {
          this.error =
            this.obtenerMensajeError(
              error
            );
        }

      });
  }


  // =======================================================
  // VALIDAR DISTRIBUCIÓN
  // =======================================================

 validarDistribucion(): void {

   if (!this.puedeDesembolsar) {

     this.error =
       'El usuario no tiene permiso para validar distribuciones de desembolso.';

     return;
   }

   if (
     !this.preparacion ||
     !this.liquidacion ||
     this.desembolsoEjecutado
   ) {
     return;
   }

    const idCaja =
      this.numero(
        this.form.get(
          'idCaja'
        )?.value
      );

    if (idCaja <= 0) {
      this.error =
        'Debe seleccionar la caja.';

      return;
    }

    const depositos =
      this.obtenerDepositos();

    if (depositos === null) {
      return;
    }

    this.error = '';
    this.mensaje = '';
    this.validando = true;

    this.api
      .validarDistribucion(
        this.idCarteraCredito,
        {
          idCaja,

          valorEfectivo:
            this.numero(
              this.form.get(
                'valorEfectivo'
              )?.value
            ),

          depositos,

          valorCuentaPorPagar:
            this.numero(
              this.form.get(
                'valorCuentaPorPagar'
              )?.value
            )
        }
      )
      .pipe(
        finalize(
          () => {
            this.validando = false;
          }
        )
      )
      .subscribe({

        next: (
          resultado:
            ResultadoValidacionDistribucion
        ) => {

          this.validacionDistribucion =
            resultado;

          if (resultado.cuadra) {
            this.mensaje =
              resultado.mensaje ||
              'La distribución del desembolso cuadra correctamente.';
          } else {
            this.error =
              resultado.mensaje ||
              'La distribución del desembolso no cuadra.';
          }
        },

        error: (
          error: unknown
        ) => {
          this.validacionDistribucion =
            null;

          this.error =
            this.obtenerMensajeError(
              error
            );
        }

      });
  }


  // =======================================================
  // EJECUTAR
  // =======================================================

  ejecutar(): void {

    if (!this.puedeDesembolsar) {

      this.error =
        'El usuario no tiene permiso para ejecutar desembolsos.';

      return;
    }

    if (
      !this.preparacion ||
      !this.liquidacion ||
      this.desembolsoEjecutado
    ) {
      return;
    }

    if (
      !this.validacionDistribucion ||
      !this.validacionDistribucion.cuadra
    ) {
      this.error =
        'Debe validar una distribución correcta antes de ejecutar el desembolso.';

      return;
    }

    this.actualizarValidadorComprobanteCobros();

    if (this.form.invalid) {

      this.form.markAllAsTouched();

      this.error =
        'Revise los datos requeridos antes de ejecutar el desembolso.';

      return;
    }

    const depositos =
      this.obtenerDepositos();

    if (depositos === null) {
      return;
    }

    const tipoComprobanteCobros =
      this.liquidacion.totalComprobante2 > 0
        ? this.texto(
            this.form.get(
              'tipoComprobanteCobros'
            )?.value
          )
        : null;

    const request:
      EjecutarDesembolsoRequest = {

      idCaja:
        this.numero(
          this.form.get(
            'idCaja'
          )?.value
        ),

      fechaContable:
        this.texto(
          this.form.get(
            'fechaContable'
          )?.value
        ),

      fechaDesembolso:
        this.texto(
          this.form.get(
            'fechaDesembolso'
          )?.value
        ),

      tipoComprobanteDesembolso:
        this.texto(
          this.form.get(
            'tipoComprobanteDesembolso'
          )?.value
        ),

      tipoComprobanteCobros,

      cantidadCentral:
        this.numero(
          this.form.get(
            'cantidadCentral'
          )?.value
        ),

      valorEfectivo:
        this.numero(
          this.form.get(
            'valorEfectivo'
          )?.value
        ),

      depositos,

      valorCuentaPorPagar:
        this.numero(
          this.form.get(
            'valorCuentaPorPagar'
          )?.value
        )
    };

    this.error = '';
    this.mensaje = '';
    this.ejecutando = true;

    this.api
      .ejecutar(
        this.idCarteraCredito,
        request
      )
      .pipe(
        finalize(
          () => {
            this.ejecutando = false;
          }
        )
      )
      .subscribe({

        next: (
          resultado:
            ResultadoEjecucionDesembolso
        ) => {

          this.resultado =
            resultado;

          this.desembolsoEjecutado =
            true;

          this.form.disable({
            emitEvent: false
          });

          this.mensaje =
            `Desembolso realizado correctamente. Comprobante ${resultado.tipoComprobanteDesembolso} ${resultado.numeroComprobanteDesembolso}.`;
        },

        error: (
          error: unknown
        ) => {
          this.error =
            this.obtenerMensajeError(
              error
            );
        }

      });
  }


  // =======================================================
  // INVALIDAR DISTRIBUCIÓN
  // =======================================================

  cambioDistribucion(): void {
    this.invalidarDistribucion();
  }


  private invalidarDistribucion(): void {

    this.validacionDistribucion =
      null;

    if (!this.desembolsoEjecutado) {
      this.mensaje = '';
    }
  }


  // =======================================================
  // COMPROBANTE DE COBROS
  // =======================================================

  private actualizarValidadorComprobanteCobros(): void {

    const control =
      this.form.get(
        'tipoComprobanteCobros'
      );

    if (!control) {
      return;
    }

    if (
      this.liquidacion &&
      this.liquidacion.totalComprobante2 > 0
    ) {
      control.setValidators(
        Validators.required
      );
    } else {
      control.clearValidators();

      control.setValue(
        '',
        {
          emitEvent: false
        }
      );
    }

    control.updateValueAndValidity({
      emitEvent: false
    });
  }


  // =======================================================
  // DEPÓSITOS PARA REQUEST
  // =======================================================

  private obtenerDepositos():
    DepositoDesembolso[] | null {

    const resultado:
      DepositoDesembolso[] = [];

    const cuentas =
      new Set<number>();

    for (
      let i = 0;
      i < this.depositos.length;
      i++
    ) {

      const grupo =
        this.depositos.at(i);

      const idCuentaAhorro =
        this.numero(
          grupo.get(
            'idCuentaAhorro'
          )?.value
        );

      const valor =
        this.numero(
          grupo.get(
            'valor'
          )?.value
        );

      if (idCuentaAhorro <= 0) {
        this.error =
          `La cuenta de ahorro de la distribución ${i + 1} no es válida.`;

        return null;
      }

      if (valor <= 0) {
        this.error =
          `El valor de la distribución ${i + 1} debe ser mayor que cero.`;

        return null;
      }

      if (
        cuentas.has(
          idCuentaAhorro
        )
      ) {
        this.error =
          'No puede utilizar la misma cuenta de ahorro más de una vez en la distribución.';

        return null;
      }

      cuentas.add(
        idCuentaAhorro
      );

      resultado.push({
        idCuentaAhorro,
        valor
      });
    }

    return resultado;
  }

  // =======================================================
  // VALIDAR SELECCIÓN DE CAJA
  // =======================================================

  private validarSeleccionCaja(): void {

    const idCaja =
      this.numero(
        this.form.get(
          'idCaja'
        )?.value
      );

    if (idCaja <= 0) {
      return;
    }

    const existe =
      this.cajas.some(
        caja =>
          Number(caja.idCaja) ===
          idCaja
      );

    if (!existe) {
      this.form.patchValue({
        idCaja: null
      });

      this.invalidarDistribucion();
    }
  }

  private validarSeleccionComprobantes(): void {

    const disponibles =
      new Set(
        this.tiposComprobantes.map(
          item =>
            item.tipoComprobante
        )
      );

    const tipoDesembolso =
      this.texto(
        this.form.get(
          'tipoComprobanteDesembolso'
        )?.value
      );

    const tipoCobros =
      this.texto(
        this.form.get(
          'tipoComprobanteCobros'
        )?.value
      );

    if (
      tipoDesembolso &&
      !disponibles.has(
        tipoDesembolso
      )
    ) {
      this.form.patchValue({
        tipoComprobanteDesembolso:
          ''
      });
    }

    if (
      tipoCobros &&
      !disponibles.has(
        tipoCobros
      )
    ) {
      this.form.patchValue({
        tipoComprobanteCobros:
          ''
      });
    }
  }


  // =======================================================
  // TOTALES DE DISTRIBUCIÓN
  // =======================================================

  get totalDepositosLocal(): number {

    return this.depositos.controls
      .reduce(
        (
          total,
          grupo
        ) =>
          total +
          this.numero(
            grupo.get(
              'valor'
            )?.value
          ),
        0
      );
  }


  get totalDistribuidoLocal(): number {

    return (
      this.numero(
        this.form.get(
          'valorEfectivo'
        )?.value
      ) +
      this.totalDepositosLocal +
      this.numero(
        this.form.get(
          'valorCuentaPorPagar'
        )?.value
      )
    );
  }


  get diferenciaDistribucionLocal(): number {

    if (!this.liquidacion) {
      return 0;
    }

    return (
      this.liquidacion.valorDistribuir -
      this.totalDistribuidoLocal
    );
  }


  // =======================================================
  // ESTADOS
  // =======================================================

  get tieneCobros(): boolean {
    return (
      (this.liquidacion
        ?.totalComprobante2 ?? 0) > 0
    );
  }


  get puedeValidarDistribucion(): boolean {

    return (
      this.puedeDesembolsar &&
      !!this.preparacion &&
      !!this.liquidacion &&
      !this.cargando &&
      !this.calculando &&
      !this.validando &&
      !this.ejecutando &&
      !this.desembolsoEjecutado
    );
  }


  get puedeEjecutar(): boolean {

    return (
      this.puedeDesembolsar &&
      this.puedeValidarDistribucion &&
      !!this.validacionDistribucion &&
      this.validacionDistribucion.cuadra &&
      this.form.valid
    );
  }

  // =======================================================
  // NOMBRE COMPROBANTE
  // =======================================================

  nombreTipoComprobante(
    tipo: TipoComprobante
  ): string {

    const codigo =
      tipo.tipoComprobante
        ?.trim();

    const nombre =
      tipo.nombreTipoComprobante
        ?.trim();

    if (
      codigo &&
      nombre
    ) {
      return `${codigo} - ${nombre}`;
    }

    return (
      nombre ||
      codigo ||
      ''
    );
  }

  // =======================================================
  // NOMBRE DE CAJA
  // =======================================================

  nombreCaja(
    caja: CajaAbierta
  ): string {

    const codigo =
      this.texto(
        caja.codigoCaja
      );

    const nombre =
      this.texto(
        caja.descripcionCaja ??
        caja.nombreCaja
      );

    if (
      codigo &&
      nombre
    ) {
      return `${codigo} - ${nombre}`;
    }

    return (
      nombre ||
      codigo ||
      `Caja ${caja.idCaja}`
    );
  }

  // =======================================================
  // FORMATO
  // =======================================================

  moneda(
    valor:
      number |
      null |
      undefined
  ): string {

    return this.formatoMoneda.format(
      Number(
        valor ?? 0
      )
    );
  }


  fecha(
    valor:
      string |
      null |
      undefined
  ): string {

    if (!valor) {
      return '-';
    }

    const partes =
      valor.split('-');

    if (partes.length !== 3) {
      return valor;
    }

    return (
      `${partes[2]}/${partes[1]}/${partes[0]}`
    );
  }


  // =======================================================
  // NAVEGACIÓN
  // =======================================================

  volver(): void {

    void this.router.navigate([
      '/cartera/originacion/desembolso'
    ]);
  }


  // =======================================================
  // HELPERS
  // =======================================================

  private fechaHoy(): string {

    const ahora =
      new Date();

    const anio =
      ahora.getFullYear();

    const mes =
      String(
        ahora.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const dia =
      String(
        ahora.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${anio}-${mes}-${dia}`;
  }


  private numero(
    valor: unknown
  ): number {

    if (
      valor === null ||
      valor === undefined ||
      valor === ''
    ) {
      return 0;
    }

    const numero =
      Number(valor);

    return Number.isFinite(numero)
      ? numero
      : 0;
  }


  private texto(
    valor: unknown
  ): string {

    if (
      valor === null ||
      valor === undefined
    ) {
      return '';
    }

    return String(valor)
      .trim();
  }


  private obtenerMensajeError(
    error: unknown
  ): string {

    if (
      typeof error === 'object' &&
      error !== null
    ) {

      const httpError =
        error as {
          error?: {
            message?: string;
            mensaje?: string;
            error?: string;
          } | string;
          message?: string;
        };

      if (
        typeof httpError.error ===
        'string'
      ) {
        return httpError.error;
      }

      if (
        httpError.error &&
        typeof httpError.error ===
        'object'
      ) {

        if (
          httpError.error.message
        ) {
          return httpError.error.message;
        }

        if (
          httpError.error.mensaje
        ) {
          return httpError.error.mensaje;
        }

        if (
          httpError.error.error
        ) {
          return httpError.error.error;
        }
      }

      if (httpError.message) {
        return httpError.message;
      }
    }

    return (
      'No fue posible completar la operación.'
    );
  }

  // =======================================================
  // CUENTAS DE AHORRO DEL ASOCIADO
  // =======================================================

  private cargarCuentasAhorro(
    preparacion: PreparacionDesembolso
  ): void {

    this.cuentasAhorroDisponibles = [];

    this.api
      .buscarCuentasAsociado(
        preparacion.documento
      )
      .subscribe({

        next: (res: any) => {

          const data =
            res?.data ?? [];

          this.cuentasAhorroDisponibles =
            data.filter(
              (cuenta: CuentaAhorroDisponible) => {

                const mismaPersona =
                  Number(
                    cuenta.id_datos_personal
                  ) ===
                  Number(
                    preparacion.idDatosPersonal
                  );

                const activa =
                  String(
                    cuenta.codigo_estado ?? ''
                  )
                    .trim()
                    .toUpperCase() === 'A';

                const idFormaAhorro =
                  Number(
                    cuenta.id_forma_ahorro ??
                    cuenta.idFormaAhorro
                  );

                const noEsAportes =
                  idFormaAhorro !== 1;

                return (
                  mismaPersona &&
                  activa &&
                  noEsAportes
                );
              }
            );
        },

        error: (
          error: unknown
        ) => {

          this.cuentasAhorroDisponibles = [];

          this.error =
            this.obtenerMensajeError(
              error
            );
        }

      });
  }

  cambiarValorEfectivo(
    valor: number
  ): void {

    this.form.get(
      'valorEfectivo'
    )?.setValue(
      this.numero(valor),
      {
        emitEvent: false
      }
    );

    this.invalidarDistribucion();
  }

  cambiarValorCuentaPorPagar(
    valor: number
  ): void {

    this.form.get(
      'valorCuentaPorPagar'
    )?.setValue(
      this.numero(valor),
      {
        emitEvent: false
      }
    );

    this.invalidarDistribucion();
  }

  cambiarValorDeposito(
    index: number,
    valor: number
  ): void {

    const grupo =
      this.depositos.at(index);

    grupo.get(
      'valor'
    )?.setValue(
      this.numero(valor),
      {
        emitEvent: false
      }
    );

    this.invalidarDistribucion();
  }

}
