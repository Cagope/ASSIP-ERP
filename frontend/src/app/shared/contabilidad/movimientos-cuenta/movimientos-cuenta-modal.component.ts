
import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

/** Movimiento de una cuenta contable, independiente del informe que abre la ventana. */
export interface MovimientoCuentaModal {
  idAuxiliarContable: number;
  idAgencia: number;
  codigoAgencia: string;
  nombreAgencia: string;
  idCatalogoCuenta: number;
  fechaAuxiliar: string;
  tipoComprobante: string;
  numeroComprobante: string;
  idDatosPersonal: number | null;
  detalleMovimiento: string;
  valorDebito: number;
  valorCredito: number;
}

/** Resultado paginado que recibe la ventana sin ejecutar solicitudes HTTP. */
export interface MovimientosCuentaModalPagina {
  codigoCuenta: string;
  ano: number;
  mes: number;
  pagina: number;
  tamanoPagina: number;
  hayMas: boolean;
  movimientos: MovimientoCuentaModal[];
}

/** Ventana compartida de movimientos contables; la pantalla origen controla la consulta. */
@Component({
  selector: 'app-movimientos-cuenta-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './movimientos-cuenta-modal.component.html',
  styleUrl: './movimientos-cuenta-modal.component.scss'
})
export class MovimientosCuentaModalComponent {
  @Input({ required: true }) codigoCuenta = '';
  @Input() nombreCuenta = '';
  @Input() nombreAgencia = '';
  @Input() fechaInicial = '';
  @Input() fechaFinal = '';
  @Input() cargando = false;
  @Input() error = '';
  @Input() pagina: MovimientosCuentaModalPagina | null = null;

  @Output() cerrar = new EventEmitter<void>();
  @Output() cambiarPagina = new EventEmitter<number>();
  @Output() seleccionarComprobante = new EventEmitter<MovimientoCuentaModal>();

  private readonly formatoMoneda = new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });

  /** Presenta el importe sin alterar su valor numérico. */
  dinero(valor: number | null | undefined): string {
    return valor == null ? '—' : this.formatoMoneda.format(valor);
  }

  /** Presenta una fecha ISO en formato colombiano, sin conversión de zona horaria. */
  formatearFecha(fecha: string): string {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) {
      return fecha || '—';
    }

    const [ano, mes, dia] = fecha.split('-');
    return `${dia}/${mes}/${ano}`;
  }

  /** Solicita al componente origen la página anterior, si corresponde. */
  anterior(): void {
    if (!this.cargando && this.pagina && this.pagina.pagina > 0) {
      this.cambiarPagina.emit(this.pagina.pagina - 1);
    }
  }

  /** Solicita al componente origen la página siguiente, si existe. */
  siguiente(): void {
    if (!this.cargando && this.pagina?.hayMas) {
      this.cambiarPagina.emit(this.pagina.pagina + 1);
    }
  }

  /** Notifica la selección; el origen decide cómo abrir el comprobante autorizado. */
  abrirComprobante(movimiento: MovimientoCuentaModal): void {
    this.seleccionarComprobante.emit(movimiento);
  }

  /** Cierra la ventana a petición del usuario. */
  cerrarVentana(): void {
    this.cerrar.emit();
  }
}
