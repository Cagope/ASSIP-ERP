
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TransferenciaCuentasApi } from './transferencia-cuentas.api';

import {
  TransferenciaCuenta,
  TransferenciaEntrada,
  TransferenciaFiltro,
  TransferenciaPreview,
  TransferenciaResultado
} from './transferencia-cuentas.models';

import {
  NumericFormatDirective
} from '../../../../shared/utils/numeric-format.directive';


// =========================================================
// COMPONENTE
// TRANSFERENCIA DE FONDOS ENTRE CUENTAS
// =========================================================
//
// Flujo de operación:
//
// 1. Buscar y seleccionar cuenta origen.
// 2. Buscar y seleccionar cuenta destino.
// 3. Capturar los datos de la transferencia.
// 4. Consultar la vista previa.
// 5. Confirmar y aplicar la transferencia.
//
// Las búsquedas de origen y destino son independientes.
//
// =========================================================

@Component({
  selector: 'app-transferencia-cuentas',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NumericFormatDirective
  ],
  templateUrl: './transferencia-cuentas.component.html',
  styleUrls: ['./transferencia-cuentas.component.scss']
})
export class TransferenciaCuentasComponent {

  // =======================================================
  // SERVICIOS
  // =======================================================

  private readonly api = inject(TransferenciaCuentasApi);


  // =======================================================
  // FORMATO MONETARIO
  // =======================================================

