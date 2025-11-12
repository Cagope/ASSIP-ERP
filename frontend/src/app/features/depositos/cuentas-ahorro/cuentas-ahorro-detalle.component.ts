import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * 🧾 Detalle de Cuenta de Ahorro
 * ------------------------------------------------------------
 * Muestra la información detallada de la cuenta seleccionada:
 * titular, datos básicos, estado, saldos, fotos y firmas.
 */
@Component({
  selector: 'app-cuentas-ahorro-detalle',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cuentas-ahorro-detalle.component.html',
  styleUrls: ['./cuentas-ahorro-detalle.component.scss']
})
export class CuentasAhorroDetalleComponent {
  @Input() cuenta: any;
  @Output() cerrar = new EventEmitter<void>();

  /** 🔙 Cierra la vista de detalle */
  onCerrar(): void {
    this.cerrar.emit();
  }

  /**
   * 🖼️ Retorna la ruta completa del archivo, según el tipo.
   * Si el campo está vacío, usa la imagen de muestra por defecto.
   */
  getImagen(nombreArchivo: string | null, tipo: 'foto' | 'firma'): string {
    // 🟡 Si el campo está vacío, usamos la imagen de muestra
    if (!nombreArchivo || nombreArchivo.trim() === '') {
      return tipo === 'foto'
        ? '/assets/fotos/foto_muestra.jpg'
        : '/assets/firmas/firma1_muestra.jpg';
    }

    // 🔠 Corrige los nombres: fuerza FI/FT en mayúscula, extensión en minúscula
    const nombreNormalizado = nombreArchivo
      .trim()
      .replace(/^fi/i, 'FI')   // asegura prefijo FI
      .replace(/^ft/i, 'FT')   // asegura prefijo FT
      .replace(/\.jpeg$/i, '.jpg'); // extensión a minúscula

    // 📂 Selecciona la carpeta según tipo
    const carpeta = tipo === 'foto' ? 'fotos' : 'firmas';

    // ✅ Devuelve la ruta que Angular puede resolver
    return `/assets/${carpeta}/${nombreNormalizado}`;
  }


  /** 🧩 Si el archivo no se encuentra, mostrar la imagen de muestra */
  onImageError(event: Event, tipo: 'foto' | 'firma'): void {
    const img = event.target as HTMLImageElement;
    img.src =
      tipo === 'foto'
        ? '/assets/fotos/foto_muestra.jpg'
        : '/assets/firmas/firma1_muestra.jpg';
  }

  /** 🌄 Control del modal de imagen ampliada */
  imagenAmpliada: string | null = null;

  /** Abre la imagen en grande */
  abrirImagen(src: string): void {
    this.imagenAmpliada = src;
  }

  /** Cierra la imagen ampliada */
  cerrarImagen(): void {
    this.imagenAmpliada = null;
  }

}
