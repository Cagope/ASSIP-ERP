import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CuentasAhorroApi } from './cuentas-ahorro.api';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../core/auth/session.service';
import { DepositosCatalogosApi } from '../catalogos/depositos.catalogos.api';
import { FormasAhorroApi, FormaAhorro } from '../formas-ahorro/formas-ahorro.api';
import { CuentasConjuntasComponent } from './cuentas-conjuntas.component';
import { BeneficiariosComponent } from './beneficiarios.component';
import { PoderesComponent } from './poderes.component';

@Component({
  standalone: true,
  selector: 'app-cuentas-ahorro',
  templateUrl: './cuentas-ahorro.component.html',
  styleUrls: ['./cuentas-ahorro.component.scss'],
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent,
    BeneficiariosComponent,
    PoderesComponent,
    CuentasConjuntasComponent
  ]


})
export class CuentasAhorroComponent {

  private readonly api = inject(CuentasAhorroApi);
  private readonly session = inject(SessionService);
  private readonly catalogos = inject(DepositosCatalogosApi);
  private readonly formasApi = inject(FormasAhorroApi);

  agenciaActiva: any = null;

  filtros = {
    documento: '',
    nombres: '',
    primer_apellido: '',
    segundo_apellido: '',
    codigo_cuenta: ''
  };

  resultados: any[] = [];
  asociadosAgrupados: any[] = [];

  cargandoBusqueda = false;
  errorBusqueda = '';

  persona: any = null;
  validacion: any = null;
  detalleCuenta: any = null;

  modoNuevaCuenta = false;
  cargando = false;
  paso = 1;

  fechaFinalCalculada = '';

  fechaAperturaSugerida =
    new Date().toISOString().substring(0, 10);


  estados: any[] = [];
  tiposGmf: any[] = [];
  formasAhorro: FormaAhorro[] = [];
  codigoCuentaSugerido = '';

  accionesConjunta: any[] = [];

  nuevo = this.nuevoModelo();

  accionDetalle: 'NINGUNA' | 'BENEFICIARIOS' | 'PODERES' | 'CONJUNTAS' = 'NINGUNA';

  constructor() {
    this.agenciaActiva = this.session.getAgenciaActiva();
    this.cargarCatalogos();
  }

  private nuevoModelo() {
    return {
      idAgencia: 0,
      idFormaAhorro: 0,
      idDatosPersonal: 0,
      cuentaConjunta: 'N',
      accionConjunta: 'N',
      documentoRelacionado: '',
      gmfCuentaCuenta: 'N',
      retencionFuenteCuenta: true,
      plazoCuenta: 0,
      cuotaMensualCuenta: 0,
      tasa: 0,
      estadoCuenta: 'A',
      beneficiarios: [],
      poderes: [],
      usuarioId: 0
    };
  }

  private cargarCatalogos() {
    this.catalogos.obtenerEstadosAhorro().subscribe({
      next: (res) => this.estados = res ?? []
    });

    this.catalogos.obtenerTiposGmf().subscribe({
      next: (res) => this.tiposGmf = res ?? []
    });

    this.formasApi.listar().subscribe({
      next: (res) => {
        const idAgencia =
          this.agenciaActiva?.idAgencia ??
          this.agenciaActiva?.id_agencia ??
          0;

        this.formasAhorro = (res ?? [])
          .filter((f: any) => Number(f.idAgencia) === Number(idAgencia))
          .sort((a, b) =>
            String(a.codigoForma).localeCompare(String(b.codigoForma))
          );
      }
    });

    this.catalogos.obtenerAccionesCuentasConjuntas().subscribe({
      next: (res) => {
        this.accionesConjunta = (res ?? [])
          .filter(a => a.codigoAccion !== 'N');
      }
    });

  }

