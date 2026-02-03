import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { MainMenuComponent } from './main-menu.component';
import { SessionService } from '../core/auth/session.service';

@Component({
  standalone: true,
  selector: 'app-main-layout',
  imports: [CommonModule, RouterOutlet, MainMenuComponent],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent {

  /** Usuario autenticado */
  username: string = '';

  /** Agencia activa del usuario */
  agenciaActual: {
    idAgencia: number | null;
    codigoAgencia: string | null;
    nombreAgencia: string | null;
  } | null = null;

  constructor(
    private session: SessionService,
    private router: Router
  ) {
    this.username = this.session.getUser() ?? '';
    this.agenciaActual = this.session.getAgenciaActiva();
  }

  logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
