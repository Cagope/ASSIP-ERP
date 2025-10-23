import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

/**
 * 📋 Componente principal del módulo Hoja de Vida.
 * Este componente actúa como contenedor y menú lateral
 * para las secciones internas (Datos Personales, Ubicaciones, etc).
 *
 * 🔸 Cada equipo de esquema (Hoja de Vida, Contabilidad, etc.)
 * puede mantener su propio menú de forma independiente.
 */
@Component({
  standalone: true,
  selector: 'app-hoja-vida-menu',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="hoja-vida-wrapper">
      <aside class="menu-lateral">
        <h2>🧾 Hoja de Vida</h2>
        <nav>
          <a routerLink="datos-personales" routerLinkActive="active">Datos Personales</a>
          <a routerLink="ubicaciones" routerLinkActive="active">Ubicación y Dirección</a>
          <a routerLink="laborales" routerLinkActive="active">Datos Laborales</a>
          <a routerLink="economicos" routerLinkActive="active">Datos Económicos y Financieros</a>
          <a routerLink="referencias-familiares" routerLinkActive="active">Referencia Familiar</a>
          <a routerLink="referencias-personales" routerLinkActive="active">Referencia Personal</a>
          <a routerLink="sarlaft" routerLinkActive="active">Datos para SARLAFT</a>
          <a routerLink="permisos-especiales" routerLinkActive="active">Permisos Especiales</a>
        </nav>
      </aside>

      <main class="contenido">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styleUrls: ['./hoja-vida-menu.component.scss'],
})
export class HojaVidaMenuComponent {}
