import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output
} from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  LavadoActivosModalApi,
  LavadoActivosRequest,
  LavadoActivosResponse
} from './lavado-activos-modal.api';

import {
  CatalogosApi,
  CodigoNombreDTO,
  Departamento,
  Ciudad
} from '../catalogos/catalogos.api';

import { LavadoActivosPrintComponent } from './lavado-activos-print.component';

@Component({
  selector: 'app-lavado-activos-modal',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    LavadoActivosPrintComponent
  ],
  templateUrl: './lavado-activos-modal.component.html',
  styleUrl: './lavado-activos-modal.component.scss'
})
export class LavadoActivosModalComponent implements OnInit, OnChanges {

  @Input() visible = false;
  @Input() datosBase: LavadoActivosRequest | null = null;

  @Output() cerrar = new EventEmitter<void>();
  @Output() guardado = new EventEmitter<LavadoActivosResponse>();

  cargando = false;
  error = '';
  mensaje = '';
  mostrarPrint = false;
  idFormatoPrint: number | null = null;
  mostrarCaptura = true;

  tiposDocumento: CodigoNombreDTO[] = [];
  departamentos: Departamento[] = [];
  ciudadesRealiza: Ciudad[] = [];

  form: LavadoActivosRequest = this.nuevoForm();

  constructor(
    private api: LavadoActivosModalApi,
    private catalogos: CatalogosApi
  ) {}

  ngOnInit(): void {
    this.cargarCatalogos();
  }

  ngOnChanges(): void {
    if (this.visible && this.datosBase) {
      this.error = '';
      this.mensaje = '';
      this.mostrarCaptura = true;

      this.form = {
        ...this.nuevoForm(),
        ...this.datosBase,
        documentoRealiza: this.datosBase.documento,
        nombreBeneficiario: this.datosBase.nombreCompleto
      };

      this.autocompletarPersonaRealiza();
      this.autocompletarBeneficiario();
    }
  }

  cargarCatalogos(): void {
    this.catalogos.listarTiposDocumentos().subscribe({
      next: data => this.tiposDocumento = data || [],
      error: () => this.tiposDocumento = []
    });

    this.catalogos.listarDepartamentos().subscribe({
      next: data => this.departamentos = data || [],
      error: () => this.departamentos = []
    });
  }

  cargarCiudadesRealiza(): void {
    this.form.ciudadRealiza = '';

    if (!this.form.departamentoRealiza) {
      this.ciudadesRealiza = [];
      return;
    }

    this.catalogos
      .listarCiudadesPorDepartamento(Number(this.form.departamentoRealiza))
      .subscribe({
        next: data => this.ciudadesRealiza = data || [],
        error: () => this.ciudadesRealiza = []
      });
  }

  autocompletarPersonaRealiza(): void {
    this.error = '';

    const documento = this.form.documentoRealiza?.trim();

    if (!documento) {
      return;
    }

    this.api.buscarPersona(documento).subscribe({
      next: persona => {
        if (!persona) {
          return;
        }

        this.form.tipoDocumentoRealiza = persona.tipoDocumento || '';
        this.form.primerApellidoRealiza = persona.primerApellido || '';
        this.form.segundoApellidoRealiza = persona.segundoApellido || '';
        this.form.primerNombreRealiza = persona.primerNombre || '';
        this.form.segundoNombreRealiza = persona.segundoNombre || '';
        this.form.direccionRealiza = persona.direccion || '';
        this.form.telefonoRealiza = persona.telefono || '';

        if (!this.form.nombreBeneficiario?.trim()) {
          this.form.nombreBeneficiario =
            this.form.nombreCompleto || '';
        }

        if (!this.form.direccionBeneficiario?.trim()) {
          this.form.direccionBeneficiario =
            this.form.direccionRealiza || '';
        }

        if (!this.form.telefonoBeneficiario?.trim()) {
          this.form.telefonoBeneficiario =
            this.form.telefonoRealiza || '';
        }

        this.form.departamentoRealiza = persona.idDepartamentoExpedicion
          ? String(persona.idDepartamentoExpedicion)
          : '';

        const ciudadRealiza = persona.idCiudadExpedicion
          ? String(persona.idCiudadExpedicion)
          : '';

        if (this.form.departamentoRealiza) {
          this.catalogos
            .listarCiudadesPorDepartamento(Number(this.form.departamentoRealiza))
            .subscribe({
              next: data => {
                this.ciudadesRealiza = data || [];
                this.form.ciudadRealiza = ciudadRealiza;
              },
              error: () => {
                this.ciudadesRealiza = [];
                this.form.ciudadRealiza = '';
              }
            });
        } else {
          this.ciudadesRealiza = [];
          this.form.ciudadRealiza = '';
        }
      },
      error: () => {
        this.error =
          'No se encontró el documento en hoja de vida. Complete los datos manualmente.';
      }
    });
  }

