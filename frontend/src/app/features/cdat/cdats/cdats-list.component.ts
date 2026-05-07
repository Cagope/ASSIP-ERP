import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CdatsApi, CdatListDTO } from './cdats.api';

@Component({
  selector: 'app-cdats-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cdats-list.component.html',
  styleUrls: ['./cdats-list.component.scss']
})
export class CdatsListComponent implements OnInit {

  lista: CdatListDTO[] = [];
  cargando = false;
  error = '';

  constructor(private api: CdatsApi) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.error = '';
    this.cargando = true;

    this.api.listar().subscribe({
      next: data => {
        this.lista = data ?? [];
        this.cargando = false;
      },
      error: err => {
        this.error = err.error?.message || 'No se pudieron cargar los últimos CDATs.';
        this.cargando = false;
      }
    });
  }

  refrescar(): void {
    this.cargar();
  }
}
