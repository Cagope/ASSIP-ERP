import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import {
  CdatsApi,
  CdatAmortizacionDTO,
  CdatAperturaPreviewDTO
} from './cdats.api';

import { CdatsUpsertValores } from './cdats-upsert.valores';
import { SessionService } from '../../../core/auth/session.service';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';
import { CuentasApi } from '../../../shared/cuentas/cuentas.api';
import { CatalogosApi, CodigoNombreDTO } from '../../../shared/catalogos/catalogos.api';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

@Component({
  selector: 'app-cdats-upsert',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NumericFormatDirective, ReactiveFormsModule],
  templateUrl: './cdats-upsert.component.html',
  styleUrls: ['./cdats-upsert.component.scss'],
})
export class CdatsUpsertComponent extends CdatsUpsertValores implements OnInit {

  paso = 1;

  agenciaActiva: any = null;
  amortizaciones: CdatAmortizacionDTO[] = [];

  filtros = {
    documento: '',
    nombres: '',
    primer_apellido: '',
    segundo_apellido: '',
    codigo_cuenta: ''
  };

  resultados: any[] = [];
  persona: any = null;

  cargandoBusqueda = false;
  guardando = false;

  errorBusqueda = '';

  proximoCodigo = '';

  cuentasAhorroDisponibles: any[] = [];
  cuentaAportes: any = null;

  documentoCotitular = '';
  cotitularSeleccionado: any = null;

  tiposComprobantes: TipoComprobante[] = [];
  cajasAbiertas: any[] = [];

  parentescos: CodigoNombreDTO[] = [];

  fechaVencimientoCalculada = '';

  previewContable: CdatAperturaPreviewDTO | null = null;

  mostrarBeneficiarios = false;
  mostrarCheques = false;
  mostrarDepositos = false;
  mostrarBancos = false;
  mostrarTraslados = false;
  mostrarEfectivo = true;

  constructor(
    private api: CdatsApi,
    private route: ActivatedRoute,
    private router: Router,
    private session: SessionService,
    private tiposComprobantesApi: TiposComprobantesApi,
    private cuentasApi: CuentasApi,
    private catalogosApi: CatalogosApi
  ) {
    super();
  }

