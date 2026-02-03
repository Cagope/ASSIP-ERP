import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PersonasBusquedaApi } from './personas-busqueda.api';
import { PersonaBusquedaDTO } from './personas-busqueda.dto';

@Component({
  selector: 'app-personas-selector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './personas-selector.component.html',
  styleUrls: ['./personas-selector.component.scss'],
})

export class PersonasSelectorComponent {

  private api = inject(PersonasBusquedaApi);

  @Input() label = 'persona';
  @Output() seleccionar =
    new EventEmitter<PersonaBusquedaDTO>();

  q = '';
  resultados: PersonaBusquedaDTO[] = [];
  cargando = false;

  buscar(): void {
    if (!this.q || this.q.length < 3) {
      this.resultados = [];
      return;
    }

    this.cargando = true;
    this.api.buscar(this.q).subscribe({
      next: data => {
        this.resultados = data;
        this.cargando = false;
      },
      error: () => this.cargando = false
    });
  }

  elegir(p: PersonaBusquedaDTO): void {
    this.q = `${p.documento} - ${p.nombreCompleto}`;
    this.resultados = [];
    this.seleccionar.emit(p);
  }
}
