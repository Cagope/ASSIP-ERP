import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CuentasAhorroApi } from './cuentas-ahorro.api';
import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { SessionService } from '../../../core/auth/session.service';
import { DepositosCatalogosApi } from '../catalogos/depositos.catalogos.api';

@Component({
  standalone: true,
  selector: 'app-cuentas-ahorro',
  templateUrl: './cuentas-ahorro.component.html',
  styleUrls: ['./cuentas-ahorro.component.scss'],
  imports: [CommonModule, FormsModule, HeaderActionsComponent]
})
export class CuentasAhorroComponent {

  private readonly api = inject(CuentasAhorroApi);
  private readonly session = inject(SessionService);
  private readonly catalogos = inject(DepositosCatalogosApi);

  // Agencia activa del usuario
  agenciaActiva: any = null;

  // ================================================================
  // PASO 1 – BUSCADOR
  // ================================================================
  filtros = {
    documento: '',
    nombres: '',
    primer_apellido: '',
    segundo_apellido: '',
    codigo_cuenta: ''
  };

  resultados: any[] = [];
  cargandoBusqueda = false;
  errorBusqueda = '';

  // ================================================================
  // PASO 2 – FORM / EDICIÓN
  // ================================================================
  persona: any = null;
  validacion: any = null;

  nuevo = {
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

  detalleCuenta: any = null;
  cargando = false;
  paso = 1;

  fechaFinalCalculada: string = '';

  // ================================
  // Catálogos reales desde backend
  // ================================
  estados: any[] = [];
  tiposGmf: any[] = [];

  accionesConjunta = [
    { codigo: 'T', nombre: 'Todos deben firmar' },
    { codigo: 'O', nombre: 'Cualquiera puede firmar' }
  ];

  constructor() {
    this.agenciaActiva = this.session.getAgenciaActiva();
    this.cargarCatalogos();
  }

  // ================================================================
  // ✔ Cargar catálogos de depósitos
  // ================================================================
  private cargarCatalogos() {
    // ESTADOS
    this.catalogos.obtenerEstadosAhorro().subscribe({
      next: (res) => (this.estados = res ?? [])
    });

    // TIPOS GMF
    this.catalogos.obtenerTiposGmf().subscribe({
      next: (res) => (this.tiposGmf = res ?? [])
    });
  }

  // ================================================================
  // 🔍 BUSCAR CUENTAS
  // ================================================================
  buscar() {

    const activos = Object.values(this.filtros).some(v => String(v).trim() !== '');
    if (!activos) {
      this.errorBusqueda = 'Ingrese al menos un criterio de búsqueda.';
      this.resultados = [];
      return;
    }

    this.cargandoBusqueda = true;
    this.errorBusqueda = '';
    this.resultados = [];

    // Agencia desde sesión
    const agencia = (window as any)?.appSession?.getAgenciaActiva?.()
        ?? null;

    const agenciaCodigo = agencia?.codigo_agencia
                          || agencia?.codigo
                          || agencia?.id_agencia
                          || null;

    const req = {
      schema: 'depositos',
      view: 'vw_depositos_cuentas_ahorro_total',
      filters: {
        documento: this.filtros.documento,
        nombres: this.filtros.nombres,
        primer_apellido: this.filtros.primer_apellido,
        segundo_apellido: this.filtros.segundo_apellido,
        codigo_cuenta: this.filtros.codigo_cuenta,
        codigo_agencia: agenciaCodigo   // restricción crítica
      }
    };

    this.api.buscarCuentas(req).subscribe({
      next: (res: any) => {
        this.resultados = res?.data ?? [];
        if (!this.resultados.length) {
          this.errorBusqueda = 'No se encontraron resultados.';
        }
      },
      error: () => {
        this.errorBusqueda = 'Error consultando cuentas.';
      },
      complete: () => (this.cargandoBusqueda = false)
    });
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
    this.errorBusqueda = '';
  }

  // ================================================================
  // ✔ SELECCIONAR PERSONA O CUENTA
  // ================================================================
  seleccionarPersona(c: any) {

    const idCuenta = c.id_cuenta_ahorro ?? c.idCuentaAhorro;

    if (!idCuenta) {
      this.persona = c;

      this.nuevo.idAgencia =
        this.agenciaActiva?.id_agencia ??
        this.agenciaActiva?.idAgencia ??
        0;

      this.nuevo.idDatosPersonal =
        c.id_datos_personal ??
        c.idDatosPersonal ??
        0;

      // establecer reglas iniciales automáticamente
      this.aplicarReglasPorForma();

      this.paso = 2;
      return;
    }


    // EDICIÓN — Backend valida agencia
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

        this.calcularFechaFinal();
        this.paso = 2;
      },
      error: () => alert('No se pudo cargar el detalle.'),
      complete: () => (this.cargando = false)
    });
  }

  validar() {
    this.cargando = true;

    this.api.validar(this.nuevo).subscribe(
      (res: any) => {
        this.validacion = res;
        res.ok ? alert('Validación exitosa') : alert(res.mensaje);
      },
      null,
      () => (this.cargando = false)
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
      () => (this.cargando = false)
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
  }

  calcularFechaFinal() {
    const apertura = this.detalleCuenta?.fechaAperturaCuenta;
    if (!apertura) {
      this.fechaFinalCalculada = '';
      return;
    }

    const fecha = new Date(apertura);
    fecha.setMonth(fecha.getMonth() + Number(this.nuevo.plazoCuenta ?? 0));
    this.fechaFinalCalculada = fecha.toISOString().substring(0, 10);
  }

  onConjuntaChange() {
    // Si no es conjunta, forzar valores coherentes
    if (this.nuevo.cuentaConjunta === 'N') {
      this.nuevo.accionConjunta = 'N';
      this.nuevo.documentoRelacionado = '';
      return;
    }

    // Si se vuelve conjunta y no tiene acción, asignar valor por defecto
    if (!this.nuevo.accionConjunta || this.nuevo.accionConjunta === 'N') {
      this.nuevo.accionConjunta = 'T'; // primera opción válida
    }
  }


  filtrosActivos(): boolean {
    return Object.values(this.filtros).some(v => String(v).trim() !== '');
  }

  aplicarReglasPorForma() {
    // Forma 01 = APORTES — gmf SIEMPRE N y reten SIEMPRE N
    if (this.nuevo.idFormaAhorro == 1) {
      this.nuevo.gmfCuentaCuenta = 'N';
      this.nuevo.retencionFuenteCuenta = false;
    }
  }

}
