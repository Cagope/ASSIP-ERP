import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuariosApi, Usuario, ListResponse } from './usuarios.api';

@Component({
  selector: 'app-usuarios-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios-list.component.html',
  styleUrls: ['./usuarios-list.component.scss'],
})
export class UsuariosListComponent implements OnInit {
  // estado
  cargando = signal(false);
  error = signal<string | null>(null);
  data = signal<Usuario[]>([]);
  page = signal(0);
  size = signal(20);
  totalElements = signal(0);
  sort = signal('username,asc');

  // filtro (para ngModel)
  q = signal('');
  qStr: string = '';

  constructor(private api: UsuariosApi) {}

  ngOnInit(): void {
    // sincroniza la caja de texto con el signal
    this.qStr = this.q();
    this.fetch();
  }

  fetch(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.api
      .listar({
        q: this.q(),
        page: this.page(),
        size: this.size(),
        sort: this.sort(),
      })
      .subscribe({
        next: (res: ListResponse<Usuario>) => {
          this.data.set(res.data);
          this.totalElements.set(res.meta.totalElements);
          this.cargando.set(false);
        },
        error: (err) => {
          const msg =
            err?.error?.errors?.[0]?.message ??
            err?.error?.message ??
            'Error al cargar usuarios';
          this.error.set(msg);
          this.cargando.set(false);
        },
      });
  }

  onBuscar(): void {
    // pasa el valor del input al signal y recarga desde page 0
    this.q.set(this.qStr.trim());
    this.page.set(0);
    this.fetch();
  }

  onOrdenUsername(): void {
    this.sort.set(this.sort() === 'username,asc' ? 'username,desc' : 'username,asc');
    this.fetch();
  }

  nextPage(): void {
    const maxPage = Math.max(0, Math.ceil(this.totalElements() / this.size()) - 1);
    if (this.page() < maxPage) {
      this.page.set(this.page() + 1);
      this.fetch();
    }
  }

  prevPage(): void {
    if (this.page() > 0) {
      this.page.set(this.page() - 1);
      this.fetch();
    }
  }
}
