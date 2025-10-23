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
  username = '';

  constructor(private session: SessionService, private router: Router) {
    this.username = this.session.getUser() ?? '';
  }

  logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