  readonly dinero = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0
  });


  // =======================================================
  // FILTROS DE BÚSQUEDA
  // =======================================================

  filtrosOrigen: TransferenciaFiltro = this.crearFiltros();

  filtrosDestino: TransferenciaFiltro = this.crearFiltros();


  // =======================================================
  // RESULTADOS DE BÚSQUEDA
  // =======================================================

  resultadosOrigen: TransferenciaCuenta[] = [];

  resultadosDestino: TransferenciaCuenta[] = [];


  // =======================================================
  // CUENTAS SELECCIONADAS
  // =======================================================

  origen: TransferenciaCuenta | null = null;

  destino: TransferenciaCuenta | null = null;


  // =======================================================
  // DATOS DE LA TRANSFERENCIA
  // =======================================================

  valor: number | null = null;

  concepto = '';

  fechaProceso = this.fechaActual();


  // =======================================================
  // VISTA PREVIA Y RESULTADO
  // =======================================================

  vista: TransferenciaPreview | null = null;

  resultado: TransferenciaResultado | null = null;

  private entradaPrevisualizada: string | null = null;


  // =======================================================
  // ESTADOS DE OPERACIÓN
  // =======================================================

  buscandoOrigen = false;

  buscandoDestino = false;

  procesando = false;

  error = '';

  mensaje = '';


  // =======================================================
  // ESTADOS DERIVADOS
  // =======================================================

  get cargando(): boolean {
    return this.buscandoOrigen
      || this.buscandoDestino
      || this.procesando;
  }

  get puedeBuscarDestino(): boolean {
    return this.origen !== null
      && !this.procesando
      && this.resultado === null;
  }

  get puedeConsultarPreview(): boolean {
    return this.origen !== null
      && this.destino !== null
      && this.valor !== null
      && Number.isFinite(this.valor)
      && this.valor > 0
      && this.concepto.trim().length > 0
      && this.concepto.trim().length <= 200
      && !this.cargando
      && this.resultado === null;
  }

  get puedeAplicar(): boolean {
    if (
      this.vista === null
      || this.entradaPrevisualizada === null
      || this.cargando
      || this.resultado !== null
    ) {
      return false;
    }

    try {
      return JSON.stringify(this.entrada())
        === this.entradaPrevisualizada;
    } catch {
      return false;
    }
  }


  // =======================================================
  // CREAR FILTROS VACÍOS
  // =======================================================

  private crearFiltros(): TransferenciaFiltro {
    return {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };
  }


  // =======================================================
  // FECHA ACTUAL DEL NAVEGADOR
  // =======================================================
  //
  // Se construye en formato yyyy-MM-dd sin utilizar
  // toISOString(), para evitar cambios de fecha por UTC.
  //
  // El backend continúa validando que corresponda
  // a la fecha de trabajo del servidor.
  //
  // =======================================================

  private fechaActual(): string {

    const fecha = new Date();

    const anio = fecha.getFullYear();

    const mes = String(
      fecha.getMonth() + 1
    ).padStart(2, '0');

    const dia = String(
      fecha.getDate()
    ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }


  // =======================================================
  // VALIDAR FILTROS DE BÚSQUEDA
  // =======================================================

  private tieneFiltros(
    filtros: TransferenciaFiltro
  ): boolean {

    return Boolean(
      filtros.documento?.trim()
      || filtros.nombres?.trim()
      || filtros.primerApellido?.trim()
      || filtros.segundoApellido?.trim()
    );
  }


  // =======================================================
  // BUSCAR CUENTAS ORIGEN
  // =======================================================
  //
  // Solo presenta cuentas que permiten débito.
  //
  // La validación definitiva del tipo de captación
  // se realiza nuevamente en el backend.
  //
  // =======================================================

  buscarOrigen(): void {

    if (this.cargando || this.resultado !== null) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    if (!this.tieneFiltros(this.filtrosOrigen)) {
      this.error =
        'Indique al menos un criterio para buscar la cuenta origen.';
      return;
    }

    this.resultadosOrigen = [];
    this.buscandoOrigen = true;

    const filtros = { ...this.filtrosOrigen };

    this.api.buscarCuentas(filtros).subscribe({

      next: cuentas => {

        this.resultadosOrigen = cuentas.filter(
          cuenta => cuenta.permiteDebito
        );

        this.buscandoOrigen = false;
      },

      error: error => {
        this.buscandoOrigen = false;
        this.fallar(error);
      }
    });
  }


  // =======================================================
  // SELECCIONAR CUENTA ORIGEN
  // =======================================================

  seleccionarOrigen(
    cuenta: TransferenciaCuenta
  ): void {

    if (this.cargando || this.resultado !== null) {
      return;
    }

    if (!cuenta.permiteDebito) {
      this.error =
        'Esta cuenta no permite transferencias de salida.';
      return;
    }

    const cambioOrigen =
      this.origen?.idCuentaAhorro !== cuenta.idCuentaAhorro;

    this.origen = cuenta;

    this.resultadosOrigen = [];

    this.error = '';
    this.mensaje = '';

    if (cambioOrigen) {

      // Al cambiar el origen, el destino debe
      // seleccionarse nuevamente.

      this.destino = null;

      this.filtrosDestino = this.crearFiltros();

      this.resultadosDestino = [];

      this.valor = null;

      this.concepto = '';
    }

    this.invalidarPreview();
  }


  // =======================================================
  // CAMBIAR CUENTA ORIGEN
  // =======================================================

  cambiarOrigen(): void {

    if (this.cargando || this.resultado !== null) {
      return;
    }

    this.origen = null;

    this.destino = null;

    this.resultadosOrigen = [];

    this.resultadosDestino = [];

    this.filtrosDestino = this.crearFiltros();

    this.valor = null;

    this.concepto = '';

    this.error = '';
    this.mensaje = '';

    this.invalidarPreview();
  }


  // =======================================================
  // BUSCAR CUENTAS DESTINO
  // =======================================================
  //
  // La cuenta destino puede pertenecer:
  //
  // - Al mismo asociado.
  // - A otro asociado.
  // - A la misma agencia.
  // - A otra agencia.
  //
  // Se permiten cuentas operativas de los tipos
  // de captación 1, 2 y 3.
  //
  // Se excluye la cuenta origen.
  //
  // =======================================================

  buscarDestino(): void {

    if (!this.puedeBuscarDestino || this.cargando) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    if (!this.tieneFiltros(this.filtrosDestino)) {
      this.error =
        'Indique al menos un criterio para buscar la cuenta destino.';
      return;
    }

    this.resultadosDestino = [];
    this.buscandoDestino = true;

    const filtros = { ...this.filtrosDestino };

    this.api.buscarCuentas(filtros).subscribe({

      next: cuentas => {

        const idOrigen = this.origen?.idCuentaAhorro;

        this.resultadosDestino = cuentas.filter(
          cuenta =>
            cuenta.operativa
            && ['1', '2', '3'].includes(
              cuenta.tipoCaptacion
            )
            && cuenta.idCuentaAhorro !== idOrigen
        );

        this.buscandoDestino = false;
      },

      error: error => {
        this.buscandoDestino = false;
        this.fallar(error);
      }
    });
  }


  // =======================================================
  // SELECCIONAR CUENTA DESTINO
  // =======================================================

  seleccionarDestino(
    cuenta: TransferenciaCuenta
  ): void {

    if (!this.puedeBuscarDestino || this.cargando) {
      return;
    }

    if (!this.origen) {
      this.error =
        'Primero debe seleccionar la cuenta origen.';
      return;
    }

    if (
      cuenta.idCuentaAhorro
      === this.origen.idCuentaAhorro
    ) {
      this.error =
        'La cuenta destino debe ser diferente de la cuenta origen.';
      return;
    }

    if (
      !cuenta.operativa
      || !['1', '2', '3'].includes(cuenta.tipoCaptacion)
    ) {
      this.error =
        'La cuenta seleccionada no permite recibir transferencias.';
      return;
    }

    this.destino = cuenta;

    this.resultadosDestino = [];

    this.error = '';
    this.mensaje = '';

    this.generarConcepto();

    this.invalidarPreview();
  }


  // =======================================================
  // CAMBIAR CUENTA DESTINO
  // =======================================================

  cambiarDestino(): void {

    if (this.cargando || this.resultado !== null) {
      return;
    }

    this.destino = null;

    this.resultadosDestino = [];

    this.error = '';
    this.mensaje = '';

    this.invalidarPreview();
  }


  // =======================================================
  // INVALIDAR VISTA PREVIA
  // =======================================================
  //
  // Se ejecuta cuando cambian:
  //
  // - Cuenta origen.
  // - Cuenta destino.
  // - Valor.
  // - Fecha.
  // - Concepto.
  //
  // Evita confirmar una contabilización preparada
  // con información diferente a la actual.
  //
  // =======================================================

  invalidarPreview(): void {

    if (this.procesando || this.resultado !== null) {
      return;
    }

    this.vista = null;

    this.entradaPrevisualizada = null;

    this.mensaje = '';
  }


  // =======================================================
  // CONSTRUIR ENTRADA DE TRANSFERENCIA
  // =======================================================

  private entrada(): TransferenciaEntrada {

    if (!this.origen) {
      throw new Error(
        'Debe seleccionar la cuenta origen.'
      );
    }

    if (!this.destino) {
      throw new Error(
        'Debe seleccionar la cuenta destino.'
      );
    }

    if (
      this.origen.idCuentaAhorro
      === this.destino.idCuentaAhorro
    ) {
      throw new Error(
        'La cuenta origen y destino deben ser diferentes.'
      );
    }

    if (
      this.valor === null
      || !Number.isFinite(this.valor)
      || this.valor <= 0
    ) {
      throw new Error(
        'Indique un valor de transferencia mayor que cero.'
      );
    }

    if (
      Math.round(this.valor * 100)
      !== this.valor * 100
    ) {
      throw new Error(
        'El valor admite máximo dos decimales.'
      );
    }

    if (!this.concepto.trim()) {
      throw new Error(
        'Debe indicar el concepto de la transferencia.'
      );
    }

    if (this.concepto.trim().length > 200) {
      throw new Error(
        'El concepto no puede superar 200 caracteres.'
      );
    }

    if (!this.fechaProceso) {
      throw new Error(
        'Debe indicar la fecha de trabajo.'
      );
    }

    return {

      idCuentaOrigen:
        this.origen.idCuentaAhorro,

      idCuentaDestino:
        this.destino.idCuentaAhorro,

      valor:
        this.valor,

      fechaProceso:
        this.fechaProceso,

      concepto:
        this.concepto.trim()
    };
  }


  // =======================================================
  // CONSULTAR VISTA PREVIA
  // =======================================================

  verPreview(): void {

    if (this.cargando || this.resultado !== null) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.invalidarPreview();

    try {

      const entrada = this.entrada();

      const firmaEntrada = JSON.stringify(entrada);

      this.procesando = true;

      this.api.preview(entrada).subscribe({

        next: vista => {

          this.vista = vista;

          this.entradaPrevisualizada = firmaEntrada;

          this.procesando = false;
        },

        error: error => {

          this.procesando = false;

          this.fallar(error);
        }
      });

    } catch (error) {

      this.fallar(error);
    }
  }


  // =========================================================
  // APLICAR TRANSFERENCIA
  // =========================================================

  aplicar(): void {

    if (!this.puedeAplicar) {
      return;
    }

    let entrada: TransferenciaEntrada;

    try {
      entrada = this.entrada();
    } catch (error) {
      this.fallar(error);
      return;
    }

    const confirmado = window.confirm(
      '¿Confirma la transferencia de fondos '
      + 'desde la cuenta '
      + this.origen?.codigoCuenta
      + ' hacia la cuenta '
      + this.destino?.codigoCuenta
      + ' por '
      + this.dinero.format(entrada.valor)
      + '?'
    );

    if (!confirmado) {
      return;
    }

    this.error = '';
    this.mensaje = '';

    this.procesando = true;

    this.api.aplicar(entrada).subscribe({

      next: resultado => {

        const comprobantes = resultado.comprobantes
          .map(
            c => `Agencia ${c.idAgencia}: `
              + `${c.tipoComprobante} ${c.numeroComprobante.trim()}`
          )
          .join(' | ');

        // Limpiar el formulario después de registrar.
        this.procesando = false;
        this.nueva();

        // Conservar únicamente la confirmación.
        this.mensaje =
          'Transferencia aplicada correctamente por '
          + this.dinero.format(resultado.valor)
          + '. ' + comprobantes;
      },

      error: error => {

        this.procesando = false;

        this.fallar(error);
      }
    });
  }


  // =======================================================
  // NUEVA TRANSFERENCIA
  // =======================================================

  nueva(): void {

    if (this.cargando) {
      return;
    }

    this.filtrosOrigen = this.crearFiltros();

    this.filtrosDestino = this.crearFiltros();

    this.resultadosOrigen = [];

    this.resultadosDestino = [];

    this.origen = null;

    this.destino = null;

    this.valor = null;

    this.concepto = '';

    this.fechaProceso = this.fechaActual();

    this.vista = null;

    this.resultado = null;

    this.entradaPrevisualizada = null;

    this.error = '';

    this.mensaje = '';
  }


  // =======================================================
  // MANEJO DE ERRORES
  // =======================================================

  private fallar(
    error: unknown
  ): void {

    const respuesta = error as {
      error?: {
        message?: string;
        mensaje?: string;
      };
      message?: string;
    };

    this.error =
      respuesta?.error?.message
      ?? respuesta?.error?.mensaje
      ?? respuesta?.message
      ?? 'No fue posible realizar la operación.';

    this.procesando = false;
  }

  private generarConcepto(): void {

    if (!this.origen || !this.destino) {
      return;
    }

    this.concepto =
      `Transferencia de fondos del asociado ` +
      `CC ${this.origen.documento}, ` +
      `cuenta ${this.origen.codigoCuenta}, ` +
      `al asociado CC ${this.destino.documento}, ` +
      `cuenta ${this.destino.codigoCuenta}.`;

  }

}
