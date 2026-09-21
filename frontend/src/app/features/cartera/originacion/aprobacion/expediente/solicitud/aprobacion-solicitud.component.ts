import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-aprobacion-solicitud',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './aprobacion-solicitud.component.html',
  styleUrl: './aprobacion-solicitud.component.scss'
})
export class AprobacionSolicitudComponent {

  @Input() foto: Record<string, unknown> | Record<string, unknown>[] | null = null;
  @Input() deudores: Record<string, unknown>[] | null = null;
  get items(): Record<string, unknown>[] { return Array.isArray(this.foto) ? this.foto : []; }
  get actual(): Record<string, unknown> { return (!Array.isArray(this.foto) && this.foto) || {}; }
  valor(o: Record<string, unknown>, k: string): string { const v=o[k];return v===null || v===undefined || v==='' ? 'No registrado' : String(v); }
  texto(o: Record<string, unknown>, k: string): string {return this.valor(o,k).replaceAll('_',' ');}
  numero(o: Record<string, unknown>, k: string): number | null {const v=o[k];return v===null || v===undefined || v==='' || !Number.isFinite(Number(v)) ? null : Number(v);}
  dinero(o: Record<string, unknown>, k: string): string {const n=this.numero(o,k);return n===null?'No registrado':new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0}).format(n);}
  porcentaje(o: Record<string, unknown>, k: string, dec=2): string {const n=this.numero(o,k);return n===null?'No registrado':new Intl.NumberFormat('es-CO',{maximumFractionDigits:dec}).format(n)+' %';}
  fecha(o: Record<string, unknown>, k: string): string {const v=o[k];if(typeof v!=='string'||!v)return 'No registrada';const d=v.slice(0,10).split('-');return d.length===3?`${d[2]}/${d[1]}/${d[0]}`:'No registrada';}
  estado(o: Record<string, unknown>, k: string): string {return o[k]===true?'Cumple':o[k]===false?'No cumple':'No evaluado';}
  nombre(id: unknown): string {const p=(this.deudores??[]).find(x=>x['id_solicitud_deudor']===id);return p ? this.valor(p,'nombre_completo'):'Nombre no disponible en fotografía';}
  rol(o: Record<string, unknown>): string {return this.texto(o,'tipo_deudor')==='PRINCIPAL'?'Titular':this.texto(o,'tipo_deudor')==='CODEUDOR'?'Codeudor':this.texto(o,'tipo_deudor');}

}
