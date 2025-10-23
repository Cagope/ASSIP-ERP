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

  constructor(private session: SessionService) {
    this.username = this.session.getUser() ?? '';
  }

  logout(): void {
    this.session.logout();
  }
}
