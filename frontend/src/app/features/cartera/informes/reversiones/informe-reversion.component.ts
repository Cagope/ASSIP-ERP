import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, Subject, debounceTime, distinctUntilChanged, switchMap, of, catchError, takeUntil } from 'rxjs';
import * as XLSX from 'xlsx';
import { InformeReversion, InformeReversionFiltro, InformeReversionAsociado } from './informe-reversion.models';
import { InformeReversionService } from './informe-reversion.service';

/** Presenta el informe de auditoría de reversiones y permite exportar resultados. */
@Component({
  selector: 'app-informe-reversion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './informe-reversion.component.html',
  styleUrls: ['./informe-reversion.component.scss']
})
export class InformeReversionComponent implements OnDestroy {
  private readonly api = inject(InformeReversionService);
  private readonly hoy = this.fechaLocal(new Date());
  filtro: InformeReversionFiltro = {
    fechaInicial: this.fechaLocal(new Date(new Date().getFullYear(), new Date().getMonth(), 1)),
    fechaFinal: this.hoy,
    idAgencia: null,
    asociado: ''
  };
  registros: InformeReversion[] = [];
  cargando = false;
  consultado = false;
  error = '';
  private secuencia = 0;
  sugerencias: InformeReversionAsociado[] = [];
  mostrarSugerencias = false;
  private readonly busqueda = new Subject<string>();
  private readonly destruir = new Subject<void>();

  constructor() {
    this.busqueda.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap(texto => texto.trim().length >= 2
        ? this.api.buscarAsociados(texto).pipe(catchError(() => of([] as InformeReversionAsociado[])))
        : of([] as InformeReversionAsociado[])),
      takeUntil(this.destruir)
    ).subscribe(opciones => {
      this.sugerencias = opciones;
      this.mostrarSugerencias = opciones.length > 0;
    });
  }

  /** Solicita sugerencias cuando cambia la cédula o el nombre digitado. */
  buscarAsociado(texto: string): void {
    this.invalidarConsulta();
    this.filtro.asociado = texto;
    this.sugerencias = [];
    this.mostrarSugerencias = false;
    this.busqueda.next(texto);
  }

  /** Selecciona el documento del asociado sin usar IDs internos. */
  seleccionarAsociado(asociado: InformeReversionAsociado): void {
    this.invalidarConsulta();
    this.filtro.asociado = asociado.documento;
    this.sugerencias = [];
    this.mostrarSugerencias = false;
    this.busqueda.next('');
  }

  /** Invalida resultados y exportación cuando se modifican los filtros. */
  invalidarConsulta(): void {
    this.secuencia++;
    this.consultado = false;
    this.registros = [];
    this.error = '';
  }

  /** Restablece los filtros y resultados sin ejecutar una consulta. */
  limpiar(): void {
    this.invalidarConsulta();
    this.filtro = {
      fechaInicial: this.fechaLocal(new Date(new Date().getFullYear(), new Date().getMonth(), 1)),
      fechaFinal: this.hoy,
      idAgencia: null,
      asociado: ''
    };
    this.sugerencias = [];
    this.mostrarSugerencias = false;
    this.busqueda.next('');
  }

  /** Libera la suscripción de búsqueda al salir del informe. */
  ngOnDestroy(): void {
    this.destruir.next();
    this.destruir.complete();
    this.busqueda.complete();
  }

  /** Resume el valor total de las reversiones mostradas. */
  get totalReversado(): number {
    return this.registros.reduce((s, r) => s + Number(r.valorReversado || 0), 0);
  }

  /** Valida fechas y consulta el informe sin modificar movimientos. */
  consultar(): void {
    if (this.cargando) return;
    this.error = '';
    if (!this.filtro.fechaInicial || !this.filtro.fechaFinal || this.filtro.fechaInicial > this.filtro.fechaFinal) {
      this.error = 'Seleccione un rango de fechas válido.';
      return;
    }
    const desde = new Date(`${this.filtro.fechaInicial}T12:00:00`);
    const hasta = new Date(`${this.filtro.fechaFinal}T12:00:00`);
    const limite = new Date(desde);
    limite.setMonth(limite.getMonth() + 24);
    if (hasta > limite) {
      this.error = 'El rango no puede superar 24 meses.';
      return;
    }
    const solicitud = ++this.secuencia;
    this.cargando = true;
    this.consultado = false;
    this.registros = [];
    this.api.consultar({ ...this.filtro })
      .pipe(finalize(() => { if (solicitud === this.secuencia) this.cargando = false; }))
      .subscribe({
        next: registros => {
          if (solicitud !== this.secuencia) return;
          this.registros = registros;
          this.consultado = true;
        },
        error: err => {
          if (solicitud !== this.secuencia) return;
          this.error = err?.error?.message || err?.error?.mensaje || 'No fue posible consultar el informe.';
        }
      });
  }

  /** Genera un archivo Excel a partir de los datos efectivamente consultados. */
  exportarExcel(): void {
    if (this.cargando || !this.consultado || !this.registros.length) return;
    const filas = this.registros.map(r => ({
      'Fecha reversión': r.fechaReversion,
      'Fecha pago': r.fechaPagoOriginal,
      'Agencia crédito': r.nombreAgencia,
      'Código agencia receptora': String(r.codigoAgenciaReceptora ?? ''),
      'Agencia receptora': r.nombreAgenciaReceptora ?? '',
      'Código línea de crédito': String(r.codigoLineaCredito ?? ''),
      'Línea de crédito': r.nombreLineaCredito ?? '',
      'Documento': r.documento,
      'Asociado': r.asociado,
      'Pagaré': r.pagare,
      'Comprobante original': r.comprobanteOriginal,
      'Comprobante reversión': r.comprobanteReversion,
      'Usuario': r.usuario,
      'Funcionario': r.funcionario,
      'Motivo': r.motivo,
      'Capital': Number(r.capital || 0),
      'Interés causado': Number(r.interesCausado || 0),
      'Interés ingreso': Number(r.interesIngreso || 0),
      'Interés anticipado': Number(r.interesAnticipado || 0),
      'Mora': Number(r.mora || 0),
      'Seguros': Number(r.seguros || 0),
      'Aportes': Number(r.aportes || 0),
      'Papelería': Number(r.papeleria || 0),
      'Fondo garantía': Number(r.fondoGarantia || 0),
      'Otros': Number(r.otros || 0),
      'Valor reversado': Number(r.valorReversado || 0)
    }));
    const hoja = XLSX.utils.json_to_sheet(filas);
    // Mantiene los códigos como texto para conservar ceros iniciales.
    const encabezados = Object.keys(filas[0]);
    for (const nombre of ['Código agencia receptora', 'Código línea de crédito']) {
      const columna = encabezados.indexOf(nombre);
      if (columna < 0) continue;
      for (let fila = 1; fila <= filas.length; fila++) {
        const celda = hoja[XLSX.utils.encode_cell({ r: fila, c: columna })];
        if (celda) { celda.t = 's'; celda.v = String(celda.v ?? ''); }
      }
    }
    hoja['!cols'] = Object.keys(filas[0]).map(k => ({ wch: Math.max(15, Math.min(38, k.length + 5)) }));
    const libro = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(libro, hoja, 'Reversiones');
    XLSX.writeFile(libro, `reversiones_cartera_${this.filtro.fechaInicial}_${this.filtro.fechaFinal}.xlsx`);
  }

  /** Devuelve la fecha local sin desplazamientos UTC. */
  private fechaLocal(fecha: Date): string {
    const y = fecha.getFullYear();
    const m = String(fecha.getMonth() + 1).padStart(2, '0');
    const d = String(fecha.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}
