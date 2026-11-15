
import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

/* =========================================================
   ASSIP ERP - COMUNICACIÓN DE NAVEGACIÓN
   COOPVALLE
   ========================================================= */

@Injectable({
  providedIn: 'root'
})
export class MenuNavigationService {

  /**
   * Solicitudes para abrir un módulo del menú lateral.
   * Se identifica mediante el índice de MENU_REGISTRY.
   */
  private readonly abrirModuloSubject = new Subject<number>();

  /**
   * Evento que escucha el menú lateral.
   */
  readonly abrirModulo$ =
    this.abrirModuloSubject.asObservable();

  /**
   * Solicita abrir un módulo sin navegar
   * automáticamente a una opción.
   */
  abrirModulo(index: number): void {

    if (!Number.isInteger(index) || index < 0) {
      return;
    }

    this.abrirModuloSubject.next(index);
  }
}
