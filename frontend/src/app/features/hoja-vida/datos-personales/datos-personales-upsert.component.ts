import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { DatosPersonalesApi, DatosPersonales } from './datos-personales.api';
import { CatalogosApi, CodigoNombreDTO, Departamento, Ciudad } from '../../../shared/catalogos/catalogos.api';
import { calcularDvNit } from '../../../shared/utils/calcular-dv-nit';
import { EventEmitter, Output, Input } from '@angular/core';


@Component({
  selector: 'app-datos-personales-upsert',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './datos-personales-upsert.component.html',
  styleUrls: ['./datos-personales-upsert.component.scss']
})
export class DatosPersonalesUpsertComponent implements OnInit {
  @Output() formularioValido = new EventEmitter<boolean>();
  @Output() registroCreado = new EventEmitter<number>();  // 👈 NUEVO
  @Input() enWizard = false;

  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(DatosPersonalesApi);
  private readonly catalogos = inject(CatalogosApi);


  form!: FormGroup;
  editando = false;

  // === 📚 Catálogos ===
  tiposDocumento: CodigoNombreDTO[] = [];
  generos: CodigoNombreDTO[] = [];
  estadosCiviles: CodigoNombreDTO[] = [];
  nivelesEscolares: CodigoNombreDTO[] = [];
  tiposVivienda: CodigoNombreDTO[] = [];
  ocupaciones: CodigoNombreDTO[] = [];
  sectoresEconomicos: CodigoNombreDTO[] = [];
  actividadesSes: CodigoNombreDTO[] = [];
  actividadesDian: CodigoNombreDTO[] = [];

  // === 🌎 Geográficos ===
  paises: CodigoNombreDTO[] = [];
  departamentos: Departamento[] = [];
  ciudadesExpedicion: Ciudad[] = [];
  ciudadesNacimiento: Ciudad[] = [];
  filtroSes = '';
  filtroDian = '';
  actividadesSesFiltradas: CodigoNombreDTO[] = [];
  actividadesDianFiltradas: CodigoNombreDTO[] = [];

  ngOnInit(): void {
    this.crearFormulario();
    this.cargarCatalogos();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editando = true;
      this.api.obtener(+id).subscribe({
        next: (data) => {
          this.form.patchValue(data);
          this.form.get('idDatosPersonal')?.setValue(data.idDatosPersonal);

          // ✅ Sincroniza el valor de SES
          if (data.codigoActividadSes && this.actividadesSes.length > 0) {
            const ses = this.actividadesSes.find(a => a.codigo === data.codigoActividadSes);
            if (ses) {
              this.filtroSes = `${ses.codigo} - ${ses.nombre}`;
            }
          }

          // ✅ Sincroniza el valor de DIAN
          if (data.codigoActividadDian && this.actividadesDian.length > 0) {
            const dian = this.actividadesDian.find(a => a.codigo === data.codigoActividadDian);
            if (dian) {
              this.filtroDian = `${dian.codigo} - ${dian.nombre}`;
            }
          }
        },
        error: (err) => console.error('Error al cargar datos personales:', err)
      });
    }

    // === 🔁 Ciudades dependientes ===
    this.form.get('idDepartamentoExpedicion')?.valueChanges.subscribe((idDepto) => {
      if (idDepto) {
        this.catalogos.listarCiudadesPorDepartamento(idDepto).subscribe({
          next: (data) => (this.ciudadesExpedicion = data),
          error: () => (this.ciudadesExpedicion = [])
        });
      } else {
        this.ciudadesExpedicion = [];
      }
    });

    this.form.get('idDepartamentoNacimiento')?.valueChanges.subscribe((idDepto) => {
      if (idDepto) {
        this.catalogos.listarCiudadesPorDepartamento(idDepto).subscribe({
          next: (data) => (this.ciudadesNacimiento = data),
          error: () => (this.ciudadesNacimiento = [])
        });
      } else {
        this.ciudadesNacimiento = [];
      }
    });

    // === 🧮 Cálculo automático del DV ===
    const docCtrl = this.form.get('documento');
    const tipoCtrl = this.form.get('tipoDocumento');
    const rutCtrl = this.form.get('tieneRut');

    docCtrl?.valueChanges.subscribe(() => this.actualizarDv());
    tipoCtrl?.valueChanges.subscribe(() => this.actualizarDv());
    rutCtrl?.valueChanges.subscribe(() => this.actualizarDv());

    // === 🧠 Si género es masculino, marcar "No" en cabezaFamilia ===
    this.form.get('codigoGenero')?.valueChanges.subscribe((valor) => {
      const genero = (valor || '').toString().toLowerCase();
      if (genero.startsWith('m')) {
        this.form.get('cabezaFamilia')?.setValue('0');
      }
    });

