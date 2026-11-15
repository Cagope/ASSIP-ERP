
import {
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  Router,
  RouterOutlet,
  RouterLink,
  NavigationEnd
} from '@angular/router';

import {
  Subject,
  filter,
  takeUntil
} from 'rxjs';

import { MainMenuComponent } from './main-menu.component';
import { SessionService } from '../core/auth/session.service';
import { MENU_REGISTRY } from './menu-registry';

/* =========================================================
   ASSIP ERP - LAYOUT PRINCIPAL
   COOPVALLE
   ========================================================= */

/* Estructura de navegación */

interface BreadcrumbItem {
  label: string;
  route?: string;
}

interface MenuNavigationItem {
  label: string;
  route?: string;
  permiso?: string;
  children?: MenuNavigationItem[];
}

interface MenuNavigationSection {
  title: string;
  items: MenuNavigationItem[];
}

@Component({
  standalone: true,
  selector: 'app-main-layout',
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    MainMenuComponent
  ],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent implements OnInit, OnDestroy {

  /* =========================================================
     SESIÓN Y AGENCIA
     ========================================================= */

  /** Usuario autenticado */
  username: string = '';

  /** Agencia activa del usuario */
  agenciaActual: {
    idAgencia: number | null;
    codigoAgencia: string | null;
    nombreAgencia: string | null;
  } | null = null;

  /* =========================================================
     RUTA DE NAVEGACIÓN
     ========================================================= */

  /** Elementos que representan la ubicación actual */
  breadcrumbs: BreadcrumbItem[] = [];

  /** Control de suscripciones */
  private readonly destroy$ = new Subject<void>();

  constructor(
    private session: SessionService,
    private router: Router
  ) {
    this.username = this.session.getUser() ?? '';
    this.agenciaActual = this.session.getAgenciaActiva();
  }

  /* =========================================================
     CICLO DE VIDA
     ========================================================= */

  ngOnInit(): void {
    this.actualizarBreadcrumb();

    this.router.events
      .pipe(
        filter(
          (event): event is NavigationEnd =>
            event instanceof NavigationEnd
        ),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.actualizarBreadcrumb();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /* =========================================================
     CONSTRUCCIÓN DEL BREADCRUMB
     ========================================================= */

  /**
   * Actualiza la ruta de navegación utilizando
   * las opciones registradas en MENU_REGISTRY.
   */
  private actualizarBreadcrumb(): void {
    const urlActual = this.normalizarRuta(
      this.router.url
    );

    let mejorCoincidencia: BreadcrumbItem[] = [];
    let longitudCoincidencia = -1;

    const menu = MENU_REGISTRY as MenuNavigationSection[];

    for (const section of menu) {

      for (const item of section.items) {

        const rutaEncontrada = this.buscarRutaEnMenu(
          item,
          urlActual,
          [
            { label: section.title }
          ]
        );

        if (!rutaEncontrada) {
          continue;
        }

        if (
          rutaEncontrada.longitudRuta >
          longitudCoincidencia
        ) {
          longitudCoincidencia =
            rutaEncontrada.longitudRuta;

          mejorCoincidencia =
            rutaEncontrada.breadcrumbs;
        }
      }
    }

    this.breadcrumbs = mejorCoincidencia;
  }

  /**
   * Recorre los niveles del menú y devuelve
   * la ruta jerárquica correspondiente.
   */
  private buscarRutaEnMenu(
    item: MenuNavigationItem,
    urlActual: string,
    rutaPadre: BreadcrumbItem[]
  ): {
    breadcrumbs: BreadcrumbItem[];
    longitudRuta: number;
  } | null {

    const rutaActual: BreadcrumbItem[] = [
      ...rutaPadre,
      {
        label: item.label,
        route: item.route
      }
    ];

    let resultado: {
      breadcrumbs: BreadcrumbItem[];
      longitudRuta: number;
    } | null = null;

    /* Comprobar ruta del elemento actual */
    if (
      item.route &&
      this.rutaCoincide(item.route, urlActual)
    ) {
      resultado = {
        breadcrumbs: rutaActual,
        longitudRuta: this.normalizarRuta(
          item.route
        ).length
      };
    }

    /* Buscar rutas más específicas en los hijos */
    for (const child of item.children ?? []) {

      const coincidencia = this.buscarRutaEnMenu(
        child,
        urlActual,
        rutaActual
      );

      if (
        coincidencia &&
        (
          !resultado ||
          coincidencia.longitudRuta >
            resultado.longitudRuta
        )
      ) {
        resultado = coincidencia;
      }
    }

    return resultado;
  }

  /* =========================================================
     VALIDACIÓN DE RUTAS
     ========================================================= */

  /**
   * Elimina parámetros de consulta, fragmentos
   * y barras finales para comparar rutas.
   */
  private normalizarRuta(url: string): string {
    const ruta = url
      .split(/[?#]/)[0]
      .replace(/\/+$/, '');

    return ruta || '/';
  }

  /**
   * Comprueba coincidencia exacta o una ruta hija.
   * Evita coincidencias entre prefijos similares.
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

  /* =========================================================
     CIERRE DE SESIÓN
     ========================================================= */

  logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
