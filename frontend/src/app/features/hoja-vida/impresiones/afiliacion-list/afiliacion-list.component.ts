import {
  Component,
  OnDestroy,
  OnInit,
  Renderer2,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import {
  DatosPersonalesApi,
  DatosPersonales
} from '../../datos-personales/datos-personales.api';

import {
  FORMATOS_IMPRESION
} from '../impresiones-menu.config';

@Component({
  selector: 'app-afiliacion-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './afiliacion-list.component.html',
  styleUrls: ['./afiliacion-list.component.scss']
})
export class AfiliacionListComponent
  implements OnInit, OnDestroy {

  private readonly api =
    inject(DatosPersonalesApi);

  private readonly router =
    inject(Router);

  private readonly renderer =
    inject(Renderer2);

  personas: DatosPersonales[] = [];
  filtradas: DatosPersonales[] = [];

  cargando = false;
  error = '';

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  formatos = FORMATOS_IMPRESION;

  pagina = 1;
  tamanoPagina = 10;

  private overlayMenu: HTMLElement | null = null;

  private removerEscuchaDocumento:
    (() => void) | null = null;

  ngOnInit(): void {
    this.cargar();

    this.removerEscuchaDocumento =
      this.renderer.listen(
        'document',
        'click',
        () => this.cerrarMenu()
      );
  }

  ngOnDestroy(): void {
    this.cerrarMenu();

    if (this.removerEscuchaDocumento) {
      this.removerEscuchaDocumento();
      this.removerEscuchaDocumento = null;
    }
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.api.listar()
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: lista => {
          this.personas = (lista ?? [])
            .sort((a, b) => {
              const fechaA =
                a.fechaActualizacion
                  ? Date.parse(a.fechaActualizacion)
                  : 0;

              const fechaB =
                b.fechaActualizacion
                  ? Date.parse(b.fechaActualizacion)
                  : 0;

              return fechaB - fechaA;
            });

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando afiliaciones:',
            err
          );

          this.error =
            'No fue posible cargar los asociados.';

          this.personas = [];
          this.filtradas = [];
          this.pagina = 1;
        }
      });
  }

  refrescar(): void {
    this.cargar();
  }

  buscar(): void {
    const documento =
      this.normalizarTexto(
        this.filtros.documento
      );

    const nombres =
      this.normalizarTexto(
        this.filtros.nombres
      );

    const primerApellido =
      this.normalizarTexto(
        this.filtros.primerApellido
      );

    const segundoApellido =
      this.normalizarTexto(
        this.filtros.segundoApellido
      );

    this.filtradas = this.personas.filter(
      persona => {
        const documentoPersona =
          this.normalizarTexto(
            persona.documento
          );

        const nombresPersona =
          this.normalizarTexto(
            persona.nombres
          );

        const primerApellidoPersona =
          this.normalizarTexto(
            persona.primerApellido
          );

        const segundoApellidoPersona =
          this.normalizarTexto(
            persona.segundoApellido
          );

        return (
          (
            !documento
            || documentoPersona.includes(
              documento
            )
          )
          &&
          (
            !nombres
            || nombresPersona.includes(
              nombres
            )
          )
          &&
          (
            !primerApellido
            || primerApellidoPersona.includes(
              primerApellido
            )
          )
          &&
          (
            !segundoApellido
            || segundoApellidoPersona.includes(
              segundoApellido
            )
          )
        );
      }
    );

    this.pagina = 1;
    this.error = '';
    this.cerrarMenu();
  }

  limpiar(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.filtradas = [...this.personas];
    this.pagina = 1;
    this.error = '';
    this.cerrarMenu();
  }

  get paginadas(): DatosPersonales[] {
    const inicio =
      (this.pagina - 1) * this.tamanoPagina;

    return this.filtradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }

  totalPaginas(): number {
    return Math.max(
      1,
      Math.ceil(
        this.filtradas.length
        / this.tamanoPagina
      )
    );
  }

  cambiarPagina(pagina: number): void {
    if (
      pagina < 1
      || pagina > this.totalPaginas()
    ) {
      return;
    }

    this.pagina = pagina;
    this.cerrarMenu();
  }

  abrirMenu(
    event: MouseEvent,
    persona: DatosPersonales
  ): void {

    event.stopPropagation();
    this.cerrarMenu();

    const menu =
      this.renderer.createElement('div');

    this.renderer.addClass(
      menu,
      'global-dropdown'
    );

    this.formatos.forEach(formato => {

      const item =
        this.renderer.createElement('button');

      this.renderer.setAttribute(
        item,
        'type',
        'button'
      );

      this.renderer.addClass(
        item,
        'menu-item'
      );

      const texto =
        this.renderer.createText(
          `${formato.icon} ${formato.label}`
        );

      this.renderer.appendChild(
        item,
        texto
      );

      this.renderer.listen(
        item,
        'click',
        (itemEvent: MouseEvent) => {

          itemEvent.stopPropagation();

          this.abrirFormato(
            formato,
            persona
          );

          this.cerrarMenu();
        }
      );

      this.renderer.appendChild(
        menu,
        item
      );
    });

    const anchoEstimado = 240;
    const margen = 12;

    const izquierda = Math.max(
      margen,
      Math.min(
        event.clientX - 120,
        window.innerWidth
          - anchoEstimado
          - margen
      )
    );

    const superior = Math.max(
      margen,
      event.clientY + 8
    );

    this.renderer.setStyle(
      menu,
      'position',
      'fixed'
    );

    this.renderer.setStyle(
      menu,
      'left',
      `${izquierda}px`
    );

    this.renderer.setStyle(
      menu,
      'top',
      `${superior}px`
    );

    this.renderer.setStyle(
      menu,
      'z-index',
      '999999'
    );

    this.renderer.appendChild(
      document.body,
      menu
    );

    this.overlayMenu = menu;
  }

  cerrarMenu(): void {
    if (!this.overlayMenu) {
      return;
    }

    const padre =
      this.overlayMenu.parentNode;

    if (padre) {
      this.renderer.removeChild(
        padre,
        this.overlayMenu
      );
    }

    this.overlayMenu = null;
  }

  abrirFormato(
    formato: { id: string },
    persona: DatosPersonales
  ): void {

    const idDatosPersonal =
      persona.idDatosPersonal;

    if (!idDatosPersonal) {
      this.error =
        'No se encontró el identificador del asociado.';

      return;
    }

    this.error = '';

    const opcionesNavegacion = {
      state: {
        persona
      }
    };

    switch (formato.id) {

      case 'afiliacion':
        this.router.navigate(
          [
            '/hoja-vida/impresiones/afiliacion-formulario',
            idDatosPersonal
          ],
          opcionesNavegacion
        );
        break;

      case 'tratamiento':
        this.router.navigate(
          [
            '/hoja-vida/impresiones/tratamiento-datos',
            idDatosPersonal
          ],
          opcionesNavegacion
        );
        break;

      case 'origen-fondos':
        this.router.navigate(
          [
            '/hoja-vida/impresiones/origen-fondos',
            idDatosPersonal
          ],
          opcionesNavegacion
        );
        break;

      case 'carta-gmf':
        this.router.navigate(
          [
            '/hoja-vida/impresiones/exoneracion-gmf',
            idDatosPersonal
          ],
          opcionesNavegacion
        );
        break;

      case 'actualizacion-datos':
        this.router.navigate(
          [
            '/hoja-vida/impresiones/actualizacion-datos',
            idDatosPersonal
          ],
          opcionesNavegacion
        );
        break;

      default:
        this.error =
          'El formato seleccionado no está reconocido.';
        break;
    }
  }

  private normalizarTexto(
    valor:
      | string
      | number
      | null
      | undefined
  ): string {

    return String(valor ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }
}
