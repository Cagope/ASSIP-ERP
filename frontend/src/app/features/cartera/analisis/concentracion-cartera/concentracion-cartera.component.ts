import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ConcentracionCarteraService } from './concentracion-cartera.service';
import { ConcentracionCredito, ConcentracionDeudor, ConcentracionFiltros, ConcentracionRanking, ConcentracionResumen, ConcentracionSegmento } from './concentracion-cartera.models';

@Component({selector:'app-concentracion-cartera',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./concentracion-cartera.component.html',styleUrl:'./concentracion-cartera.component.scss'})
export class ConcentracionCarteraComponent implements OnInit {
  cargando=false; error=''; cortes:string[]=[];
  filtros:ConcentracionFiltros={fechaCorte:'',idAgencia:null,idLineaCredito:null,codigoGarantia:'',codigoClasificacion:'',edadContable:'',codigoDestino:''};
  resumen?:ConcentracionResumen; exposicion:ConcentracionRanking[]=[]; deterioro:ConcentracionRanking[]=[];
  lineas:ConcentracionSegmento[]=[]; agencias:ConcentracionSegmento[]=[]; garantias:ConcentracionSegmento[]=[]; clasificaciones:ConcentracionSegmento[]=[]; edades:ConcentracionSegmento[]=[]; destinos:ConcentracionSegmento[]=[];
  catalogoLineas:ConcentracionSegmento[]=[]; catalogoAgencias:ConcentracionSegmento[]=[]; catalogoGarantias:ConcentracionSegmento[]=[]; catalogoClasificaciones:ConcentracionSegmento[]=[]; catalogoEdades:ConcentracionSegmento[]=[]; catalogoDestinos:ConcentracionSegmento[]=[];
  deudores:ConcentracionDeudor[]=[]; creditos:ConcentracionCredito[]=[]; deudorSeleccionado?:ConcentracionDeudor; pagina=1; porPagina=25;
  constructor(private api:ConcentracionCarteraService){}
  ngOnInit(){this.cargando=true;forkJoin({control:this.api.control(),cortes:this.api.cortes()}).subscribe({next:r=>{this.cortes=r.cortes;this.filtros.fechaCorte=r.control.corteSugerido;this.cargarCatalogosYDatos();},error:e=>this.fallar(e)});}
  cargarCatalogosYDatos(){const base={...this.filtros,idAgencia:null,idLineaCredito:null,codigoGarantia:'',codigoClasificacion:'',edadContable:'',codigoDestino:''};this.cargando=true;forkJoin({a:this.api.segmentos('agencias',base),l:this.api.segmentos('lineas',base),g:this.api.segmentos('garantias',base),c:this.api.segmentos('clasificaciones',base),e:this.api.segmentos('edades-contables',base),d:this.api.segmentos('destinos',base)}).subscribe({next:r=>{this.catalogoAgencias=r.a;this.catalogoLineas=r.l;this.catalogoGarantias=r.g;this.catalogoClasificaciones=r.c;this.catalogoEdades=r.e;this.catalogoDestinos=r.d;this.cargar();},error:e=>this.fallar(e)});}
  cargar(){this.cargando=true;this.error='';this.deudorSeleccionado=undefined;this.creditos=[];forkJoin({resumen:this.api.resumen(this.filtros),exp:this.api.rankingExposicion(this.filtros,20),det:this.api.rankingDeterioro(this.filtros,20),lin:this.api.segmentos('lineas',this.filtros),age:this.api.segmentos('agencias',this.filtros),gar:this.api.segmentos('garantias',this.filtros),cla:this.api.segmentos('clasificaciones',this.filtros),eda:this.api.segmentos('edades-contables',this.filtros),des:this.api.segmentos('destinos',this.filtros),deu:this.api.detalleDeudores(this.filtros)}).subscribe({next:r=>{this.resumen=r.resumen;this.exposicion=r.exp;this.deterioro=r.det;this.lineas=r.lin;this.agencias=r.age;this.garantias=r.gar;this.clasificaciones=r.cla;this.edades=r.eda;this.destinos=r.des;this.deudores=r.deu;this.pagina=1;this.cargando=false;},error:e=>this.fallar(e)});}
  cambiarCorte(){this.filtros.idAgencia=null;this.filtros.idLineaCredito=null;this.filtros.codigoGarantia='';this.filtros.codigoClasificacion='';this.filtros.edadContable='';this.filtros.codigoDestino='';this.cargarCatalogosYDatos();}
  limpiar(){const fecha=this.filtros.fechaCorte;this.filtros={fechaCorte:fecha,idAgencia:null,idLineaCredito:null,codigoGarantia:'',codigoClasificacion:'',edadContable:'',codigoDestino:''};this.cargar();}
  verCreditos(d:ConcentracionDeudor){this.deudorSeleccionado=d;this.api.detalleCreditos(this.filtros,d.idDatosPersonal).subscribe({next:r=>this.creditos=r,error:e=>this.fallar(e)});}
  get deudoresPagina(){const i=(this.pagina-1)*this.porPagina;return this.deudores.slice(i,i+this.porPagina);} get totalPaginas(){return Math.max(1,Math.ceil(this.deudores.length/this.porPagina));}
  moneda(v:number|null|undefined){return new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0}).format(v??0);} pct(v:number|null|undefined){return `${Number(v??0).toLocaleString('es-CO',{minimumFractionDigits:2,maximumFractionDigits:2})} %`;}
  ancho(v:number,max:number){return max?Math.max(1,(v/max)*100):0;} abs(v:number){return Math.abs(v??0);}
  private fallar(e:any){this.cargando=false;this.error=e?.error?.message||e?.message||'No fue posible cargar el análisis de concentración.';}
}
