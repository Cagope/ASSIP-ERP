
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
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

import { SessionService } from '../../core/auth/session.service';
import { MENU_REGISTRY } from '../../layout/menu-registry';
import { MenuNavigationService } from '../../layout/menu-navigation.service';

/* =========================================================
   ASSIP ERP - PANTALLA DE INICIO
   COOPVALLE
   ========================================================= */

interface OpcionMenu {
  label: string;
  route?: string;
  permiso?: string;
  children?: OpcionMenu[];
}

interface ModuloMenu {
  title: string;
  icon?: string;
  items: OpcionMenu[];
}

interface TarjetaModulo {
  index: number;
  title: string;
  icon?: string;
  descripcion: string;
}

@Component({
  standalone: true,
  selector: 'app-inicio',
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './inicio.component.html',
  styleUrls: ['./inicio.component.scss']
})
export class InicioComponent {

  readonly icons: Record<string, any> = {
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

  /* Descripciones institucionales de los módulos. */
  private readonly descripciones: Record<string, string> = {
    'Hoja de Vida': 'Administración de asociados y datos personales.',
    'Depósitos': 'Gestión de cuentas de ahorro y movimientos.',
    'Cartera': 'Originación, administración y recaudo de créditos.',
    'CDAT': 'Constitución, renovación y cancelación de certificados.',
    'Caja': 'Operaciones de recaudo y pago en efectivo.',
    'Tesorería': 'Gestión de operaciones bancarias y pagos.',
    'Activos Fijos': 'Registro y control de los activos de la entidad.',
    'Nómina': 'Administración de personal y liquidación de nómina.',
    'Contabilidad': 'Comprobantes, cierres y estados financieros.',
    'Gestión Gerencial': 'Consultas e indicadores para la toma de decisiones.',
    'Sarlaft': 'Gestión y seguimiento del riesgo LA/FT.',
    'Superintendencia': 'Generación de informes regulatorios.',
    'General': 'Configuración de parámetros generales del ERP.',
    'Seguridad': 'Administración de usuarios, roles y permisos.'
  };

  readonly username: string;
  readonly modulos: TarjetaModulo[];

  constructor(
    private readonly session: SessionService,
    private readonly menuNavigation: MenuNavigationService
  ) {
    this.username = this.session.getUser() ?? '';

    // Conservar el índice original para abrir el módulo correcto.
    this.modulos = (MENU_REGISTRY as ModuloMenu[])
      .map((modulo, index) => ({
        index,
        title: modulo.title,
        icon: modulo.icon,
        visible: modulo.items.some(item => this.opcionVisible(item))
      }))
      .filter(modulo => modulo.visible)
      .map(({ index, title, icon }) => ({
        index,
        title,
        icon,
        descripcion: this.descripciones[title] ??
          'Acceso a las funciones del módulo.'
      }));
  }

  /** Respeta los permisos definidos en cada nivel del menú. */
  private opcionVisible(item: OpcionMenu): boolean {
    if (item.permiso && !this.session.tienePermiso(item.permiso)) {
      return false;
    }

    if (item.children?.length) {
      return item.children.some(child => this.opcionVisible(child));
    }

    return !!item.route;
  }

  /** Expande el módulo lateral sin ejecutar una operación. */
  abrirModulo(index: number): void {
    this.menuNavigation.abrirModulo(index);
  }

  trackByIndex(_index: number, modulo: TarjetaModulo): number {
    return modulo.index;
  }
}
