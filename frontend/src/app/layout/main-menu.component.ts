import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SessionService } from '../core/auth/session.service';
import { MENU_REGISTRY } from './menu-registry';

@Component({
  standalone: true,
  selector: 'app-main-menu',
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.scss'],
})
export class MainMenuComponent {

  menu = MENU_REGISTRY;
  openedIndex: number | null = null;

  constructor(private session: SessionService) {}

  /** Saber si la sección está abierta */
  isOpen(index: number): boolean {
    return this.openedIndex === index;
  }

  /** Abrir una y cerrar las demás */
  toggleSection(index: number): void {
    this.openedIndex = this.openedIndex === index ? null : index;
  }

  /** Cerrar todo (al navegar) */
  closeMenu(): void {
    this.openedIndex = null;
  }

  // ============================================================
  // ⭐ Permisos
  // ============================================================
  tienePermiso(item: any): boolean {
    if (this.session.esAdmin()) return true;
    if (!item?.permiso) return true;
    return this.session.tienePermiso(item.permiso);
  }

  logout(): void {
    this.session.logout();
  }
}
