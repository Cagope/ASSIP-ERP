import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  OnInit,
  SimpleChanges,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormControl
} from '@angular/forms';

import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';

import { CuentasAhorroApi } from './cuentas-ahorro.api';
import {
  PersonasApi,
  PersonaBusquedaDTO
} from '../../../shared/personas/personas.api';

@Component({
  selector: 'app-cuentas-conjuntas',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './cuentas-conjuntas.component.html',
  styleUrls: ['./cuentas-conjuntas.component.scss']
})
export class CuentasConjuntasComponent implements OnChanges, OnInit {

  private readonly api = inject(CuentasAhorroApi);
  private readonly personasApi = inject(PersonasApi);

  @Input() idCuentaAhorro: number | null = null;
  @Input() accionesConjunta: any[] = [];

  @Output() cerrar = new EventEmitter<void>();

  asociadoCtrl = new FormControl<string>('', { nonNullable: true });

  lista: any[] = [];
  asociados: PersonaBusquedaDTO[] = [];

  cargando = false;
  error = '';
  errores: any = {};

  nuevo = this.nuevoModelo();

  ngOnInit(): void {
    this.autocompletePersonas();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idCuentaAhorro'] && this.idCuentaAhorro) {
      this.cargar();
    }
  }

  private nuevoModelo() {
    return {
      idDatosPersonal: 0,
      documento: '',
      nombreCompleto: '',
      codigoAccion: ''
    };
  }

  private autocompletePersonas(): void {
    this.asociadoCtrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(text =>
        typeof text === 'string' && text.trim().length >= 3
          ? this.personasApi.buscar(text.trim())
          : of([])
      )
    ).subscribe(data => {
      this.asociados = data ?? [];
    });
  }

  seleccionarAsociado(p: PersonaBusquedaDTO): void {
    this.nuevo.idDatosPersonal = p.idDatosPersonal;
    this.nuevo.documento = p.documento;
    this.nuevo.nombreCompleto = p.nombreCompleto;

    this.asociadoCtrl.setValue(
      `${p.nombreCompleto} (${p.documento})`,
      { emitEvent: false }
    );

    this.asociados = [];
    this.limpiarError('asociado');
  }

  cargar(): void {
    if (!this.idCuentaAhorro) return;

    this.cargando = true;
    this.error = '';

    this.api.listarCuentasConjuntas(this.idCuentaAhorro).subscribe({
      next: (res: any) => this.lista = res ?? [],
      error: () => this.error = 'No se pudieron cargar las cuentas conjuntas.',
      complete: () => this.cargando = false
    });
  }

  validarCampos(): boolean {
    this.errores = {};

    if (!this.nuevo.idDatosPersonal || this.nuevo.idDatosPersonal <= 0) {
      this.errores.asociado = 'Debe seleccionar un asociado.';
    }

    if (!this.nuevo.codigoAccion) {
      this.errores.accion = 'Debe seleccionar la acción conjunta.';
    }

    return Object.keys(this.errores).length === 0;
  }

  agregar(): void {
    if (!this.idCuentaAhorro) {
      alert('No hay cuenta seleccionada.');
      return;
    }

    if (!this.validarCampos()) return;

    this.api.agregarCuentaConjunta(this.idCuentaAhorro, this.nuevo).subscribe({
      next: (res: any) => {
        if (res?.ok) {
          this.limpiarFormulario();
          this.cargar();
        } else {
          alert(res?.mensaje ?? 'No se pudo agregar la cuenta conjunta.');
        }
      },
      error: () => alert('Error agregando cuenta conjunta.')
    });
  }

  eliminar(item: any): void {
    if (!this.idCuentaAhorro || !item?.idCuentaConjunta) return;

    if (!confirm('¿Eliminar este asociado conjunto?')) return;

    this.api.eliminarCuentaConjunta(
      this.idCuentaAhorro,
      item.idCuentaConjunta
    ).subscribe({
      next: (res: any) => {
        if (res?.ok) {
          this.cargar();
        } else {
          alert(res?.mensaje ?? 'No se pudo eliminar.');
        }
      },
      error: () => alert('Error eliminando cuenta conjunta.')
    });
  }

  limpiarError(campo: string): void {
    if (this.errores?.[campo]) {
      delete this.errores[campo];
    }
  }

  limpiarFormulario(): void {
    this.nuevo = this.nuevoModelo();
    this.errores = {};
    this.asociados = [];
    this.asociadoCtrl.setValue('', { emitEvent: false });
  }

  regresar(): void {
    this.cerrar.emit();
  }
}
