import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { CatalogosApi, DepartamentoDTO, CiudadDTO } from '../catalogos.api';

@Component({
  standalone: true,
  selector: 'app-depto-ciudad',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './depto-ciudad.component.html',
  styleUrls: ['./depto-ciudad.component.scss']
})
export class DeptoCiudadComponent implements OnInit {
  private api = inject(CatalogosApi);

  @Input({ required: true }) deptoCtrl!: FormControl<number | null>;
  @Input({ required: true }) ciudadCtrl!: FormControl<number | null>;

  departamentos = signal<DepartamentoDTO[]>([]);
  ciudades = signal<CiudadDTO[]>([]);
  loadingDeptos = signal(false);
  loadingCiudades = signal(false);
  error = signal<string | null>(null);

  ngOnInit(): void {
    // ⚠️ NO deshabilitar deptoCtrl aquí
    // Solo CIUDAD inicia deshabilitada hasta elegir depto
    this.ciudadCtrl.disable({ emitEvent: false });

    this.cargarDepartamentos();

    // Cascada ciudad cuando cambia el departamento
    this.deptoCtrl.valueChanges.subscribe(id => {
      this.ciudadCtrl.setValue(null, { emitEvent: false });
      this.ciudadCtrl.disable({ emitEvent: false });
      this.ciudades.set([]);

      if (id != null) {
        this.cargarCiudades(id);
      }
    });
  }

  private cargarDepartamentos() {
    this.loadingDeptos.set(true);
    this.error.set(null);

    this.api.departamentos().subscribe({
      next: list => {
        this.departamentos.set(list);
        this.loadingDeptos.set(false);
        // Asegura que el control quede habilitado para seleccionar
        if (this.deptoCtrl.disabled) this.deptoCtrl.enable({ emitEvent: false });

        // Si ya venía un depto preseleccionado (editar), dispara la carga de ciudades
        const depId = this.deptoCtrl.value;
        if (depId != null) this.cargarCiudades(depId);
      },
      error: () => {
        this.loadingDeptos.set(false);
        this.error.set('No se pudieron cargar los departamentos');
        // Aun así, deja el control habilitado para no “bloquear” la UI
        if (this.deptoCtrl.disabled) this.deptoCtrl.enable({ emitEvent: false });
      }
    });
  }

  private cargarCiudades(idDepartamento: number) {
    this.loadingCiudades.set(true);
    this.error.set(null);

    this.api.ciudadesPorDepartamento(idDepartamento).subscribe({
      next: list => {
        this.ciudades.set(list);
        this.loadingCiudades.set(false);
        this.ciudadCtrl.enable({ emitEvent: false });
      },
      error: () => {
        this.loadingCiudades.set(false);
        this.error.set('No se pudieron cargar las ciudades');
        this.ciudadCtrl.disable({ emitEvent: false });
      }
    });
  }
}
