import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { SessionService } from '../../core/auth/session.service';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule, MatButtonModule
  ],
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
})
export class MenuComponent {
  private session = inject(SessionService);
  private router = inject(Router);

  appTitle = 'ASSIP ERP';
  username = computed(() => this.session.me()?.username ?? '...');

  logout(): void {
    this.session.logout();
    this.router.navigateByUrl('/login');
  }

  /**
   * Si el usuario no está autenticado, lo redirige a /login con returnUrl a /hoja-vida/sarlaft.
   * Si ya está autenticado, navega directamente a SARLAFT.
   */
  openSarlaft(ev?: Event): void {
    if (ev) ev.preventDefault();
    const isLogged = !!this.session.me();
    const target = '/hoja-vida/sarlaft';

    if (!isLogged) {
      const returnUrl = encodeURIComponent(target);
      this.router.navigateByUrl(`/login?returnUrl=${returnUrl}`);
    } else {
      this.router.navigateByUrl(target);
    }
  }
}