  autocompletarBeneficiario(): void {
    if (!this.form.nombreBeneficiario) {
      this.form.nombreBeneficiario = this.form.nombreCompleto;
    }

    if (!this.form.direccionBeneficiario && this.form.direccionRealiza) {
      this.form.direccionBeneficiario = this.form.direccionRealiza;
    }

    if (!this.form.telefonoBeneficiario && this.form.telefonoRealiza) {
      this.form.telefonoBeneficiario = this.form.telefonoRealiza;
    }
  }

  guardar(): void {
    this.error = '';
    this.mensaje = '';

    if (!this.validar()) {
      return;
    }

    this.cargando = true;

    this.api.generar(this.form).subscribe({
      next: data => {
        this.mensaje = data.mensaje;
        this.cargando = false;

        this.idFormatoPrint =
          data.idFormatoLavadoActivos || null;

        this.mostrarPrint =
          !!this.idFormatoPrint;

        if (this.mostrarPrint) {
          this.mostrarCaptura = false;
        }

        // No emitir todavía, para no cerrar el modal antes de imprimir.
        // this.guardado.emit(data);
      },
      error: err => {
        this.error =
          err?.error?.message
          || 'No fue posible guardar el formato de lavado de activos.';
        this.cargando = false;
      }
    });
  }

  imprimir(id?: number): void {
    if (!id) {
      window.print();
      return;
    }

    this.api.marcarImpreso(id).subscribe({
      next: () => window.print(),
      error: () => window.print()
    });
  }

  cerrarModal(): void {
    this.cerrar.emit();
  }

  private validar(): boolean {
    if (!this.form.actividadEconomica?.trim()) {
      this.error = 'El origen de fondos es obligatorio.';
      return false;
    }

    if (!this.form.tipoDocumentoRealiza?.trim()) {
      this.error = 'El tipo de documento de quien realiza la operación es obligatorio.';
      return false;
    }

    if (!this.form.documentoRealiza?.trim()) {
      this.error = 'El documento de quien realiza la operación es obligatorio.';
      return false;
    }

    if (!this.form.departamentoRealiza?.trim()) {
      this.error = 'El departamento del documento es obligatorio.';
      return false;
    }

    if (!this.form.ciudadRealiza?.trim()) {
      this.error = 'La ciudad del documento es obligatoria.';
      return false;
    }

    if (!this.form.primerApellidoRealiza?.trim()) {
      this.error = 'El primer apellido de quien realiza la operación es obligatorio.';
      return false;
    }

    if (!this.form.primerNombreRealiza?.trim()) {
      this.error = 'El primer nombre de quien realiza la operación es obligatorio.';
      return false;
    }

    if (!this.form.direccionRealiza?.trim()) {
      this.error = 'La dirección de quien realiza la operación es obligatoria.';
      return false;
    }

    if (!this.form.telefonoRealiza?.trim()) {
      this.error = 'El teléfono de quien realiza la operación es obligatorio.';
      return false;
    }

    if (!this.form.nombreBeneficiario?.trim()) {
      this.error = 'El nombre de la persona en nombre de la cual se realiza la operación es obligatorio.';
      return false;
    }

    return true;
  }

  private nuevoForm(): LavadoActivosRequest {
    return {
      modulo: '',
      proceso: '',
      fechaTransaccion: '',
      fechaContable: '',
      tipoTransaccion: '',
      valorTransaccion: 0,
      idAgencia: 0,
      documento: '',
      nombreCompleto: ''
    };
  }

  cerrarPrint(): void {
    this.mostrarPrint = false;
    this.finalizarFormato();
  }

  finalizarFormato(): void {
    if (!this.idFormatoPrint) {
      return;
    }

    this.guardado.emit({
      idFormatoLavadoActivos: this.idFormatoPrint,
      generado: true,
      requiereFormato: true,
      mensaje: this.mensaje || 'Formato de lavado de activos guardado correctamente.'
    });
  }

}
