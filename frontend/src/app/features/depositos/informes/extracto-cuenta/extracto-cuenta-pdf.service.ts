import { Injectable } from '@angular/core';

import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

@Injectable({
  providedIn: 'root'
})
export class ExtractoCuentaPdfService {

  async generarPdf(
    elementId: string,
    nombreArchivo: string
  ): Promise<void> {

    const element =
      document.getElementById(elementId);

    if (!element) {
      alert('No se encontró el contenido para generar PDF.');
      return;
    }

    const canvas =
      await html2canvas(element, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#ffffff'
      });

    const imgData =
      canvas.toDataURL('image/png');

    const pdf =
      new jsPDF({
        orientation: 'portrait',
        unit: 'mm',
        format: 'letter'
      });

    const pdfWidth =
      pdf.internal.pageSize.getWidth();

    const pdfHeight =
      pdf.internal.pageSize.getHeight();

    const imgWidth =
      pdfWidth;

    const imgHeight =
      (canvas.height * imgWidth) / canvas.width;

    let heightLeft =
      imgHeight;

    let position =
      0;

    pdf.addImage(
      imgData,
      'PNG',
      0,
      position,
      imgWidth,
      imgHeight
    );

    heightLeft -= pdfHeight;

    while (heightLeft > 0) {

      position =
        heightLeft - imgHeight;

      pdf.addPage();

      pdf.addImage(
        imgData,
        'PNG',
        0,
        position,
        imgWidth,
        imgHeight
      );

      heightLeft -= pdfHeight;
    }

    pdf.save(nombreArchivo);
  }

}