  ngOnInit(): void {
    this.agenciaActiva = this.session.getAgenciaActiva();

    this.catalogosApi.listarParentescos().subscribe({
      next: data => {
        this.parentescos = data ?? [];
      }
    });

    this.api.listarAmortizaciones().subscribe({
      next: data => {
        this.amortizaciones = data ?? [];

        if (!this.model.amortizacionDeposito && this.amortizaciones.length) {
          this.model.amortizacionDeposito = this.amortizaciones[0].codigoAmortizacion;
        }
      }
    });

    this.configurarAutocompleteBancos();
    this.configurarAutocompleteTraslados();

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.api.obtenerPorId(+id).subscribe({
        next: data => {
          this.model = data;
          this.paso = 2;
        },
        error: err => {
          this.error = err.error?.message || 'No se pudo cargar el CDAT.';
        }
      });
    }
  }

  private configurarAutocompleteBancos(): void {
    this.cuentaBancoCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(texto => {
        const idAgencia = this.model.idAgencia;

        if (!idAgencia || typeof texto !== 'string' || texto.length < 2) {
          return of([]);
        }

        return this.cuentasApi.buscarBancos(idAgencia, texto);
      })
    ).subscribe(data => {
      this.cuentasBanco = data ?? [];
    });
  }

  private configurarAutocompleteTraslados(): void {
    this.cuentaTrasladoCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(texto => {
        const idAgencia = this.model.idAgencia;

        if (!idAgencia || typeof texto !== 'string' || texto.length < 2) {
          return of([]);
        }

        return this.cuentasApi.buscarTrasladosAgencias(idAgencia, texto);
      })
    ).subscribe(data => {
      this.cuentasTraslado = data ?? [];
    });
  }

  buscar(): void {
    this.error = '';
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
        codigo_cuenta: this.filtros.codigo_cuenta,
        codigo_agencia: agenciaCodigo,
        cuenta_activa: 'A'
      }
    };

    this.api.buscarCuentas(req).subscribe({
      next: (res: any) => {
        const data = res?.data ?? [];

        this.resultados = data.filter((c: any) =>
          String(c.codigo_estado ?? '').trim().toUpperCase() === 'A'
        );

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
      segundo_apellido: '',
      codigo_cuenta: ''
    };

    this.resultados = [];
    this.persona = null;
    this.error = '';
    this.errorBusqueda = '';
    this.cuentasAhorroDisponibles = [];
    this.cuentaAportes = null;
    this.proximoCodigo = '';
  }

  seleccionarPersona(c: any): void {
    this.error = '';

    const idDatosPersonal = c.id_datos_personal ?? c.idDatosPersonal;

    if (!idDatosPersonal) {
      this.error = 'No se pudo identificar el asociado.';
      return;
    }

    const cuentasPersona = this.resultados.filter((x: any) =>
      Number(x.id_datos_personal ?? x.idDatosPersonal) === Number(idDatosPersonal)
    );

    this.cuentaAportes = cuentasPersona.find((x: any) =>
      Number(x.id_forma_ahorro ?? x.idFormaAhorro) === 1
    );

    this.cuentasAhorroDisponibles = cuentasPersona.filter((x: any) =>
      Number(x.id_forma_ahorro ?? x.idFormaAhorro) !== 1 &&
      String(x.codigo_estado ?? '').trim().toUpperCase() === 'A'
    );

    if (!this.cuentaAportes) {
      this.error = 'El asociado no tiene cuenta de aportes activa.';
      return;
    }

    if (!this.cuentasAhorroDisponibles.length) {
      this.error = 'El asociado no tiene cuentas de ahorro activas diferentes a aportes.';
      return;
    }

    this.persona = {
      id_datos_personal: idDatosPersonal,
      documento: c.documento,
      nombre_completo: c.nombre_completo,
      agencia: c.nombre_agencia,
      forma: this.cuentaAportes.nombre_forma_ahorro,
      codigo: this.cuentaAportes.codigo_cuenta,
      saldo_actual_cuenta: this.cuentaAportes.saldo_actual_cuenta
    };

    this.model.idDatosPersonal = idDatosPersonal;
    this.model.idCuentaAportes = this.cuentaAportes.id_cuenta_ahorro;
    this.model.idCuentaAhorro = null;
    this.model.idAgencia =
      this.agenciaActiva?.id_agencia ??
      this.agenciaActiva?.idAgencia ??
      null;

    this.cargarTiposComprobantes();

    this.paso = 2;

    this.api.obtenerProximoCodigo().subscribe({
      next: codigo => {
        this.proximoCodigo = codigo;
      }
    });
  }

  regresarValidacion(): void {
    this.paso = 1;
    this.error = '';
    this.persona = null;
    this.cuentasAhorroDisponibles = [];
    this.cuentaAportes = null;
    this.proximoCodigo = '';
    this.model = this.initModel();
  }

  guardar(): void {
    if (this.guardando) return;

    this.error = '';

    const errorValidacion = this.validarGuardar();

    if (errorValidacion) {
      this.error = errorValidacion;
      return;
    }

    if (!this.previewContable || !this.previewContable.cuadrado) {
      this.error = 'Debe generar y validar el preview contable antes de guardar.';
      return;
    }

    this.guardando = true;

    this.model.beneficiarios = this.beneficiarios;
    this.model.cheques = this.cheques;

    this.model.mediosPago = {
      idCaja: this.model.idCaja,
      valorEfectivo: this.model.valorEfectivo || 0,
      valorCheques: this.model.valorCheque || 0,
      valorDepositos: this.model.valorDepositos || 0,
      valorBancos: this.model.valorBanco || 0,
      valorTrasladosAgencias: this.model.valorTrasladosAgencias || 0,
      cheques: this.cheques || [],
      depositos: this.depositos || [],
      bancos: this.bancos || [],
      trasladosAgencias: this.trasladosAgencias || []
    };

    this.api.guardar(this.model).subscribe({
      next: () => {
        this.guardando = false;
        this.router.navigate(['/cdat/cdats']);
      },
      error: err => {
        this.error = err.error?.message || 'Error guardando el CDAT.';
        this.guardando = false;
      }
    });
  }

  private validarGuardar(): string | null {
    if (!this.model.idDatosPersonal) {
      return 'Debe seleccionar primero el asociado.';
    }

    if (!this.model.idCuentaAhorro) {
      return 'Debe seleccionar la cuenta de ahorro.';
    }

    if (!this.model.fechaAperturaCdat) {
      return 'Debe ingresar la fecha de apertura.';
    }

    if (!this.model.plazoMeses || this.model.plazoMeses <= 0) {
      return 'El plazo en meses debe ser mayor a cero.';
    }

    if (!this.model.valorAperturaCdat || this.model.valorAperturaCdat <= 0) {
      return 'El valor del CDAT debe ser mayor a cero.';
    }

    const suma =
      Number(this.model.valorEfectivo || 0) +
      Number(this.model.valorCheque || 0) +
      Number(this.model.valorDepositos || 0) +
      Number(this.model.valorBanco || 0) +
      Number(this.model.valorTrasladosAgencias || 0);

    if (suma !== Number(this.model.valorAperturaCdat || 0)) {
      return 'La suma de los valores de apertura no coincide con el valor del CDAT.';
    }

    if (this.requiereCaja() && !this.model.idCaja) {
      return 'Debe seleccionar la caja destino para efectivo o cheques.';
    }

    if (this.model.cuentaConjunta === 'S') {
      if (!this.model.accionConjunta) {
        return 'Debe seleccionar la acción conjunta.';
      }

      if (!this.model.idDatosPersonalCotitular) {
        return 'Debe seleccionar el cotitular.';
      }
    }

    if (!this.model.tipoComprobante) {
      return 'Debe seleccionar el tipo de comprobante.';
    }

    if (!this.model.fechaComprobante) {
      return 'Debe ingresar la fecha del comprobante.';
    }

    return null;
  }

  filtrosActivos(): boolean {
    return Object.values(this.filtros).some(v => String(v).trim() !== '');
  }

  calcularPlazos(): void {
    if (!this.model.fechaAperturaCdat || !this.model.plazoMeses) {
      this.fechaVencimientoCalculada = '';
      this.model.fechaVencimientoCdat = null;
      this.model.plazoDias = null;
      this.limpiarPreview();
      return;
    }

    const fechaApertura = new Date(this.model.fechaAperturaCdat + 'T00:00:00');
    const meses = Number(this.model.plazoMeses);

    const fechaVencimiento = new Date(fechaApertura);
    fechaVencimiento.setMonth(fechaVencimiento.getMonth() + meses);

    this.fechaVencimientoCalculada = fechaVencimiento.toISOString().substring(0, 10);
    this.model.fechaVencimientoCdat = this.fechaVencimientoCalculada;

    if (!this.model.fechaComprobante && this.model.fechaAperturaCdat) {
      this.model.fechaComprobante = this.model.fechaAperturaCdat;
    }

    this.cargarCajasAbiertas();

    const diferenciaMs = fechaVencimiento.getTime() - fechaApertura.getTime();
    this.model.plazoDias = Math.round(diferenciaMs / (1000 * 60 * 60 * 24));
  }

  calcularTasaEfectivaAnual(): void {
    const na = Number(this.model.tasaNominalAnual || 0);

    if (na <= 0) {
      this.model.tasaEfectivaAnual = 0;
      return;
    }

    const tasaDecimal = na / 100;
    const efectiva = Math.pow(1 + (tasaDecimal / 12), 12) - 1;

    this.model.tasaEfectivaAnual = Number((efectiva * 100).toFixed(6));
  }

  onChangeFechaApertura(): void {
    if (!this.model.fechaComprobante && this.model.fechaAperturaCdat) {
      this.model.fechaComprobante = this.model.fechaAperturaCdat;
      this.limpiarPreview();
    }

    this.calcularPlazos();
  }

  cargarConsecutivoComprobante(): void {
    this.model.numeroComprobante = '';

    if (!this.model.idAgencia || !this.model.tipoComprobante) {
      return;
    }

    const tc = this.tiposComprobantes.find(
      t => t.tipoComprobante === this.model.tipoComprobante
    );

    if (!tc) {
      this.error = 'El tipo de comprobante seleccionado no es válido para la agencia.';
      return;
    }

    const siguiente = Number(tc.cscComprobante || 0) + 1;

    this.model.numeroComprobante = siguiente
      .toString()
      .padStart(10, '0');
  }

  cargarTiposComprobantes(): void {
    this.tiposComprobantes = [];
    this.model.tipoComprobante = '';
    this.model.numeroComprobante = '';

    if (!this.model.idAgencia) {
      return;
    }

    this.tiposComprobantesApi
      .listarPorAgencia(this.model.idAgencia)
      .subscribe({
        next: data => {
          this.tiposComprobantes = data ?? [];
        },
        error: () => {
          this.error = 'No se pudieron cargar los tipos de comprobante.';
        }
      });
  }

  cargarCajasAbiertas(): void {
    this.cajasAbiertas = [];

    if (!this.model.idAgencia || !this.model.fechaComprobante) {
      return;
    }

    this.api.obtenerCajasAbiertas(
      this.model.idAgencia,
      this.model.fechaComprobante
    ).subscribe({
      next: data => {
        this.cajasAbiertas = data ?? [];
      },
      error: err => {
        this.error = err.error?.message || 'No se pudieron cargar las cajas abiertas.';
      }
    });
  }

  onChangeCuentaConjunta(): void {
    if (this.model.cuentaConjunta === 'N') {
      this.model.accionConjunta = null;
      this.model.idDatosPersonalCotitular = null;
      this.documentoCotitular = '';
      this.cotitularSeleccionado = null;
    }
  }

  buscarCotitular(): void {
    this.error = '';
    this.cotitularSeleccionado = null;
    this.model.idDatosPersonalCotitular = null;

    if (!this.documentoCotitular || !this.documentoCotitular.trim()) {
      this.error = 'Debe ingresar el documento del cotitular.';
      return;
    }

    if (this.documentoCotitular.trim() === String(this.persona?.documento ?? '').trim()) {
      this.error = 'El cotitular no puede ser el mismo titular.';
      return;
    }

    this.api.buscarCotitular(this.documentoCotitular.trim()).subscribe({
      next: data => {
        this.cotitularSeleccionado = data;
        this.model.idDatosPersonalCotitular = data.idDatosPersonal;
      },
      error: err => {
        this.error = err.error?.message || 'No se encontró el cotitular.';
      }
    });
  }

  onSelectCuentaDeposito(): void {
    const cuenta = this.cuentasAhorroDisponibles.find(
      c => c.id_cuenta_ahorro === this.nuevoDeposito.idCuentaAhorro
    );

    if (!cuenta) {
      this.nuevoDeposito.codigoCuenta = '';
      this.nuevoDeposito.saldoDisponible = 0;
      return;
    }

    this.nuevoDeposito.codigoCuenta = cuenta.codigo_cuenta;
    this.nuevoDeposito.saldoDisponible = Number(cuenta.saldo_actual_cuenta || 0);
    this.limpiarPreview();
  }

  generarPreviewContable(): void {

    this.error = '';
    this.previewContable = null;

    const errorValidacion = this.validarGuardar();

    if (errorValidacion) {
      this.error = errorValidacion;
      return;
    }

    this.model.beneficiarios = this.beneficiarios;
    this.model.cheques = this.cheques;

    this.model.mediosPago = {
      idCaja: this.model.idCaja,
      valorEfectivo: this.model.valorEfectivo || 0,
      valorCheques: this.model.valorCheque || 0,
      valorDepositos: this.model.valorDepositos || 0,
      valorBancos: this.model.valorBanco || 0,
      valorTrasladosAgencias: this.model.valorTrasladosAgencias || 0,
      cheques: this.cheques || [],
      depositos: this.depositos || [],
      bancos: this.bancos || [],
      trasladosAgencias: this.trasladosAgencias || []
    };

    this.api.previewAperturaContable(this.model).subscribe({
      next: res => {
        this.previewContable = res;
      },
      error: err => {
        this.error = err.error?.message || 'Error generando preview contable.';
      }
    });
  }

  limpiarPreview(): void {
    this.previewContable = null;
  }

  totalMediosPago(): number {
    return Number(this.model.valorEfectivo || 0)
      + Number(this.model.valorCheque || 0)
      + Number(this.model.valorDepositos || 0)
      + Number(this.model.valorBanco || 0)
      + Number(this.model.valorTrasladosAgencias || 0);
  }

  mediosPagoCuadran(): boolean {
    return this.totalMediosPago() === Number(this.model.valorAperturaCdat || 0);
  }

}
