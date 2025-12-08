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

  username = '';
  menu = MENU_REGISTRY;

  /** 🔽 Estado del colapso (se recuerda por índice) */
  openSections: Record<number, boolean> = {};

  constructor(private session: SessionService) {
    this.username = this.session.getUser() ?? '';
  }

  logout(): void {
    this.session.logout();
  }

  /** Saber si la sección está abierta */
  isOpen(index: number): boolean {
    return !!this.openSections[index];
  }

  /** Alternar la apertura/cierre */
  toggleSection(index: number): void {
    this.openSections[index] = !this.openSections[index];
  }

  // ============================================================
  // ⭐ Bypass para ADMIN — siempre ve todo el menú
  // ============================================================
  tienePermiso(item: any): boolean {

    // 🔥 1. Admin ve todo
    if (this.session.esAdmin()) return true;

    // 🔥 2. El item no necesita permiso → mostrar
    if (!item || !item.permiso) return true;

    // 🔥 3. Usuarios normales sí dependen de la lista de permisos
    return this.session.tienePermiso(item.permiso);
  }
}