  buscar() {
    const activos = Object.values(this.filtros).some(v => String(v).trim() !== '');

    if (!activos) {
      this.errorBusqueda = 'Ingrese al menos un criterio de búsqueda.';
      this.resultados = [];
      this.asociadosAgrupados = [];
      return;
    }

    this.cargandoBusqueda = true;
    this.errorBusqueda = '';
    this.resultados = [];
    this.asociadosAgrupados = [];

    const agencia = (window as any)?.appSession?.getAgenciaActiva?.() ?? null;

    const agenciaCodigo =
      agencia?.codigo_agencia ||
      agencia?.codigo ||
      agencia?.id_agencia ||
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
        codigo_agencia: agenciaCodigo
      }
    };

    this.api.buscarCuentas(req).subscribe({
      next: (res: any) => {
        this.resultados = res?.data ?? [];
        this.asociadosAgrupados = this.agruparPorAsociado(this.resultados);

        if (!this.resultados.length) {
          this.errorBusqueda = 'No se encontraron resultados.';
        }
      },
      error: () => {
        this.errorBusqueda = 'Error consultando cuentas.';
      },
      complete: () => this.cargandoBusqueda = false
    });
  }

  private agruparPorAsociado(lista: any[]): any[] {
    const mapa = new Map<string, any>();

    for (const item of lista) {
      const idDatosPersonal =
        item.id_datos_personal ??
        item.idDatosPersonal ??
        item.id_datos_personal_titular ??
        item.idDatosPersonalTitular;

      const documento = item.documento ?? '';
      const nombre = item.nombre_completo ?? item.nombreTitular ?? '';

      const key = String(idDatosPersonal || documento);

      if (!mapa.has(key)) {
        mapa.set(key, {
          idDatosPersonal,
          documento,
          nombreCompleto: nombre,
          cuentas: []
        });
      }

      mapa.get(key).cuentas.push(item);
    }

    return Array.from(mapa.values());
  }

  limpiar() {
    this.filtros = {
      documento: '',
      nombres: '',
      primer_apellido: '',
      segundo_apellido: '',
      codigo_cuenta: ''
    };

    this.resultados = [];
    this.asociadosAgrupados = [];
    this.errorBusqueda = '';
  }

  seleccionarPersona(c: any) {
    const idCuenta = c.id_cuenta_ahorro ?? c.idCuentaAhorro;

    if (!idCuenta) {
      return;
    }

    this.modoNuevaCuenta = false;
    this.cargando = true;

    this.api.detalle(idCuenta).subscribe({
      next: (det: any) => {
        this.detalleCuenta = det;

        this.nuevo = {
          idAgencia: det.idAgencia,
          idFormaAhorro: det.idFormaAhorro,
          idDatosPersonal: det.idDatosPersonal,
          cuentaConjunta: det.cuentaConjunta,
          accionConjunta: det.accionConjunta,
          documentoRelacionado: det.documentoRelacionado ?? '',
          gmfCuentaCuenta: det.gmfCuentaCuenta,
          retencionFuenteCuenta: det.retencionFuenteCuenta,
          plazoCuenta: det.plazoCuenta,
          cuotaMensualCuenta: det.cuotaMensualCuenta,
          tasa: det.tasa,
          estadoCuenta: det.estadoCuenta ?? det.codigoEstado ?? 'A',
          beneficiarios: det.beneficiarios ?? [],
          poderes: det.poderes ?? [],
          usuarioId: 0
        };

        this.persona = {
          nombre_completo: det.nombreTitular,
          documento: det.documento,
          agencia: det.nombreAgencia,
          forma: det.nombreForma,
          codigo: det.codigoCuenta
        };

        this.aplicarReglasPorForma();
        this.calcularFechaFinal();
        this.paso = 2;
      },
      error: () => alert('No se pudo cargar el detalle.'),
      complete: () => this.cargando = false
    });
  }

  abrirNuevaCuenta(asociado: any) {
    this.modoNuevaCuenta = true;
    this.detalleCuenta = null;
    this.validacion = null;
    this.fechaFinalCalculada = '';

    this.nuevo = this.nuevoModelo();

    this.nuevo.idAgencia =
      this.agenciaActiva?.id_agencia ??
      this.agenciaActiva?.idAgencia ??
      0;

    this.nuevo.idDatosPersonal = asociado.idDatosPersonal;

    this.persona = {
      nombre_completo: asociado.nombreCompleto,
      documento: asociado.documento,
      agencia:
        this.agenciaActiva?.nombre_agencia ??
        this.agenciaActiva?.nombreAgencia ??
        '',
      forma: 'Nueva cuenta',
      codigo: ''
    };

    this.fechaAperturaSugerida =
      new Date().toISOString().substring(0, 10);

    this.calcularFechaFinal();

    this.paso = 2;
  }

  validar() {
    if (!this.nuevo.idFormaAhorro || this.nuevo.idFormaAhorro === 0) {
      alert('Seleccione la forma de ahorro.');
      return;
    }

    this.cargando = true;

    this.api.validar(this.nuevo).subscribe(
      (res: any) => {
        this.validacion = res;
        res.ok ? alert('Validación exitosa') : alert(res.mensaje);
      },
      null,
      () => this.cargando = false
    );
  }

  crear() {
    if (!this.validacion?.ok) {
      return alert('Debe validar antes de crear');
    }

    this.cargando = true;

    this.api.crear(this.nuevo).subscribe(
      (res: any) => {
        if (res.ok) {
          this.cargarDetalle(res.idCuentaAhorro);
        } else {
          alert(res.mensaje);
        }
      },
      null,
      () => this.cargando = false
    );
  }

  cargarDetalle(idCuenta: number) {
    this.api.detalle(idCuenta).subscribe(
      (det: any) => {
        this.detalleCuenta = det;
        this.paso = 3;
      }
    );
  }

  regresar() {
    this.paso = 1;
    this.persona = null;
    this.validacion = null;
    this.detalleCuenta = null;
    this.modoNuevaCuenta = false;
    this.accionDetalle = 'NINGUNA';
  }

  calcularFechaFinal() {
    const apertura = this.detalleCuenta?.fechaAperturaCuenta ?? new Date().toISOString().substring(0, 10);

    if (!apertura) {
      this.fechaFinalCalculada = '';
      return;
    }

    const fecha = new Date(apertura);
    fecha.setMonth(fecha.getMonth() + Number(this.nuevo.plazoCuenta ?? 0));
    this.fechaFinalCalculada = this.nuevo.plazoCuenta > 0
      ? fecha.toISOString().substring(0, 10)
      : '';
  }

  onConjuntaChange() {
    if (this.nuevo.cuentaConjunta === 'N') {
      this.nuevo.accionConjunta = 'N';
      this.nuevo.documentoRelacionado = '';
      return;
    }

    if (this.nuevo.cuentaConjunta === 'S') {
      this.nuevo.accionConjunta = '';
    }
  }

  filtrosActivos(): boolean {
    return Object.values(this.filtros).some(v => String(v).trim() !== '');
  }

  aplicarReglasPorForma() {
    const forma = this.formasAhorro.find(f =>
      this.obtenerIdForma(f) === Number(this.nuevo.idFormaAhorro)
    );

    if (!forma) {
      this.codigoCuentaSugerido = '';
      return;
    }

    const consecutivo =
      Number((forma as any).consecutivoForma ?? 0);

    this.codigoCuentaSugerido =
      consecutivo > 0 ? String(consecutivo + 1) : '';

    if (forma.codigoForma === '01') {
      this.nuevo.gmfCuentaCuenta = 'N';
      this.nuevo.retencionFuenteCuenta = false;
    }

    if (forma.codigoForma !== '07') {
      this.nuevo.plazoCuenta = 0;
      this.nuevo.cuotaMensualCuenta = 0;
      this.nuevo.tasa = 0;
      this.fechaFinalCalculada = '';
    } else {
      this.calcularFechaFinal();
    }

    this.validacion = null;
  }

  obtenerIdForma(f: FormaAhorro): number {
    return Number((f as any).id ?? (f as any).idFormaAhorro ?? 0);
  }

  esPrimeraFilaAsociado(c: any, index: number): boolean {
    if (index === 0) {
      return true;
    }

    const actual =
      c.id_datos_personal ??
      c.idDatosPersonal ??
      c.documento;

    const anterior =
      this.resultados[index - 1]?.id_datos_personal ??
      this.resultados[index - 1]?.idDatosPersonal ??
      this.resultados[index - 1]?.documento;

    return actual !== anterior;
  }

  asociadoDesdeFila(c: any) {
    return {
      idDatosPersonal:
        c.id_datos_personal ??
        c.idDatosPersonal,

      documento: c.documento,

      nombreCompleto:
        c.nombre_completo ??
        c.nombreTitular ??
        ''
    };
  }

  esForma07(): boolean {
    const forma = this.formasAhorro.find(f =>
      this.obtenerIdForma(f) === Number(this.nuevo.idFormaAhorro)
    );

    return forma?.codigoForma === '07';
  }

  abrirDetalleCuenta(c: any, accion: 'BENEFICIARIOS' | 'PODERES' | 'CONJUNTAS') {
    const idCuenta = c.id_cuenta_ahorro ?? c.idCuentaAhorro;

    if (!idCuenta) {
      return;
    }

    this.cargando = true;
    this.accionDetalle = accion;

    this.api.detalle(idCuenta).subscribe({
      next: (det: any) => {
        this.detalleCuenta = det;
        this.paso = 3;
      },
      error: () => alert('No se pudo cargar el detalle de la cuenta.'),
      complete: () => this.cargando = false
    });
  }

  regresarDetalle() {
    this.paso = 1;
    this.detalleCuenta = null;
    this.accionDetalle = 'NINGUNA';
  }


}
