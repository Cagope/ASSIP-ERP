import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { CatalogosApi, Departamento, Ciudad } from '../catalogos.api';

@Component({
  selector: 'app-depto-ciudad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './depto-ciudad.component.html',
  styleUrls: ['./depto-ciudad.component.scss']
})
export class DeptoCiudadComponent implements OnInit {
  private readonly api = inject(CatalogosApi);

  departamentos: Departamento[] = [];
  ciudades: Ciudad[] = [];

  @Input() idDepartamento?: number;
  @Input() idCiudad?: number;
  @Output() cambioDepto = new EventEmitter<number>();
  @Output() cambioCiudad = new EventEmitter<number>();

  ngOnInit(): void {
    this.api.listarDepartamentos().subscribe({
      next: (data) => (this.departamentos = data)
    });

    if (this.idDepartamento) {
      this.cargarCiudades(this.idDepartamento);
    }
  }

  // ✅ versión corregida: el evento se maneja tipado
  onDeptoChange(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    this.idDepartamento = id;
    this.cargarCiudades(id);
    this.cambioDepto.emit(id);
  }

  onCiudadChange(event: Event): void {
    const id = Number((event.target as HTMLSelectElement).value);
    this.idCiudad = id;
    this.cambioCiudad.emit(id);
  }

  private cargarCiudades(idDepartamento: number): void {
    this.api.listarCiudadesPorDepartamento(idDepartamento).subscribe({
      next: (data) => (this.ciudades = data)
    });
  }
}
