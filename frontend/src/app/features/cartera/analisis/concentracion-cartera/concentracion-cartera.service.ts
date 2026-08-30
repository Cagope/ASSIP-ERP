import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ConcentracionControl, ConcentracionCredito, ConcentracionDeudor, ConcentracionFiltros, ConcentracionRanking, ConcentracionResumen, ConcentracionSegmento } from './concentracion-cartera.models';

@Injectable({ providedIn: 'root' })
export class ConcentracionCarteraService {
  private readonly baseUrl = `${environment.apiUrl}/cartera/analisis/concentracion-cartera`;
  constructor(private http: HttpClient) {}
  control():Observable<ConcentracionControl>{ return this.http.get<ConcentracionControl>(`${this.baseUrl}/control`); }
  cortes():Observable<string[]>{ return this.http.get<string[]>(`${this.baseUrl}/cortes`); }
  resumen(f:ConcentracionFiltros){ return this.http.get<ConcentracionResumen>(`${this.baseUrl}/resumen`,{params:this.params(f)}); }
  rankingExposicion(f:ConcentracionFiltros, limite=20){ return this.http.get<ConcentracionRanking[]>(`${this.baseUrl}/deudores-exposicion`,{params:this.params(f).set('limite',limite)}); }
  rankingDeterioro(f:ConcentracionFiltros, limite=20){ return this.http.get<ConcentracionRanking[]>(`${this.baseUrl}/deudores-deterioro`,{params:this.params(f).set('limite',limite)}); }
  segmentos(tipo:'lineas'|'agencias'|'garantias'|'clasificaciones'|'edades-contables'|'destinos', f:ConcentracionFiltros){ return this.http.get<ConcentracionSegmento[]>(`${this.baseUrl}/${tipo}`,{params:this.params(f)}); }
  detalleDeudores(f:ConcentracionFiltros){ return this.http.get<ConcentracionDeudor[]>(`${this.baseUrl}/detalle-deudores`,{params:this.params(f)}); }
  detalleCreditos(f:ConcentracionFiltros,idDatosPersonal:number){ return this.http.get<ConcentracionCredito[]>(`${this.baseUrl}/detalle-creditos`,{params:this.params(f).set('idDatosPersonal',idDatosPersonal)}); }
  private params(f:ConcentracionFiltros):HttpParams{
    let p=new HttpParams();
    if(f.fechaCorte)p=p.set('fechaCorte',f.fechaCorte);
    if(f.idAgencia!=null)p=p.set('idAgencia',f.idAgencia);
    if(f.idLineaCredito!=null)p=p.set('idLineaCredito',f.idLineaCredito);
    if(f.codigoGarantia)p=p.set('codigoGarantia',f.codigoGarantia);
    if(f.codigoClasificacion)p=p.set('codigoClasificacion',f.codigoClasificacion);
    if(f.edadContable)p=p.set('edadContable',f.edadContable);
    if(f.codigoDestino)p=p.set('codigoDestino',f.codigoDestino);
    return p;
  }
}