    // === Inicializar filtros ===
    this.actividadesSesFiltradas = [...this.actividadesSes];
    this.actividadesDianFiltradas = [...this.actividadesDian];

    this.form.statusChanges.subscribe(status => {
      this.formularioValido.emit(status === 'VALID');
    });

  }

  // ======================================================
  // 🧱 FORMULARIO BASE
  // ======================================================
  private crearFormulario(): void {
    const fechaActual = new Date().toISOString().split('T')[0]; // formato YYYY-MM-DD

    this.form = this.fb.group({
      // 🧾 Identificación
      idDatosPersonal: [null],
      tipoDocumento: ['', Validators.required],
      documento: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      tipoPersona: [{ value: '1', disabled: true }], // fijo, sin opción de edición
      tieneRut: [false],
      digitoVerificacion: [''],
      fechaDocumento: ['', Validators.required],
      idPaisDocumento: [null, Validators.required],
      idDepartamentoExpedicion: [null, Validators.required],
      idCiudadExpedicion: [null, Validators.required],

      // 👤 Nombres y nacimiento
      nombres: ['', Validators.required],
      primerApellido: ['', Validators.required],
      segundoApellido: ['', Validators.required],
      fechaNacimiento: ['', Validators.required],
      idPaisNacimiento: [null, Validators.required],
      idDepartamentoNacimiento: [null, Validators.required],
      idCiudadNacimiento: [null, Validators.required],
      fechaApertura: [{ value: fechaActual, disabled: true }],
      fechaActualizacion: [{ value: fechaActual, disabled: true }],

      // 🌍 Demográficos
      codigoGenero: ['', Validators.required],
      codigoEstadoCivil: ['', Validators.required],
      codigoEscolaridad: ['', Validators.required],
      cabezaFamilia: ['0', Validators.required],
      estratoSocial: [null, [Validators.required, Validators.min(0), Validators.max(6)]],
      codigoTipoVivienda: ['', Validators.required],
      numeroHijos: [null, [Validators.required, Validators.min(0), Validators.max(99)]],

      // 💼 Económicos
      codigoOcupacion: ['', Validators.required],
      codigoSectorEconomico: ['', Validators.required],
      codigoActividadSes: ['', Validators.required],
      codigoActividadDian: ['', Validators.required],

      // 📝 Comentario
      comentario: ['', [Validators.required, Validators.minLength(5)]],

      // ⚙️ Auditoría
      fkSeguridadCreacion: [1],
      fkSeguridadEdicion: [1],
    });
    this.form.statusChanges.subscribe(status => {
      this.formularioValido.emit(status === 'VALID');
    });
  }

  // ======================================================
  // 📦 CARGA DE CATÁLOGOS
  // ======================================================
  private cargarCatalogos(): void {
    forkJoin({
      tiposDocumento: this.catalogos.listarTiposDocumentos(),
      generos: this.catalogos.listarGeneros(),
      estadosCiviles: this.catalogos.listarEstadosCiviles(),
      nivelesEscolares: this.catalogos.listarNivelesEscolares(),
      tiposVivienda: this.catalogos.listarTiposViviendas(),
      ocupaciones: this.catalogos.listarOcupaciones(),
      sectoresEconomicos: this.catalogos.listarSectoresEconomicos(),
      actividadesSes: this.catalogos.listarActividadesSes(),
      actividadesDian: this.catalogos.listarActividadesDian(),
      paises: this.catalogos.listarPaises(),
      departamentos: this.catalogos.listarDepartamentos()
    }).subscribe({
      next: (res) => {
        this.tiposDocumento = res.tiposDocumento;
        this.generos = res.generos;
        this.estadosCiviles = res.estadosCiviles;
        this.nivelesEscolares = res.nivelesEscolares;
        this.tiposVivienda = res.tiposVivienda;
        this.ocupaciones = res.ocupaciones;
        this.sectoresEconomicos = res.sectoresEconomicos;
        this.actividadesSes = res.actividadesSes;
        this.actividadesDian = res.actividadesDian;
        this.paises = res.paises;
        this.departamentos = res.departamentos;

        // ✅ Si estamos editando, sincronizar selección visible SES/DIAN
        if (this.editando) {
          const data = this.form.getRawValue();

          if (data.codigoActividadSes) {
            const ses = this.actividadesSes.find(a => a.codigo === data.codigoActividadSes);
            if (ses) this.filtroSes = `${ses.codigo} - ${ses.nombre}`;
          }

          if (data.codigoActividadDian) {
            const dian = this.actividadesDian.find(a => a.codigo === data.codigoActividadDian);
            if (dian) this.filtroDian = `${dian.codigo} - ${dian.nombre}`;
          }
        }
      },
      error: (err) => console.error('Error cargando catálogos', err)
    });
  }


  // ======================================================
  // 🔍 FILTRO DE ACTIVIDADES ECONÓMICAS
  // ======================================================
  filtrarSes(): void {
    const q = this.filtroSes.trim().toLowerCase();
    this.actividadesSesFiltradas = this.actividadesSes.filter(a =>
      a.nombre.toLowerCase().includes(q)
    );
  }

  filtrarDian(): void {
    const q = this.filtroDian.trim().toLowerCase();
    this.actividadesDianFiltradas = this.actividadesDian.filter(a =>
      a.nombre.toLowerCase().includes(q)
    );
  }

  // 🟢 Selección de actividad SES
  seleccionarActividadSes(item: CodigoNombreDTO): void {
    this.form.get('codigoActividadSes')?.setValue(item.codigo);
    this.filtroSes = `${item.codigo} - ${item.nombre}`;
    this.actividadesSesFiltradas = []; // Cierra la lista al seleccionar
  }

  // 🔵 Selección de actividad DIAN
  seleccionarActividadDian(item: CodigoNombreDTO): void {
    this.form.get('codigoActividadDian')?.setValue(item.codigo);
    this.filtroDian = `${item.codigo} - ${item.nombre}`;
    this.actividadesDianFiltradas = []; // Cierra la lista al seleccionar
  }

  // ======================================================
  // 🧮 CÁLCULO AUTOMÁTICO DEL DV
  // ======================================================
  private actualizarDv(): void {
    const tieneRut = this.form.get('tieneRut')?.value;
    const documento = this.form.get('documento')?.value ?? '';
    const dvCtrl = this.form.get('digitoVerificacion');

    // Si no hay documento o no es numérico → limpiar DV
    if (!documento || !/^\d+$/.test(documento)) {
      dvCtrl?.setValue('');
      dvCtrl?.disable({ emitEvent: false });
      return;
    }

    // Si tiene RUT activo → calcular automáticamente
    if (tieneRut) {
      const dv = calcularDvNit(documento);
      if (dv !== null) {
        dvCtrl?.setValue(dv);
      } else {
        dvCtrl?.setValue('');
      }
      dvCtrl?.disable({ emitEvent: false }); // siempre bloqueado
    } else {
      // Si NO tiene RUT → limpiar DV y mantenerlo bloqueado
      dvCtrl?.setValue('');
      dvCtrl?.disable({ emitEvent: false });
    }
  }

  // ======================================================
  // 💾 GUARDAR / VALIDACIÓN FINAL
  // ======================================================
  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('⚠️ Complete todos los campos obligatorios.');
      return;
    }

    const fNac = this.form.get('fechaNacimiento')?.value;
    const fDoc = this.form.get('fechaDocumento')?.value;
    const fApe = this.form.get('fechaApertura')?.value;
    const fAct = this.form.get('fechaActualizacion')?.value;

    // Convertir a fechas reales
    const fechaNac = fNac ? new Date(fNac) : null;
    const fechaDoc = fDoc ? new Date(fDoc) : null;
    const fechaApe = fApe ? new Date(fApe) : null;
    const fechaAct = fAct ? new Date(fAct) : null;

    // === 📅 Validaciones de coherencia cronológica ===

    if (fechaNac && fechaDoc && fechaDoc < fechaNac) {
      alert('⚠️ La fecha del documento no puede ser anterior a la fecha de nacimiento.');
      return;
    }

    if (fechaApe && fechaNac && fechaApe < fechaNac) {
      alert('⚠️ La fecha de apertura no puede ser anterior a la fecha de nacimiento.');
      return;
    }

    if (fechaApe && fechaDoc && fechaApe < fechaDoc) {
      alert('⚠️ La fecha de apertura no puede ser anterior a la fecha del documento.');
      return;
    }

    if (fechaAct && fechaApe && fechaAct < fechaApe) {
      alert('⚠️ La fecha de última actualización no puede ser anterior a la fecha de apertura.');
      return;
    }

    // Si todo está correcto
    const datos: DatosPersonales = this.form.getRawValue();
    if (!datos.tieneRut) {
      datos.digitoVerificacion = ''; // Limpio y coherente
    }
    const id = this.form.get('idDatosPersonal')?.value;

    const accion = this.editando && id
      ? this.api.actualizar(id, datos)
      : this.api.crear(datos);

    accion.subscribe({
      next: (res) => {
        const nuevoId = res.idDatosPersonal;
        if (nuevoId) {
          this.registroCreado.emit(nuevoId); // 🔗 Notifica al wizard
        }

        alert('✅ Registro guardado correctamente.');

        // 🧭 Solo redirige si NO está en el wizard
        if (!this.enWizard) {
          this.router.navigate(['/hoja-vida/datos-personales']);
        }
      },
      error: (err) => console.error('Error al guardar', err)
    });
  }

  volver(): void {
    this.router.navigate(['/hoja-vida/datos-personales']);
  }
}
