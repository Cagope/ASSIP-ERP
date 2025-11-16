import {
  Component,
  Input,
  Output,
  EventEmitter,
  inject,
  OnChanges,
  SimpleChanges,
  HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReportingService } from '../../../shared/reporting/reporting.service';

@Component({
  selector: 'app-extracto-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './extracto-modal.component.html',
  styleUrls: ['./extracto-modal.component.scss']
})
export class ExtractoModalComponent implements OnChanges {
  @Input() formaAhorro!: string;        // ✅ Recibida desde el botón del detalle
  @Input() codigoCuenta!: string;       // ✅ Cuenta seleccionada
  @Input() asociado: any = null;        // ✅ Datos del asociado (documento, nombre)
  @Input() visible = false;
  @Output() cerrar = new EventEmitter<void>();

  private readonly reporting = inject(ReportingService);

  fechaInicial = '';
  fechaFinal = '';
  cargando = false;
  movimientos: any[] = [];
  resumen: any | null = null;
  hoy = new Date();

  // 🔹 Cierra el modal con tecla ESC
  @HostListener('document:keydown.escape')
  onEsc(): void {
    if (this.visible) this.onCerrar();
  }

  // 🔹 Al abrir, establece fechas por defecto
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue === true) {
      const hoy = new Date();
      const inicioMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
      this.fechaInicial = this.toISODate(inicioMes);
      this.fechaFinal = this.toISODate(hoy);
    }
  }

  /** 🔙 Cierra el modal */
  onCerrar(): void {
    this.cerrar.emit();
    this.cargando = false;
  }

  /** 🔍 Consulta el extracto desde el backend */
  async consultar(): Promise<void> {
    if (!this.formaAhorro) {
      console.warn('⚠️ Forma de ahorro no definida al abrir el modal.');
      alert('Falta la forma de ahorro. Debe venir desde la cuenta seleccionada.');
      return;
    }
    if (!this.fechaInicial || !this.fechaFinal) {
      alert('Debe seleccionar ambas fechas.');
      return;
    }
    if (this.fechaInicial > this.fechaFinal) {
      alert('La fecha inicial no puede ser mayor que la final.');
      return;
    }
    if (!this.codigoCuenta) {
      alert('Cuenta inválida.');
      return;
    }

    this.cargando = true;
    this.movimientos = [];
    this.resumen = null;

    try {
      const res = await this.reporting.obtenerExtractoCuentaAvanzado(
        this.formaAhorro,
        this.codigoCuenta,
        this.fechaInicial,
        this.fechaFinal
      );

      const rows = this.normalizeRows(res);
      this.movimientos = rows;
      this.calcularResumen();
    } catch (err) {
      console.error('❌ Error al consultar extracto:', err);
      alert('No se pudo obtener el extracto.');
    } finally {
      this.cargando = false;
    }
  }

  /** 📊 Calcula totales del extracto */
  private calcularResumen(): void {
    if (!this.movimientos.length) {
      this.resumen = null;
      return;
    }

    const totalDebitos = this.movimientos.reduce(
      (s, m) => s + (Number(m.valor_debito) || 0),
      0
    );
    const totalCreditos = this.movimientos.reduce(
      (s, m) => s + (Number(m.valor_credito) || 0),
      0
    );
    const saldoInicial =
      this.movimientos[0]?.saldo_inicial ??
      this.movimientos[0]?.saldo_resultante ??
      0;
    const saldoFinal =
      this.movimientos[this.movimientos.length - 1]?.saldo_resultante ?? 0;

    this.resumen = { saldoInicial, totalDebitos, totalCreditos, saldoFinal };
  }

  /** 🧹 Normaliza nombres snake/camel */
  private normalizeRows(res: any): any[] {
    const base = Array.isArray(res)
      ? res
      : res?.data ?? res?.rows ?? res?.result ?? res?.items ?? [];
    return (base as any[]).map((r: any) => ({
      fecha_movimiento:
        r.fecha_movimiento ?? r.fechaMovimiento ?? r.fecha ?? r.fecha_mov,
      tipo_movimiento:
        r.tipo_movimiento ?? r.tipoMovimiento ?? r.movimiento,
      numero_comprobante:
        r.numero_comprobante ?? r.comprobante ?? r.numeroComprobante,
      valor_debito: r.valor_debito ?? r.debito ?? r.valorDebito ?? 0,
      valor_credito: r.valor_credito ?? r.credito ?? r.valorCredito ?? 0,
      saldo_resultante:
        r.saldo_resultante ?? r.saldo ?? r.saldoResultante ?? 0,
      saldo_inicial: r.saldo_inicial ?? r.saldoInicial ?? null
    }));
  }

  /** 🗓️ Convierte a ISO */
  private toISODate(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  /** 🖨 Imprimir extracto */
  onImprimir(): void {
    const printContent = document.getElementById('extracto-print-area')?.innerHTML;
    if (!printContent) return;

    const ventana = window.open('', '_blank', 'width=900,height=700');
    ventana?.document.write(`
      <html>
        <head>
          <title>Extracto ${this.codigoCuenta}</title>
          <style>
            body { font-family: Arial, sans-serif; margin: 25px; }
            h2, h3 { margin: 0; text-align: center; }
            header { display: flex; align-items: center; justify-content: space-between; border-bottom: 2px solid #000; margin-bottom: 10px; }
            header .logo img { width: 90px; }
            header .info { text-align: center; flex: 1; }
            header .fecha-emision { font-size: 12px; text-align: right; }
            .datos-cuenta { margin-top: 10px; font-size: 13px; }
            table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 12px; }
            th, td { border: 1px solid #ccc; padding: 6px; }
            th { background-color: #f4f4f4; }
            .num { text-align: right; }
            .resumen { margin-top: 10px; font-weight: bold; }
            footer { margin-top: 15px; text-align: center; font-size: 11px; border-top: 1px solid #ccc; padding-top: 5px; }
            @media print { .no-print { display: none; } }
          </style>
        </head>
        <body onload="window.print();window.close()">
          ${printContent}
        </body>
      </html>
    `);
    ventana?.document.close();
  }
}
