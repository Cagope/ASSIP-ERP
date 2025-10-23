import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private tokenKey = 'assip_token';
  private userKey = 'assip_user';

  // Signals reactivas (Angular 17+)
  tokenSig = signal<string | null>(null);
  userSig = signal<string | null>(null);

  constructor() {
    const storedToken = localStorage.getItem(this.tokenKey);
    const storedUser = localStorage.getItem(this.userKey);

    if (storedToken) this.tokenSig.set(storedToken);
    if (storedUser) this.userSig.set(storedUser);
  }

  // === TOKEN ===
  setToken(token: string) {
    localStorage.setItem(this.tokenKey, token);
    this.tokenSig.set(token);
  }

  getToken(): string | null {
    return this.tokenSig() || localStorage.getItem(this.tokenKey);
  }

  // === USUARIO ===
  setUser(username: string) {
    localStorage.setItem(this.userKey, username);
    this.userSig.set(username);
  }

  getUser(): string | null {
    return this.userSig() || localStorage.getItem(this.userKey);
  }

  // === ESTADO DE SESIÓN ===
  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && token.trim() !== '';
  }

  // === CIERRE DE SESIÓN ===
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.tokenSig.set(null);
    this.userSig.set(null);
  }

  // === ALTERNATIVA: limpiar todo (si el backend también tiene logout) ===
  clear(): void {
    this.logout();
  }
}
