import { Injectable } from '@angular/core';
import { CierreDiarioInforme } from './cierre-diario.models';

/** Genera un documento contable independiente de la pantalla de cierre diario. */
@Injectable({ providedIn: 'root' })
export class CierreDiarioPrintService {
  private readonly moneda = new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
  private readonly entero = new Intl.NumberFormat('es-CO', {
    maximumFractionDigits: 0
  });

  /** Abre el informe en una ventana de impresión sin modificar sus datos. */
  imprimir(informe: CierreDiarioInforme): boolean {
    if (!informe || !informe.fecha || !informe.idAgencia) {
      throw new Error('No existe un informe diario válido para imprimir.');
    }
    const ventana = window.open('', '_blank', 'width=1400,height=900');
    if (!ventana) {
      throw new Error('El navegador bloqueó la ventana de impresión.');
    }
    ventana.document.open();
    ventana.document.write(this.construirDocumento(informe));
    ventana.document.close();
    ventana.focus();
    ventana.addEventListener('load', () => ventana.print(), { once: true });
    return true;
  }

  /** Construye el informe completo, con resumen y detalle de auxiliares. */
  private construirDocumento(i: CierreDiarioInforme): string {
    const fecha = this.fecha(i.fecha);
    const generado = this.fechaHora(i.fechaGeneracion);
    const cuentas = [...(i.resumenCuentas ?? [])].sort((a, b) =>
      a.codigoCuenta.localeCompare(b.codigoCuenta, 'es', { numeric: true })
    );
    const movimientos = [...(i.movimientos ?? [])].sort((a, b) =>
      a.codigoCuenta.localeCompare(b.codigoCuenta, 'es', { numeric: true }) ||
      a.tipoComprobante.localeCompare(b.tipoComprobante, 'es') ||
      a.numeroComprobante.localeCompare(b.numeroComprobante, 'es', { numeric: true }) ||
      a.idAuxiliarContable - b.idAuxiliarContable
    );
    const filasCuentas = cuentas.map(c => `
      <tr>
        <td>${this.esc(c.codigoCuenta)}</td>
        <td>${this.esc(c.nombreCuenta)}</td>
        <td class="numero">${this.entero.format(c.cantidadMovimientos)}</td>
        <td class="numero">${this.valor(c.totalDebitos)}</td>
        <td class="numero">${this.valor(c.totalCreditos)}</td>
      </tr>`).join('');
    const filasMovimientos = movimientos.map(m => `
      <tr>
        <td class="nowrap">${this.esc(m.tipoComprobante)} ${this.esc(m.numeroComprobante)}</td>
        <td class="nowrap">${this.esc(m.codigoCuenta)}</td>
        <td>${this.esc(m.nombreCuenta)}</td>
        <td>${this.esc(m.documentoTercero)}</td>
        <td>${this.esc(m.nombreTercero)}</td>
        <td>${this.esc(m.detalleMovimiento)}</td>
        <td class="numero">${this.valor(m.valorDebito)}</td>
        <td class="numero">${this.valor(m.valorCredito)}</td>
      </tr>`).join('');
    const pie = `
      <tr class="total"><td colspan="3">TOTALES GENERALES</td>
        <td class="numero">${this.entero.format(i.cantidadMovimientos)}</td>
        <td class="numero">${this.valor(i.totalDebitos)}</td>
        <td class="numero">${this.valor(i.totalCreditos)}</td>
      </tr>`;
    return `<!doctype html>
<html lang="es"><head><meta charset="utf-8"><title>Informe cierre diario - ${this.esc(i.fecha)}</title>
<style>
@page { size: letter landscape; margin: 13mm 11mm 15mm; }
* { box-sizing: border-box; }
body { font: 9px Arial, sans-serif; color: #222; margin: 0; }
header { border-bottom: 2px solid #333; padding-bottom: 7px; margin-bottom: 10px; }
h1 { font-size: 16px; margin: 0 0 5px; }
h2 { font-size: 12px; margin: 17px 0 6px; }
.meta { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 18px; }
.meta span { font-weight: bold; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
thead { display: table-header-group; }
tr { break-inside: avoid; page-break-inside: avoid; }
th { background: #e8e8e8; border: 1px solid #aaa; padding: 5px 4px; text-align: left; }
td { border-bottom: 1px solid #ddd; padding: 4px; vertical-align: top; overflow-wrap: anywhere; }
.numero { text-align: right; white-space: nowrap; font-variant-numeric: tabular-nums; }
.nowrap { white-space: nowrap; }
.total { background: #eee; font-weight: bold; border-top: 2px solid #444; }
.resumen th:nth-child(1) { width: 13%; }
.resumen th:nth-child(2) { width: 43%; }
.resumen th:nth-child(3) { width: 10%; }
.resumen th:nth-child(4), .resumen th:nth-child(5) { width: 17%; }
.detalle th:nth-child(1) { width: 10%; }
.detalle th:nth-child(2) { width: 9%; }
.detalle th:nth-child(3) { width: 15%; }
.detalle th:nth-child(4) { width: 10%; }
.detalle th:nth-child(5) { width: 15%; }
.detalle th:nth-child(6) { width: 21%; }
.detalle th:nth-child(7), .detalle th:nth-child(8) { width: 10%; }
.control { margin-top: 12px; border-top: 1px solid #888; padding-top: 7px; }
@media screen { body { max-width: 1250px; margin: 24px auto; padding: 0 20px; } }
</style></head><body>
<header><h1>INFORME CONTABLE DEL DÍA</h1>
<div class="meta">
<div><span>Agencia:</span> ${this.esc(i.codigoAgencia)} - ${this.esc(i.nombreAgencia)}</div>
<div><span>Fecha contable:</span> ${this.esc(fecha)}</div>
<div><span>Estado del día:</span> ${i.estadoDia === 'C' ? 'CERRADO' : 'ABIERTO'}</div>
<div><span>Estado mensual:</span> ${this.esc(i.estadoPeriodoMensual)}</div>
<div><span>Generado:</span> ${this.esc(generado)}</div>
<div><span>Comprobantes:</span> ${this.entero.format(i.cantidadComprobantes)} &nbsp; <span>Movimientos:</span> ${this.entero.format(i.cantidadMovimientos)}</div>
</div></header>
<h2>1. RESUMEN POR CUENTA CONTABLE</h2>
<table class="resumen"><thead><tr><th>Código</th><th>Cuenta contable</th><th class="numero">Movimientos</th><th class="numero">Débitos</th><th class="numero">Créditos</th></tr></thead>
<tbody>${filasCuentas || '<tr><td colspan="5">Sin movimientos contables.</td></tr>'}
<tr class="total"><td colspan="2">TOTALES GENERALES</td><td class="numero">${this.entero.format(i.cantidadMovimientos)}</td><td class="numero">${this.valor(i.totalDebitos)}</td><td class="numero">${this.valor(i.totalCreditos)}</td></tr></tbody></table>
<h2>2. DETALLE DE MOVIMIENTOS CONTABLES</h2>
<table class="detalle"><thead><tr><th>Comprobante</th><th>Cuenta</th><th>Nombre de cuenta</th><th>Documento</th><th>Tercero</th><th>Detalle del movimiento</th><th class="numero">Débito</th><th class="numero">Crédito</th></tr></thead>
<tbody>${filasMovimientos || '<tr><td colspan="8">Sin movimientos contables.</td></tr>'}
<tr class="total"><td colspan="6">TOTALES GENERALES</td><td class="numero">${this.valor(i.totalDebitos)}</td><td class="numero">${this.valor(i.totalCreditos)}</td></tr></tbody></table>
<div class="control"><strong>CONTROL CONTABLE:</strong> Diferencia débitos - créditos: ${this.valor(i.diferencia)}.
Este documento es una consulta de auxiliares contables para la fecha indicada; no registra ni modifica cierres.</div>
</body></html>`;
  }

  /** Evita interpretar valores contables como etiquetas HTML. */
  private esc(valor: unknown): string {
    return String(valor ?? '').replace(/[&<>"']/g, caracter => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[caracter] ?? caracter));
  }

  private valor(valor: number): string {
    return this.moneda.format(Number(valor ?? 0));
  }

  private fecha(valor: string): string {
    const [ano, mes, dia] = (valor ?? '').slice(0, 10).split('-');
    return ano && mes && dia ? `${dia}/${mes}/${ano}` : valor;
  }

  private fechaHora(valor: string): string {
    const fecha = new Date(valor);
    return Number.isNaN(fecha.getTime()) ? (valor ?? '') :
      fecha.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'medium' });
  }
}
