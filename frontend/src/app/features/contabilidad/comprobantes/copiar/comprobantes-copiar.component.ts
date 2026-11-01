import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { SessionService } from '../../../../core/auth/session.service';
import { TipoComprobante, TiposComprobantesApi } from '../../tipos-comprobantes/tipos-comprobantes.api';
import { ComprobanteCompleto, FiltrosComprobantes, ResumenComprobante } from '../consulta/comprobantes-consulta.models';
import { ComprobantesConsultaService } from '../consulta/comprobantes-consulta.api';
import { ComprobantesCopiarService } from './comprobantes-copiar.api';
import { ComprobanteCopiarRequest, ComprobanteCopiadoResponse } from './comprobantes-copiar.models';
/** Proceso independiente: ubica un comprobante y lo copia sin editar movimientos. */
@Component({
  selector: 'app-comprobantes-copiar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comprobantes-copiar.component.html',
  styleUrls: ['./comprobantes-copiar.component.scss']
})
export class ComprobantesCopiarComponent implements OnInit {
  private readonly session = inject(SessionService);
  private readonly consulta = inject(ComprobantesConsultaService);
  private readonly tiposApi = inject(TiposComprobantesApi);
  private readonly copiarApi = inject(ComprobantesCopiarService);
  readonly agencias = this.session.agenciasSig().filter(a => a.idAgencia != null);
  readonly moneda = new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 });
  filtros: FiltrosComprobantes = {};
  resultados: ResumenComprobante[] = [];
  origen: ComprobanteCompleto | null = null;
  tiposComprobantes: TipoComprobante[] = [];
  tiposDestino: TipoComprobante[] = [];
  cargandoTiposOrigen = false;
  destino = { idAgenciaDestino: null as number | null, tipoComprobanteDestino: '', fechaContableDestino: '', conceptoComprobanteDestino: '' };
  buscando = false;
  cargando = false;
  cargandoTipos = false;
  guardando = false;
  consultado = false;
  error = '';
  creado: ComprobanteCopiadoResponse | null = null;
  /** Preselecciona la agencia activa y carga tipos de origen y destino. */
  ngOnInit(): void {
    const activa = this.session.agenciaActivaSig()?.idAgencia;
    const agencia = this.agencias.find(a => a.idAgencia === activa) ?? this.agencias[0];
    if (agencia?.idAgencia != null) {
      this.filtros.idAgencia = agencia.idAgencia;
      this.destino.idAgenciaDestino = agencia.idAgencia;
      this.cargarTiposDestino();
    }
    this.cargarTiposComprobantes();
  }
  /** Carga tipos de origen por agencia y elimina códigos repetidos entre agencias. */
  cargarTiposComprobantes(): void {
    this.tiposComprobantes = [];
    this.filtros.tipoComprobante = undefined;
    this.error = '';
    const agenciasConsulta = this.filtros.idAgencia != null
      ? [this.filtros.idAgencia]
      : this.agencias.map(a => a.idAgencia).filter((id): id is number => id != null);
    if (!agenciasConsulta.length) return;
    this.cargandoTiposOrigen = true;
    const agenciaSeleccionada = this.filtros.idAgencia;
    forkJoin(agenciasConsulta.map(id => this.tiposApi.listarPorAgencia(id)))
      .pipe(finalize(() => { this.cargandoTiposOrigen = false; }))
      .subscribe({
        next: respuestas => {
          if (this.filtros.idAgencia !== agenciaSeleccionada) return;
          const tiposUnicos = new Map<string, TipoComprobante>();
          for (const tipo of respuestas.flat().filter(t => t.comprobanteActivo !== false)) {
            if (!tiposUnicos.has(tipo.tipoComprobante)) {
              tiposUnicos.set(tipo.tipoComprobante, tipo);
            }
          }
          this.tiposComprobantes = Array.from(tiposUnicos.values())
            .sort((a, b) => a.tipoComprobante.localeCompare(b.tipoComprobante));
        },
        error: () => {
          this.tiposComprobantes = [];
          this.error = 'No se pudieron consultar los tipos de comprobante de origen.';
        }
      });
  }
  /** Busca comprobantes de cualquier origen y estado usando el servicio existente. */
  buscar(): void {
    if (this.buscando || this.cargando || this.guardando || this.cargandoTiposOrigen) return;
    this.error = '';
    this.creado = null;
    if (this.filtros.fechaDesde && this.filtros.fechaHasta && this.filtros.fechaDesde > this.filtros.fechaHasta) {
      this.error = 'La fecha inicial no puede ser posterior a la final.';
      return;
    }
    this.buscando = true;
    this.consulta.buscar(this.filtros).pipe(finalize(() => this.buscando = false)).subscribe({
      next: datos => { this.resultados = datos; this.consultado = true; },
      error: err => { this.error = err.error?.message || 'No se pudieron buscar comprobantes.'; }
    });
  }
  /** Recupera cabecera y movimientos del origen sin exigir período abierto. */
  seleccionar(resumen: ResumenComprobante): void {
    if (this.cargando || this.guardando) return;
    this.error = '';
    this.creado = null;
    this.cargando = true;
    this.consulta.consultar(resumen.idAgencia, resumen.tipoComprobante, resumen.numeroComprobante)
      .pipe(finalize(() => this.cargando = false)).subscribe({
        next: datos => { this.origen = datos; this.destino.conceptoComprobanteDestino = datos.cabecera.conceptoComprobante || ''; },
        error: err => { this.error = err.error?.message || 'No se pudo consultar el comprobante de origen.'; }
      });
  }
  /** Vuelve a resultados sin perder los filtros ni ejecutar una copia. */
  volver(): void { this.origen = null; this.creado = null; this.error = ''; }
  /** Obtiene únicamente los tipos activos configurados para la agencia destino. */
  cargarTiposDestino(): void {
    this.tiposDestino = [];
    this.destino.tipoComprobanteDestino = '';
    const agencia = this.destino.idAgenciaDestino;
    if (agencia == null) return;
    this.cargandoTipos = true;
    this.tiposApi.listarPorAgencia(agencia).pipe(finalize(() => this.cargandoTipos = false)).subscribe({
      next: tipos => {
        if (this.destino.idAgenciaDestino !== agencia) return;
        this.tiposDestino = tipos.filter(t => t.comprobanteActivo !== false);
      },
      error: () => { this.error = 'No se pudieron cargar los tipos de comprobante de destino.'; }
    });
  }
  /** Solicita una copia inmutable del origen; el backend valida período, cuentas y cuadre. */
  copiar(): void {
    if (!this.origen || this.guardando || this.creado) return;
    this.error = '';
    const cab = this.origen.cabecera;
    if (this.destino.idAgenciaDestino == null || !this.destino.tipoComprobanteDestino ||
        !this.destino.fechaContableDestino || !this.destino.conceptoComprobanteDestino.trim()) {
      this.error = 'Complete agencia, tipo, fecha contable y concepto del destino.';
      return;
    }
    const solicitud: ComprobanteCopiarRequest = {
      idAgenciaOrigen: cab.idAgencia,
      tipoComprobanteOrigen: cab.tipoComprobante,
      numeroComprobanteOrigen: cab.numeroComprobante,
      idAgenciaDestino: this.destino.idAgenciaDestino,
      tipoComprobanteDestino: this.destino.tipoComprobanteDestino,
      fechaContableDestino: this.destino.fechaContableDestino,
      conceptoComprobanteDestino: this.destino.conceptoComprobanteDestino.trim()
    };
    this.guardando = true;
    this.copiarApi.copiar(solicitud).pipe(finalize(() => this.guardando = false)).subscribe({
      next: respuesta => { this.creado = respuesta; },
      error: err => { this.error = err.error?.message || 'No se pudo copiar el comprobante. Verifique el período y las cuentas del destino.'; }
    });
  }
  /** Suma los importes originales exclusivamente para visualización. */
  total(campo: 'valorDebito' | 'valorCredito'): number {
    return (this.origen?.movimientos || []).reduce((s, m) => s + Number(m[campo] || 0), 0);
  }
}
