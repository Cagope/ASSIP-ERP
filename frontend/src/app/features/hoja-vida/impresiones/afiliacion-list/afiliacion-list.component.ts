import { Component, OnInit, inject, Renderer2 } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DatosPersonalesApi, DatosPersonales } from '../../datos-personales/datos-personales.api';
import { FORMATOS_IMPRESION } from '../impresiones-menu.config';

@Component({
  selector: 'app-afiliacion-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './afiliacion-list.component.html',
  styleUrls: ['./afiliacion-list.component.scss']
})
export class AfiliacionListComponent implements OnInit {
  private readonly api = inject(DatosPersonalesApi);
  private readonly router = inject(Router);
  private readonly renderer = inject(Renderer2);

  datos: DatosPersonales[] = [];
  filtrados: DatosPersonales[] = [];
  cargando = false;
  error = '';
  filtro = '';

  // 📜 Ahora usa el menú global de formatos
  formatos = FORMATOS_IMPRESION;

  private overlayMenu: HTMLElement | null = null;

  ngOnInit(): void {
    this.cargar();
    document.addEventListener('click', () => this.cerrarMenu());
  }

  // ===========================================================
  // 📥 Cargar registros
  // ===========================================================
  private cargar(): void {
    this.cargando = true;
    this.api.listar().subscribe({
      next: (data) => {
        this.datos = (data ?? []).sort((a, b) => {
          const fa = a.fechaActualizacion ? new Date(a.fechaActualizacion).getTime() : 0;
          const fb = b.fechaActualizacion ? new Date(b.fechaActualizacion).getTime() : 0;
          return fb - fa;
        });
        this.filtrar();
      },
      error: () => (this.error = 'Error al cargar afiliaciones'),
      complete: () => (this.cargando = false)
    });
  }

  refrescar(): void {
    this.cargar();
  }

  // ===========================================================
  // 🔍 Filtro local
  // ===========================================================
  filtrar(): void {
    const term = this.filtro.toLowerCase().trim();
    this.filtrados = !term
      ? this.datos
      : this.datos.filter(d =>
          `${d.documento} ${d.nombres ?? ''} ${d.primerApellido ?? ''} ${d.segundoApellido ?? ''}`
            .toLowerCase()
            .includes(term)
        );
  }

  // ===========================================================
  // 🖨️ Menú contextual de impresión
  // ===========================================================
  abrirMenu(event: MouseEvent, persona: DatosPersonales): void {
    event.stopPropagation();
    this.cerrarMenu(); // Cierra si hay uno abierto

    const menu = this.renderer.createElement('div');
    this.renderer.addClass(menu, 'menu-overlay');

    this.formatos.forEach(f => {
      const item = this.renderer.createElement('button');
      this.renderer.addClass(item, 'menu-item');
      item.innerHTML = `${f.icon} ${f.label}`;
      item.addEventListener('click', () => {
        this.abrirFormato(f, persona);
        this.cerrarMenu();
      });
      this.renderer.appendChild(menu, item);
    });

    // Posición en pantalla
    Object.assign(menu.style, {
      position: 'fixed',
      left: `${event.clientX - 120}px`,
      top: `${event.clientY + 8}px`,
      zIndex: '9999999'
    });

    document.body.appendChild(menu);
    this.overlayMenu = menu;
  }

  cerrarMenu(): void {
    if (this.overlayMenu) {
      this.overlayMenu.remove();
      this.overlayMenu = null;
    }
  }

  // ===========================================================
  // 🧭 Navegación a formatos de impresión
  // ===========================================================
  abrirFormato(formato: { id: string }, persona: DatosPersonales): void {
    const id = persona.idDatosPersonal;
    if (!id) {
      alert('⚠️ No se encontró el identificador.');
      return;
    }

    switch (formato.id) {
      case 'afiliacion':
        this.router.navigate(['/hoja-vida/impresiones/afiliacion-formulario', id], { state: { persona } });
        break;

      case 'tratamiento':
        this.router.navigate(['/hoja-vida/impresiones/tratamiento-datos', id], { state: { persona } });
        break;

      case 'origen-fondos':
        this.router.navigate(['/hoja-vida/impresiones/origen-fondos', id], { state: { persona } });
        break;

      case 'carta-gmf':
        this.router.navigate(['/hoja-vida/impresiones/carta-gmf', id], { state: { persona } });
        break;

      case 'actualizacion-datos':
        this.router.navigate(['/hoja-vida/impresiones/actualizacion-datos', id], { state: { persona } });
        break;

      default:
        alert('Formato no reconocido.');
    }
  }
}
