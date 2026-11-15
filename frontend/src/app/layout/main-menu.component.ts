import { Component, OnInit, OnDestroy } from '@angular/core';
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
  Wallet,
  HandCoins,
  Search
} from 'lucide-angular';

import {
  Router,
  RouterLink,
  RouterLinkActive,
  NavigationEnd
} from '@angular/router';

import { Subject, filter, takeUntil } from 'rxjs';

import { SessionService } from '../core/auth/session.service';
import { MENU_REGISTRY } from './menu-registry';
import { MenuNavigationService } from './menu-navigation.service';

/* =========================================================
   MODELOS DEL MENÚ
   ========================================================= */

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

interface RutaMenu {
  sectionIndex: number;
  groupIndex: number;
  subGroupIndex: number | null;
  route: string;
}

/* =========================================================
   COMPONENTE PRINCIPAL
   ========================================================= */

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
  styleUrls: ['./main-menu.component.scss']
})
export class MainMenuComponent implements OnInit, OnDestroy {

  menu: MenuSection[] = MENU_REGISTRY;

  icons: Record<string, any> = {
    hojaVida: UserRound,
    depositos: Landmark,
    cartera: HandCoins,
    contabilidad: Calculator,
    activos: Building2,
    nomina: Users,
    sarlaf: ShieldCheck,
    superintendencia: FileText,
    general: Settings,
    seguridad: LockKeyhole,
    cdat: PiggyBank,
    cajas: Wallet,
    tesoreria: Landmark,
    consultas: Search
  };

  /* Estado de expansión */

  openedIndex: number | null = null;
  openedGroupIndex: number | null = null;
  openedSubGroupKey: string | null = null;

  /* Identificación independiente de la ruta activa */

  activeSectionIndex: number | null = null;

