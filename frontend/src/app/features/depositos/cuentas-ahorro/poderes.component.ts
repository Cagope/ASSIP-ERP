import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  inject
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PoderDTO } from '../../../shared/models/poder.model';
import { CuentasAhorroApi } from './cuentas-ahorro.api';

@Component({
  selector: 'app-poderes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './poderes.component.html',
  styleUrls: ['./poderes.component.scss']
})
export class PoderesComponent implements OnChanges {

  private readonly api = inject(CuentasAhorroApi);

  @Input() idCuentaAhorro: number | null = null;
  @Input() poderes: PoderDTO[] = [];

  @Output() poderesChange = new EventEmitter<PoderDTO[]>();
  @Output() cerrar = new EventEmitter<void>();

  cargando = false;
  error = '';
  errores: any = {};

  editando = false;
  idPoderEditando: number | null = null;

  nuevo: PoderDTO = this.resetNuevo();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idCuentaAhorro'] && this.idCuentaAhorro) {
      this.cargar();
    }
  }

  private resetNuevo(): PoderDTO {
    return {
      idPoder: undefined,
      idCuenta: undefined,
      documentoPoder: '',
      nombrePoder: '',
      telefonoPoder: '',
      celularPoder: '',
      correoPoder: ''
    };
  }

  cargar(): void {
    if (!this.idCuentaAhorro) return;

    this.cargando = true;
    this.error = '';

    this.api.listarPoderes(this.idCuentaAhorro).subscribe({
      next: (res: any) => {
        this.poderes = res ?? [];
        this.poderesChange.emit(this.poderes);
      },
      error: () => this.error = 'No se pudieron cargar los poderes.',
      complete: () => this.cargando = false
    });
  }

  validarCampos(): boolean {
    this.errores = {};

    const documento = String(this.nuevo.documentoPoder ?? '').trim();
    const nombre = String(this.nuevo.nombrePoder ?? '').trim();
    const telefono = String(this.nuevo.telefonoPoder ?? '').trim();
    const celular = String(this.nuevo.celularPoder ?? '').trim();
    const correo = String(this.nuevo.correoPoder ?? '').trim();

    if (!documento) {
      this.errores.documento = 'El documento es obligatorio.';
    }

    if (!nombre) {
      this.errores.nombre = 'El nombre completo es obligatorio.';
    }

    if (telefono && !/^\d{7}$/.test(telefono)) {
      this.errores.telefono = 'Debe estar vacío o tener 7 dígitos numéricos.';
    }

    if (celular && !/^\d{10}$/.test(celular)) {
      this.errores.celular = 'Debe estar vacío o tener 10 dígitos numéricos.';
    }

    if (correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
      this.errores.correo = 'Digite un correo electrónico válido.';
    }

    return Object.keys(this.errores).length === 0;
  }

  agregar(): void {
    if (!this.idCuentaAhorro) {
      alert('No hay cuenta seleccionada.');
      return;
    }

    if (!this.validarCampos()) return;

    this.normalizarNuevo();

    if (this.editando && this.idPoderEditando) {
      this.api.actualizarPoder(
        this.idCuentaAhorro,
        this.idPoderEditando,
        this.nuevo
      ).subscribe({
        next: (res: any) => {
          if (res?.ok) {
            this.cancelarEdicion();
            this.cargar();
          } else {
            alert(res?.mensaje ?? 'No se pudo actualizar el poder.');
          }
        },
        error: () => alert('Error actualizando poder.')
      });

      return;
    }

    this.api.agregarPoder(this.idCuentaAhorro, this.nuevo).subscribe({
      next: (res: any) => {
        if (res?.ok) {
          this.nuevo = this.resetNuevo();
          this.errores = {};
          this.cargar();
        } else {
          alert(res?.mensaje ?? 'No se pudo agregar el poder.');
        }
      },
      error: () => alert('Error agregando poder.')
    });
  }

  editar(item: PoderDTO): void {
    this.editando = true;
    this.idPoderEditando = item.idPoder ?? null;

    this.nuevo = {
      idPoder: item.idPoder,
      idCuenta: item.idCuenta,
      documentoPoder: item.documentoPoder ?? '',
      nombrePoder: item.nombrePoder ?? '',
      telefonoPoder: item.telefonoPoder ?? '',
      celularPoder: item.celularPoder ?? '',
      correoPoder: item.correoPoder ?? ''
    };

    this.errores = {};
  }

  cancelarEdicion(): void {
    this.editando = false;
    this.idPoderEditando = null;
    this.nuevo = this.resetNuevo();
    this.errores = {};
  }

  limpiarError(campo: string): void {
    if (this.errores?.[campo]) {
      delete this.errores[campo];
    }
  }

  eliminar(i: number): void {
    if (!this.idCuentaAhorro) return;

    const item = this.poderes[i];

    if (!item?.idPoder) {
      alert('No se encontró el identificador del poder.');
      return;
    }

    if (!confirm('¿Eliminar este poder?')) return;

    this.api.eliminarPoder(this.idCuentaAhorro, item.idPoder).subscribe({
      next: (res: any) => {
        if (res?.ok) {
          this.cargar();
        } else {
          alert(res?.mensaje ?? 'No se pudo eliminar el poder.');
        }
      },
      error: () => alert('Error eliminando poder.')
    });
  }

  regresar(): void {
    this.cerrar.emit();
  }

  private normalizarNuevo(): void {
    this.nuevo.documentoPoder = String(this.nuevo.documentoPoder ?? '').trim();
    this.nuevo.nombrePoder = String(this.nuevo.nombrePoder ?? '').trim();
    this.nuevo.telefonoPoder = String(this.nuevo.telefonoPoder ?? '').trim();
    this.nuevo.celularPoder = String(this.nuevo.celularPoder ?? '').trim();
    this.nuevo.correoPoder = String(this.nuevo.correoPoder ?? '').trim();
  }
}
