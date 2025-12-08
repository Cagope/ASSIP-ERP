import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class HabilidadAsociadoPrintService {

  imprimir(resultados: any[], filtros: any) {

    if (!resultados || resultados.length === 0) {
      alert('No hay información para imprimir.');
      return;
    }

    const html = this.buildHTML(resultados, filtros);

    const win = window.open('', '_blank', 'width=1200,height=800');
    if (!win) {
      alert('Bloqueador de ventanas emergentes activo.');
      return;
    }

    win.document.open();
    win.document.write(html);
    win.document.close();
    win.onload = () => win.print();
  }

  // ======================================================
  // 🧾 CONSTRUCCIÓN HTML COMPLETO
  // ======================================================
  private buildHTML(resultados: any[], filtros: any): string {

    const habiles = resultados.filter(r => (r.resultado ?? '').toUpperCase() === 'HÁBIL');
    const inHabiles = resultados.filter(r => (r.resultado ?? '').toUpperCase() !== 'HÁBIL');

    return `
      <html>
      <head>
        <meta charset="utf-8">
        <title>Habilidad del Asociado</title>

        <style>

          @page { size: letter portrait; margin: 10mm 12mm; }

          body {
            font-family: Arial, sans-serif;
            font-size: 11px;
            margin: 0;
            color: #222;
          }

          .enc {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
          }

          .enc img {
            height: 40px;
            margin-right: 10px;
          }

          .tit {
            font-size: 16px;
            font-weight: bold;
            margin: 0;
          }

          .sub {
            font-size: 11px;
            margin-top: 2px;
          }

          h2 {
            margin-top: 25px;
            margin-bottom: 5px;
            font-size: 14px;
            border-bottom: 1px solid #ccc;
            padding-bottom: 3px;
          }

          h3 {
            margin: 12px 0 4px 0;
            font-size: 13px;
            color: #444;
          }

          table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 15px;
          }

          th {
            background: #f4f4f4;
            border-bottom: 1px solid #555;
            padding: 4px 4px;
            text-align: left;
          }

          td {
            padding: 3px 4px;
            border-bottom: 1px solid #eee;
          }

          .right { text-align: right; }
          .tot { font-weight: bold; background: #fafafa; }

        </style>

      </head>
      <body>

        ${this.buildHeader(filtros)}

        ${this.seccion('HÁBILES', habiles)}
        ${this.seccion('INHÁBILES', inHabiles)}

      </body>
      </html>
    `;
  }

  // ======================================================
  // 🟦 ENCABEZADO
  // ======================================================
  private buildHeader(filtros: any): string {

    const fechaInicio = filtros.fechaInicio ?? '';
    const fechaFin    = filtros.fechaFin ?? '';

    const agencia = filtros.agenciaId
      ? `${filtros.agenciaId} — ${filtros.nombreAgencia ?? ''}`
      : 'Todas';

    return `
      <div class="enc">
        <img src="${window.location.origin}/assets/LOGO_EMPRESA.png" />
        <div>
          <div class="tit">Proceso — Habilidad del Asociado</div>
          <div class="sub">Agencia: ${agencia}</div>
          <div class="sub">Fechas: ${fechaInicio} → ${fechaFin}</div>
        </div>
      </div>
    `;
  }

  // ======================================================
  // 📌 SECCIÓN HÁBILES / INHÁBILES
  // ======================================================
  private seccion(titulo: string, lista: any[]): string {

    if (!lista || lista.length === 0) {
      return `<h2>${titulo}</h2><p>No hay registros.</p>`;
    }

    let html = `<h2>${titulo}</h2>`;

    const zonas = Array.from(new Set(lista.map(r => r.zona ?? 'SIN_ZONA')));

    let totalGeneral = 0;

    zonas.forEach(zona => {

      const itemsZona = lista.filter(r => (r.zona ?? 'SIN_ZONA') === zona);
      if (itemsZona.length === 0) return;

      html += `<h3>Zona: <b>${zona}</b></h3>`;

      const subzonas = Array.from(new Set(itemsZona.map(r => r.subzona ?? 'SIN_SUBZONA')));

      let totalZona = 0;

      subzonas.forEach(subzona => {

        const itemsSub = itemsZona.filter(r => (r.subzona ?? 'SIN_SUBZONA') === subzona);
        if (itemsSub.length === 0) return;

        html += `<div><b>Subzona: ${subzona}</b></div>`;

        html += `
          <table>
            <thead>
              <tr>
                <th>Documento</th>
                <th>Nombre</th>
                <th>Edad</th>
                <th>Tipo Persona</th>
                <th class="right">Saldo Actual</th>
                <th class="right">Aportes</th>
                <th>Resultado</th>
              </tr>
            </thead>
            <tbody>
        `;

        itemsSub.forEach(r => {
          html += `
            <tr>
              <td>${r.documento}</td>
              <td>${r.nombre}</td>
              <td>${r.edad}</td>
              <td>${r.tipoPersona === '1' ? 'Natural' : 'Jurídica'}</td>
              <td class="right">${Number(r.saldoHoy ?? 0).toLocaleString('es-CO')}</td>
              <td class="right">${Number(r.totalAportes ?? 0).toLocaleString('es-CO')}</td>
              <td>${r.resultado}</td>
            </tr>
          `;
        });

        html += `
            </tbody>
          </table>

          <div class="tot">TOTAL SUBZONA ${subzona}: ${itemsSub.length}</div>
          <br/>
        `;

        totalZona += itemsSub.length;
      });

      html += `<div class="tot">TOTAL ZONA ${zona}: ${totalZona}</div><br/>`;

      totalGeneral += totalZona;
    });

    html += `<h3 class="tot">TOTAL GENERAL ${titulo}: ${totalGeneral}</h3>`;

    return html;
  }
}