  // Conserva el módulo desde el cual se abrió una ruta compartida.
  private origenNavegacionIndex: number | null = null;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private session: SessionService,
    private router: Router,
    private menuNavigation: MenuNavigationService
  ) {}

  /* =========================================================
     CICLO DE VIDA
     ========================================================= */

  ngOnInit(): void {

    // Identificar el módulo correspondiente a la ruta actual.
    this.abrirSegunRuta();

    // Actualizar el menú cuando cambia la ruta.
    this.router.events
      .pipe(
        filter(
          (event): event is NavigationEnd =>
            event instanceof NavigationEnd
        ),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.abrirSegunRuta();
      });

    // Abrir módulos desde las tarjetas de la pantalla principal.
    this.menuNavigation.abrirModulo$
      .pipe(
        takeUntil(this.destroy$)
      )
      .subscribe((index: number) => {

        if (index < 0 || index >= this.menu.length) {
          return;
        }

        // Expandir el módulo seleccionado.
        this.openedIndex = index;

        // Cerrar grupos y subgrupos anteriores.
        this.openedGroupIndex = null;
        this.openedSubGroupKey = null;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /* =========================================================
     NIVEL 1 - MÓDULOS
     ========================================================= */

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

  /**
   * Solo el módulo identificado por la ruta
   * puede tener el estado active-section.
   */
  seccionActiva(section: MenuSection): boolean {
    const index = this.menu.indexOf(section);

    return index >= 0 &&
      this.activeSectionIndex === index;
  }

  /* =========================================================
     NIVEL 2 - GRUPOS
     ========================================================= */

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

  /* =========================================================
     NIVEL 3 - SUBGRUPOS
     ========================================================= */

  isSubGroupOpen(
    groupIndex: number,
    subGroupIndex: number
  ): boolean {
    return this.openedSubGroupKey ===
      `${groupIndex}-${subGroupIndex}`;
  }

  toggleSubGroup(
    groupIndex: number,
    subGroupIndex: number
  ): void {
    const key = `${groupIndex}-${subGroupIndex}`;

    this.openedSubGroupKey =
      this.openedSubGroupKey === key
        ? null
        : key;
  }

  /* =========================================================
     SEGURIDAD
     ========================================================= */

  tienePermiso(item: MenuItem): boolean {
    if (item.permiso && !this.session.tienePermiso(item.permiso)) {
      return false;
    }
    // Un grupo solo aparece cuando contiene opciones autorizadas.
    if (item.children?.length) {
      return item.children.some(child => this.tienePermiso(child));
    }
    // Las opciones sin permiso se mantienen provisionalmente hasta
    // completar la auditoría de los demás módulos.
    return true;
  }

  seccionVisible(section: MenuSection): boolean {
    return section.items.some(item => this.tienePermiso(item));
  }

  /* =========================================================
     IDENTIFICACIÓN DE RUTAS
     ========================================================= */

  /**
   * Normaliza las rutas sin parámetros de consulta,
   * fragmentos ni barras finales.
   */
  private normalizarRuta(url: string): string {
    const ruta = url
      .split(/[?#]/)[0]
      .replace(/\/+$/, '');

    return ruta || '/';
  }

  /**
   * Exige coincidencia completa o un segmento hijo.
   * Evita confundir rutas con prefijos similares.
   */
  private rutaCoincide(
    rutaMenu: string,
    urlActual: string
  ): boolean {
    const ruta = this.normalizarRuta(rutaMenu);
    const actual = this.normalizarRuta(urlActual);

    if (ruta === actual) {
      return true;
    }

    if (ruta === '/') {
      return false;
    }

    return actual.startsWith(`${ruta}/`);
  }

  /**
   * Localiza la ruta más específica dentro del
   * registro completo de módulos.
   *
   * Así se evita que una ruta padre tenga prioridad
   * sobre una opción más específica.
   */
  private buscarRutaActiva(
    url: string
  ): RutaMenu | null {
    let resultado: RutaMenu | null = null;

    this.menu.forEach((section, sectionIndex) => {
      section.items.forEach((item, groupIndex) => {

        const recorrer = (
          actual: MenuItem,
          subGroupIndex: number | null
        ): void => {

          if (
            actual.route &&
            this.rutaCoincide(actual.route, url)
          ) {
            if (
              !resultado ||
              this.normalizarRuta(actual.route).length >
                this.normalizarRuta(resultado.route).length
            ) {
              resultado = {
                sectionIndex,
                groupIndex,
                subGroupIndex,
                route: actual.route
              };
            }
          }

          actual.children?.forEach(
            (child, childIndex) => {
              recorrer(
                child,
                subGroupIndex === null &&
                !!child.children?.length
                  ? childIndex
                  : subGroupIndex
              );
            }
          );
        };

        recorrer(item, null);
      });
    });

    return resultado;
  }

  /**
   * Busca una coincidencia solo en el módulo desde el que se navegó.
   * Respeta el permiso de cada opción y prioriza la ruta más específica.
   */
  private buscarRutaEnSeccion(
    url: string,
    sectionIndex: number
  ): RutaMenu | null {
    const section = this.menu[sectionIndex];
    if (!section) {
      return null;
    }

    let resultado: RutaMenu | null = null;

    section.items.forEach((item, groupIndex) => {
      const recorrer = (
        actual: MenuItem,
        subGroupIndex: number | null
      ): void => {
        if (!this.tienePermiso(actual)) {
          return;
        }

        if (actual.route && this.rutaCoincide(actual.route, url)) {
          if (!resultado ||
              this.normalizarRuta(actual.route).length >
              this.normalizarRuta(resultado.route).length) {
            resultado = {
              sectionIndex,
              groupIndex,
              subGroupIndex,
              route: actual.route
            };
          }
        }

        actual.children?.forEach((child, childIndex) => {
          recorrer(
            child,
            subGroupIndex === null && !!child.children?.length
              ? childIndex
              : subGroupIndex
          );
        });
      };

      recorrer(item, null);
    });

    return resultado;
  }

  /**
   * Determina si un grupo contiene la ruta actual.
   */
  grupoActivo(item: MenuItem): boolean {
    const url = this.router.url;

    if (
      item.route &&
      this.rutaCoincide(item.route, url)
    ) {
      return true;
    }

    return !!item.children?.some(child =>
      this.grupoActivo(child)
    );
  }

  /* =========================================================
     APERTURA AUTOMÁTICA
     ========================================================= */

  private abrirSegunRuta(): void {
    const rutaActiva = this.buscarRutaActiva(
      this.router.url
    );

    // Cuando una ruta se comparte, se prioriza el módulo elegido por el usuario.
    // Al salir de esa ruta, se vuelve a la identificación normal por URL.
    let seleccion = rutaActiva;

    if (this.origenNavegacionIndex !== null) {
      const origen = this.buscarRutaEnSeccion(
        this.router.url,
        this.origenNavegacionIndex
      );

      if (origen) {
        seleccion = origen;
      } else {
        this.origenNavegacionIndex = null;
      }
    }

    this.activeSectionIndex = seleccion?.sectionIndex ?? null;

    if (!seleccion) {
      return;
    }

    this.openedIndex = seleccion.sectionIndex;

    const section = this.menu[seleccion.sectionIndex];
    const item = section.items[seleccion.groupIndex];

    this.openedGroupIndex = item.children?.length
      ? seleccion.groupIndex
      : null;

    this.openedSubGroupKey =
      seleccion.subGroupIndex !== null &&
      this.openedGroupIndex !== null
        ? `${seleccion.groupIndex}-${seleccion.subGroupIndex}`
        : null;
  }

  /* =========================================================
     NAVEGACIÓN Y SESIÓN
     ========================================================= */

  closeMenu(sectionIndex: number): void {
    // La misma ruta puede existir en Gestión Gerencial y en su módulo de origen.
    this.origenNavegacionIndex = sectionIndex;
  }

  logout(): void {
    this.session.logout();
  }
}
