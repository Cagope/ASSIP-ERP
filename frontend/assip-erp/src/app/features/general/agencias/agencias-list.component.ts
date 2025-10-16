import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AgenciasApi } from './agencias.api';

@Component({
  selector: 'app-agencias-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './agencias-list.component.html',
  styleUrls: ['./agencias-list.component.scss'],
})
export class AgenciasListComponent implements OnInit {
  private api = inject(AgenciasApi);
  private router = inject(Router);

  q = signal<string>('');
  loading = signal<boolean>(false);
  error = signal<string | null>(null);
  data = signal<any[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.api.list(this.q()).subscribe({
      next: (res: any) => {
        const rows = Array.isArray(res) ? res : res?.content ?? [];
        this.data.set(rows);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Error cargando agencias');
        this.loading.set(false);
      },
    });
  }

  crear(): void {
    this.router.navigate(['/general/agencias/new']);
  }

  editar(id: number): void {
    this.router.navigate(['/general/agencias', id, 'edit']);
  }

  eliminar(id: number): void {
    if (!confirm('¿Eliminar esta agencia?')) return;
    this.loading.set(true);

    this.api.remove(id).subscribe({
      next: () => this.load(),
      error: () => {
        this.error.set('No se pudo eliminar');
        this.loading.set(false);
      },
    });
  }
}
