import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { SessionService } from '../core/auth/session.service';
import { AuthApi } from '../core/auth/auth.api';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [CommonModule, RouterOutlet],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  username: string = '';

  constructor(
    private session: SessionService,
    private authApi: AuthApi,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.username = this.session.getUser() ?? '';

    this.authApi.me().subscribe({
      next: () => {},
      error: () => {
        this.session.logout();
        this.router.navigate(['/login']);
      },
    });
  }

  isAuthenticated(): boolean {
    return this.session.isAuthenticated();
  }

  logout(): void {
    this.session.logout();
    this.router.navigate(['/login']);
  }
}
