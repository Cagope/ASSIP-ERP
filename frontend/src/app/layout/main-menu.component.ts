import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideAngularModule,
  UserRound,
  Landmark,
  Calculator,
  Building2,
  Users,
  ShieldCheck,
  FileText,
  Settings,
  LockKeyhole,
  PiggyBank,
  Gauge
} from 'lucide-angular';

import {
  Router,
  RouterLink,
  RouterLinkActive,
  NavigationEnd
} from '@angular/router';
import { filter } from 'rxjs/operators';

import { SessionService } from '../core/auth/session.service';
import { MENU_REGISTRY } from './menu-registry';

export interface MenuItem {
  label: string;
  route?: string;
  permiso?: string;
  children?: MenuItem[];
}

export interface MenuSection {
  title: string;
  icon?: string;
  items: MenuItem[];
}

@Component({
  standalone: true,
  selector: 'app-main-menu',
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    LucideAngularModule
  ],
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.scss'],
})
export class MainMenuComponent implements OnInit {

  menu: MenuSection[] = MENU_REGISTRY;

  icons: Record<string, any> = {
    hojaVida: UserRound,
    depositos: Landmark,
    contabilidad: Calculator,
    activos: Building2,
    nomina: Users,
    sarlaf: ShieldCheck,
    superintendencia: FileText,
    general: Settings,
    seguridad: LockKeyhole,
    cdat: PiggyBank,
    gerencia: Gauge
  };

  openedIndex: number | null = null;
  openedGroupIndex: number | null = null;
  openedSubGroupKey: string | null = null;

  constructor(
    private session: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.abrirSegunRuta();

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.abrirSegunRuta());
  }

  isOpen(index: number): boolean {
    return this.openedIndex === index;
  }

  toggleSection(index: number): void {
    if (this.openedIndex === index) {
      this.openedIndex = null;
      this.openedGroupIndex = null;
      this.openedSubGroupKey = null;
      return;
    }

    this.openedIndex = index;
    this.openedGroupIndex = null;
    this.openedSubGroupKey = null;
  }

  isGroupOpen(index: number): boolean {
    return this.openedGroupIndex === index;
  }

  toggleGroup(index: number): void {
    if (this.openedGroupIndex === index) {
      this.openedGroupIndex = null;
      this.openedSubGroupKey = null;
      return;
    }

    this.openedGroupIndex = index;
    this.openedSubGroupKey = null;
  }

  isSubGroupOpen(groupIndex: number, subGroupIndex: number): boolean {
    return this.openedSubGroupKey === `${groupIndex}-${subGroupIndex}`;
  }

  toggleSubGroup(groupIndex: number, subGroupIndex: number): void {
    const key = `${groupIndex}-${subGroupIndex}`;
    this.openedSubGroupKey = this.openedSubGroupKey === key ? null : key;
  }

  closeMenu(): void {
    this.abrirSegunRuta();
  }

  tienePermiso(item: MenuItem): boolean {
    if (this.session.esAdmin()) return true;
    if (!item?.permiso) return true;
    return this.session.tienePermiso(item.permiso);
  }

  grupoActivo(item: MenuItem): boolean {
    const url = this.router.url;
    return this.itemContieneRuta(item, url);
  }

  private abrirSegunRuta(): void {
    const url = this.router.url;

    this.menu.forEach((section, sectionIndex) => {

      const tieneRutaDirecta = section.items.some(item =>
        !!item.route && url.startsWith(item.route)
      );

      const groupIndex = section.items.findIndex(item =>
        this.itemContieneRuta(item, url)
      );

      if (tieneRutaDirecta || groupIndex >= 0) {
        this.openedIndex = sectionIndex;
        this.openedGroupIndex = groupIndex >= 0 ? groupIndex : null;
        this.openedSubGroupKey = null;

        if (groupIndex >= 0) {
          const group = section.items[groupIndex];

          const subGroupIndex = group.children?.findIndex(child =>
            child.children?.some(subchild =>
              !!subchild.route && url.startsWith(subchild.route)
            )
          ) ?? -1;

          if (subGroupIndex >= 0) {
            this.openedSubGroupKey = `${groupIndex}-${subGroupIndex}`;
          }
        }
      }
    });
  }

  private itemContieneRuta(item: MenuItem, url: string): boolean {
    if (item.route && url.startsWith(item.route)) {
      return true;
    }

    return !!item.children?.some(child => this.itemContieneRuta(child, url));
  }

  logout(): void {
    this.session.logout();
  }
}
