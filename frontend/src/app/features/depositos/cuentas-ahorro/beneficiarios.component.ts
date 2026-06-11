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
import { FormsModule } from '@angular/forms';

import { BeneficiarioDTO } from '../../../shared/models/beneficiario.model';
import { CuentasAhorroApi } from './cuentas-ahorro.api';
import { CatalogosApi } from '../../../shared/catalogos/catalogos.api';

@Component({
  selector: 'app-beneficiarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './beneficiarios.component.html',
  styleUrls: ['./beneficiarios.component.scss']
})
export class BeneficiariosComponent implements OnChanges, OnInit {

  private readonly api = inject(CuentasAhorroApi);
  private readonly catalogosApi = inject(CatalogosApi);

  @Input() idCuentaAhorro: number | null = null;
  @Input() beneficiarios: BeneficiarioDTO[] = [];

  @Output() beneficiariosChange = new EventEmitter<BeneficiarioDTO[]>();
  @Output() cerrar = new EventEmitter<void>();

  parentescos: any[] = [];
  errores: any = {};

  cargando = false;
  error = '';

  nuevo: BeneficiarioDTO = this.resetNuevo();
  editando = false;
  idBeneficiarioEditando: number | null = null;

  ngOnInit(): void {
    this.cargarParentescos();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idCuentaAhorro'] && this.idCuentaAhorro) {
      this.cargar();
    }
  }

  private resetNuevo(): BeneficiarioDTO {
    return {
      idBeneficiario: undefined,
      idCuentaAhorro: undefined,
      documentoBeneficiario: '',
      nombreBeneficiario: '',
      telefonoBeneficiario: '',
      celularBeneficiario: '',
      tipoParentesco: ''
    };
  }

  private cargarParentescos(): void {
    this.catalogosApi.listarParentescos().subscribe({
      next: (res: any) => {
        this.parentescos = res ?? [];
      },
      error: () => {
        this.parentescos = [];
      }
    });
  }

  cargar(): void {
    if (!this.idCuentaAhorro) {
      return;
    }

    this.cargando = true;
    this.error = '';

    this.api.listarBeneficiarios(this.idCuentaAhorro).subscribe({
      next: (res: any) => {
        this.beneficiarios = res ?? [];
        this.beneficiariosChange.emit(this.beneficiarios);
      },
      error: () => {
        this.error = 'No se pudieron cargar los beneficiarios.';
      },
      complete: () => {
        this.cargando = false;
      }
    });
  }

  validarCampos(): boolean {
    this.errores = {};

    const documento = String(this.nuevo.documentoBeneficiario ?? '').trim();
    const nombre = String(this.nuevo.nombreBeneficiario ?? '').trim();
    const telefono = String(this.nuevo.telefonoBeneficiario ?? '').trim();
    const celular = String(this.nuevo.celularBeneficiario ?? '').trim();
    const parentesco = String(this.nuevo.tipoParentesco ?? '').trim();

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

    if (!parentesco) {
      this.errores.parentesco = 'Seleccione el parentesco.';
    }

    return Object.keys(this.errores).length === 0;
  }

  agregar(): void {
    if (!this.idCuentaAhorro) {
      alert('No hay cuenta seleccionada.');
      return;
    }

    if (!this.validarCampos()) {
      return;
    }

    this.nuevo.documentoBeneficiario =
      String(this.nuevo.documentoBeneficiario ?? '').trim();

    this.nuevo.nombreBeneficiario =
      String(this.nuevo.nombreBeneficiario ?? '').trim();

    this.nuevo.telefonoBeneficiario =
      String(this.nuevo.telefonoBeneficiario ?? '').trim();

    this.nuevo.celularBeneficiario =
      String(this.nuevo.celularBeneficiario ?? '').trim();

    this.nuevo.tipoParentesco =
      String(this.nuevo.tipoParentesco ?? '').trim();

    if (this.editando && this.idBeneficiarioEditando) {
      this.api.actualizarBeneficiario(
        this.idCuentaAhorro,
        this.idBeneficiarioEditando,
        this.nuevo
      ).subscribe({
        next: (res: any) => {
          if (res?.ok) {
            this.cancelarEdicion();
            this.cargar();
          } else {
            alert(res?.mensaje ?? 'No se pudo actualizar el beneficiario.');
          }
        },
        error: () => {
          alert('Error actualizando beneficiario.');
        }
      });

      return;
    }

    this.api.agregarBeneficiario(
      this.idCuentaAhorro,
      this.nuevo
    ).subscribe({
      next: (res: any) => {
        if (res?.ok) {
          this.nuevo = this.resetNuevo();
          this.errores = {};
          this.cargar();
        } else {
          alert(res?.mensaje ?? 'No se pudo agregar el beneficiario.');
        }
      },
      error: () => {
        alert('Error agregando beneficiario.');
      }
    });
  }

  limpiarError(campo: string): void {
    if (this.errores?.[campo]) {
      delete this.errores[campo];
    }
  }

  nombreParentesco(codigo: any): string {
    const valor = String(codigo ?? '').trim();

    const item = this.parentescos.find(p =>
      String(p.codigo ?? p.codigoParentesco ?? '').trim() === valor
    );

    return item?.nombre ?? item?.nombreParentesco ?? valor;
  }

  editar(item: BeneficiarioDTO): void {
    this.editando = true;
    this.idBeneficiarioEditando = item.idBeneficiario ?? null;

    this.nuevo = {
      idBeneficiario: item.idBeneficiario,
      idCuentaAhorro: item.idCuentaAhorro,
      documentoBeneficiario: item.documentoBeneficiario,
      nombreBeneficiario: item.nombreBeneficiario,
      telefonoBeneficiario: item.telefonoBeneficiario,
      celularBeneficiario: item.celularBeneficiario,
      tipoParentesco: String(item.tipoParentesco ?? '').trim()
    };

    this.errores = {};
  }

  cancelarEdicion(): void {
    this.editando = false;
    this.idBeneficiarioEditando = null;
    this.nuevo = this.resetNuevo();
    this.errores = {};
  }

  eliminar(i: number): void {
    if (!this.idCuentaAhorro) {
      return;
    }

    const item = this.beneficiarios[i];

    if (!item?.idBeneficiario) {
      alert('No se encontró el identificador del beneficiario.');
      return;
    }

    if (!confirm('¿Eliminar este beneficiario?')) {
      return;
    }

    this.api.eliminarBeneficiario(
      this.idCuentaAhorro,
      item.idBeneficiario
    ).subscribe({
      next: (res: any) => {
        if (res?.ok) {
          this.cargar();
        } else {
          alert(res?.mensaje ?? 'No se pudo eliminar el beneficiario.');
        }
      },
      error: () => {
        alert('Error eliminando beneficiario.');
      }
    });
  }

  regresar(): void {
    this.cerrar.emit();
  }

  codigoParentesco(p: any): string {
    return String(p.codigo ?? p.codigoParentesco ?? '').trim();
  }

}
