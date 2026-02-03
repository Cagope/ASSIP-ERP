import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SessionService } from '../../../core/auth/session.service';

import {
  FormArray,
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  FormControl
} from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

import { IngresoActivosApi } from './ingreso-activos.api';

import {
  PersonasApi,
  PersonaBusquedaDTO
} from '../../../shared/personas/personas.api';

import {
  TiposComprobantesApi,
  TipoComprobante
} from '../../contabilidad/tipos-comprobantes/tipos-comprobantes.api';

import { of } from 'rxjs';

import {
  CuentasApi,
  CuentaAutocompleteDTO
} from '../../../shared/cuentas/cuentas.api';

// ✅ combos items
import { EstadosActivoApi } from '../catalogos/estados-activo.api';
import { FormasDepreciacionApi } from '../catalogos/formas-depreciacion.api';
import { LocalizacionesApi, LocalizacionListDTO } from '../localizaciones/localizaciones.api';
import { BloquesApi, BloqueListDTO } from '../bloques/bloques.api';
import { NumericFormatDirective } from '../../../shared/utils/numeric-format.directive';
import { TiposAdquisicionApi } from '../catalogos/tipos-adquisicion.api';

// ✅ Plan de cuentas (para resolver ID -> codigo + nombre en vista previa)
import { PlanCuentasApi, PlanCuenta } from '../../contabilidad/plan-cuentas/plan-cuentas.api';

type PlanCuentaBusqueda = {
  id: number;
  codigoCuenta: string;
  nombre: string;
};


@Component({
  standalone: true,
  selector: 'app-ingreso-activos',
  imports: [CommonModule, ReactiveFormsModule, NumericFormatDirective],
  templateUrl: './ingreso-activos.component.html',
  styleUrls: ['./ingreso-activos.component.scss']
})
export class IngresoActivosComponent implements OnInit {

  // =========================================================
  // INYECCIONES
  // =========================================================
  private fb = inject(FormBuilder);
  private api = inject(IngresoActivosApi);
  private personasApi = inject(PersonasApi);
  private router = inject(Router);
  private tiposComprobantesApi = inject(TiposComprobantesApi);
  public session = inject(SessionService);
  private cuentasApi = inject(CuentasApi);

  private estadosActivoApi = inject(EstadosActivoApi);
  private formasDepreciacionApi = inject(FormasDepreciacionApi);
  private localizacionesApi = inject(LocalizacionesApi);
  private bloquesApi = inject(BloquesApi);
  private tiposAdquisicionApi = inject(TiposAdquisicionApi);

  private planCuentasApi = inject(PlanCuentasApi);
  private cuentasSeleccionadas = new Map<number, string>();

  // =========================================================
  // AUTOCOMPLETE — PROVEEDOR (HEADER)
  // =========================================================
  proveedorCtrl = new FormControl('');
  proveedores: PersonaBusquedaDTO[] = [];

  // =========================================================
  // AUTOCOMPLETE — RESPONSABLE (ITEM)
  // =========================================================
  responsableCtrl: FormControl[] = [];
  responsables: PersonaBusquedaDTO[][] = [];

  // =========================================================
  // TIPOS COMPROBANTES
  // =========================================================
  tiposComprobantes: TipoComprobante[] = [];
  consecutivoSugerido: string | null = null;

  // =========================================================
  // TIPOS ADQUISICIÓN
  // =========================================================
  tiposAdquisicion: any[] = [];

  // =========================================================
  // AUTOCOMPLETE — CUENTA CONTABLE FACTURA
  // =========================================================
  ctaFacturaCtrl = new FormControl<string>('', { nonNullable: true });
  cuentasFactura: CuentaAutocompleteDTO [] = [];


  // =========================================================
  // AUTOCOMPLETE — CUENTAS POR ITEM
  // =========================================================
  ctaActivoCtrl: FormControl[] = [];
  ctaDepreciacionCtrl: FormControl[] = [];
  ctaGastoCtrl: FormControl[] = [];
  ctaControlCtrl: FormControl[] = [];
  ctaIvaCtrl: FormControl[] = [];
  ctaRetencionCtrl: FormControl[] = [];

  cuentasActivo: CuentaAutocompleteDTO[][] = [];
  cuentasDepreciacion: CuentaAutocompleteDTO[][] = [];
  cuentasGasto: CuentaAutocompleteDTO[][] = [];
  cuentasControl: CuentaAutocompleteDTO[][] = [];
  cuentasIva: CuentaAutocompleteDTO[][] = [];
  cuentasRetencion: CuentaAutocompleteDTO[][] = [];

  // =========================================================
  // COMBOS ITEMS
  // =========================================================
  estadosActivo: any[] = [];
  formasDepreciacion: any[] = [];
  localizaciones: LocalizacionListDTO[] = [];
  bloques: BloqueListDTO[] = [];

  // =========================================================
  // PLAN DE CUENTAS (resolver labels en vista previa)
  // =========================================================
  catalogoCuentas: PlanCuenta[] = [];
  private cuentasById = new Map<number, string>(); // id -> "codigo — nombre"

  // =========================================================
  // FORM PRINCIPAL
  // =========================================================
  form = this.fb.group({

    header: this.fb.group({
      idAgencia: [null, Validators.required],
      fechaInclusion: [null, Validators.required],
      tipoComprobante: ['', Validators.required],
      numeroComprobante: [''],
      detalle: ['', [Validators.required, Validators.maxLength(100)]],
      idProveedor: [null, Validators.required],
      idCuentaFactura: [null, Validators.required],
      idTipoAdquisicion: [null, Validators.required] // ✅ ahora en encabezado
    }),

    items: this.fb.array([])
  });

  // =========================================================
  // CICLO DE VIDA
  // =========================================================
  ngOnInit(): void {

    const agencia = this.session.getAgenciaActiva();

    if (agencia?.idAgencia) {
      this.header.get('idAgencia')?.setValue(agencia.idAgencia);
      this.onChangeAgencia(agencia.idAgencia);
    }

    // combos
    this.estadosActivoApi.listar().subscribe(d => this.estadosActivo = d);
    this.formasDepreciacionApi.listar().subscribe(d => this.formasDepreciacion = d);
    this.bloquesApi.listar().subscribe(d => this.bloques = d);
    this.tiposAdquisicionApi.listar().subscribe(d => this.tiposAdquisicion = d);

    // cuando cambia tipo comprobante -> consecutivo sugerido
    this.header.get('tipoComprobante')?.valueChanges.subscribe(tipo => {

      if (!tipo) {
        this.consecutivoSugerido = null;
        this.header.get('numeroComprobante')?.reset();
        return;
      }

      const seleccionado = this.tiposComprobantes.find(t => t.tipoComprobante === tipo);

      if (!seleccionado) {
        this.consecutivoSugerido = null;
        return;
      }

      const siguiente = (seleccionado.cscComprobante ?? 0) + 1;
      this.consecutivoSugerido = siguiente.toString().padStart(10, '0');
      this.header.get('numeroComprobante')?.setValue(this.consecutivoSugerido);
    });

    // Autocomplete proveedor
    this.proveedorCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(valor => {
        if (!valor || valor.length < 3) {
          this.proveedores = [];
          return;
        }
        this.personasApi.buscar(valor).subscribe(res => this.proveedores = res);
      });

    // Autocomplete cuenta contable factura
    this.autocompleteCuentas(this.ctaFacturaCtrl, this.cuentasFactura);

    // ✅ si cambia el tipo de adquisición en header, lo empujo a los items existentes
    this.header.get('idTipoAdquisicion')?.valueChanges.subscribe(idTipo => {
      this.items.controls.forEach(g => g.get('idTipoAdquisicion')?.setValue(idTipo));
    });
  }

  // =========================================================
  // GETTERS
  // =========================================================
  get header(): FormGroup {
    return this.form.get('header') as FormGroup;
  }

  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  get headerValido(): boolean {
    return this.header.valid;
  }

  private toNumber(value: any): number {
    if (value == null) return 0;
    if (typeof value === 'number') return value;
    return Number(String(value).replace(/,/g, '')) || 0;
  }

  // =========================================================
  // TOTALES
  // =========================================================
  get totalItems(): number {
    return this.items.length;
  }

  get totalValorHistorico(): number {
    return this.items.controls.reduce((sum, g) => sum + this.toNumber(g.get('valorHistorico')?.value), 0);
  }

  get totalIva(): number {
    return this.items.controls.reduce((sum, g) => sum + this.toNumber(g.get('valorIva')?.value), 0);
  }

  get totalRetencion(): number {
    return this.items.controls.reduce((sum, g) => sum + this.toNumber(g.get('valorRetencion')?.value), 0);
  }

  get puedeGuardar(): boolean {
    return this.totalValorHistorico > 0 && this.form.valid;
  }

  get totalDebitoContable(): number {
    return this.lineasContables.reduce(
      (s, x) => s + (x.debito || 0),
      0
    );
  }

  get totalCreditoContable(): number {
    return this.lineasContables.reduce(
      (s, x) => s + (x.credito || 0),
      0
    );
  }

  // =========================================================
  // VISTA PREVIA CONTABLE
  // =========================================================
  get lineasContables(): { cuenta: string; debito: number; credito: number }[] {

    const lineas: { cuenta: string; debito: number; credito: number }[] = [];

    let totalDebito = 0;
    let totalCredito = 0;

    this.items.controls.forEach(g => {

      const vh = this.toNumber(g.get('valorHistorico')?.value);
      const iva = this.toNumber(g.get('valorIva')?.value);
      const ret = this.toNumber(g.get('valorRetencion')?.value);

      const ctaActivo = g.get('idCuentaActivo')?.value;
      const ctaIva = g.get('idCuentaIvaActivo')?.value;
      const ctaRet = g.get('idCuentaRetencion')?.value;

      if (vh > 0 && ctaActivo) {
        lineas.push({ cuenta: this.getCuentaLabel(ctaActivo), debito: vh, credito: 0 });
        totalDebito += vh;
      }

      if (iva > 0 && ctaIva) {
        lineas.push({ cuenta: this.getCuentaLabel(ctaIva), debito: iva, credito: 0 });
        totalDebito += iva;
      }

      if (ret > 0 && ctaRet) {
        lineas.push({ cuenta: this.getCuentaLabel(ctaRet), debito: 0, credito: ret });
        totalCredito += ret;
      }
    });

    // contrapartida factura
    const ctaFactura = this.header.get('idCuentaFactura')?.value;
    const neto = totalDebito - totalCredito;

    if (ctaFactura && neto !== 0) {
      lineas.push({
        cuenta: this.getCuentaLabel(ctaFactura),
        debito: neto < 0 ? Math.abs(neto) : 0,
        credito: neto > 0 ? neto : 0
      });
    }

    return lineas;
  }

  // =========================================================
  // RESOLVER CUENTA (ID -> "codigo — nombre")
  // =========================================================
  private getCuentaLabel(idCuenta: number | null | undefined): string {
    if (!idCuenta) return '';
    return this.cuentasSeleccionadas.get(idCuenta) ?? `Cuenta ${idCuenta}`;
  }

  private rebuildCuentasMap(): void {
    this.cuentasById.clear();
    this.catalogoCuentas.forEach(c => {
      if (c?.id != null) this.cuentasById.set(Number(c.id), `${c.codigoCuenta} — ${c.nombre}`);
    });
  }

  // =========================================================
  // ITEMS
  // =========================================================
  agregarItem(): void {

    // ✅ no deja agregar items si el encabezado no está OK
    if (!this.header.valid) {
      this.header.markAllAsTouched();
      return;
    }

    const index = this.items.length;

    const itemGroup = this.fb.group({
      placaActivo: ['', Validators.required],
      nombreActivo: ['', Validators.required],

      fechaGarantia: [null],

      idFormaDepreciacion: [null, Validators.required],
      mesesDepreciacion: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),

      valorHistorico: [0, Validators.required],
      valorIva: [0],
      valorRetencion: [0],

      idEstadoActivo: [null, Validators.required],
      idTipoAdquisicion: [null], // se setea desde header

      idUbicacion: [null, Validators.required],
      idBloque: [null],

      idResponsable: [null],
      idProveedor: [null],

      idCuentaActivo: [null, Validators.required],
      cuentaActivoLabel: [''],            // 👈 NUEVO (solo UI)

      idCuentaDepreciacion: [null, Validators.required],
      cuentaDepreciacionLabel: [''],

      idCuentaGasto: [null, Validators.required],
      cuentaGastoLabel: [''],

      idCuentaControl: [null, Validators.required],
      cuentaControlLabel: [''],

      idCuentaIvaActivo: [null],
      cuentaIvaLabel: [''],

      idCuentaRetencion: [null],
      cuentaRetencionLabel: ['']

    });

    // autollenar meses desde bloque
    itemGroup.get('idBloque')?.valueChanges.subscribe(idBloque => {
      if (!idBloque) return;

      const bloque = this.bloques.find(b => b.idBloque === Number(idBloque));
      if (bloque && bloque.mesesDepreciacionDefecto != null) {
        itemGroup.get('mesesDepreciacion')?.setValue(bloque.mesesDepreciacionDefecto, { emitEvent: false });
      }
    });

    // ✅ copiar tipo adquisición desde header al item
    const idTipoAdq = this.header.get('idTipoAdquisicion')?.value;
    if (idTipoAdq) itemGroup.get('idTipoAdquisicion')?.setValue(idTipoAdq);

    this.items.push(itemGroup);

    // autocomplete responsable
    const respCtrl = new FormControl('');
    this.responsableCtrl[index] = respCtrl;
    this.responsables[index] = [];

    respCtrl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(valor => {
        if (!valor || valor.length < 3) {
          this.responsables[index] = [];
          return;
        }
        this.personasApi.buscar(valor).subscribe(res => this.responsables[index] = res);
      });

    // autocomplete cuentas por item
    this.initCuentaAutocomplete(index, this.ctaActivoCtrl, this.cuentasActivo);
    this.initCuentaAutocomplete(index, this.ctaDepreciacionCtrl, this.cuentasDepreciacion);
    this.initCuentaAutocomplete(index, this.ctaGastoCtrl, this.cuentasGasto);
    this.initCuentaAutocomplete(index, this.ctaControlCtrl, this.cuentasControl);
    this.initCuentaAutocomplete(index, this.ctaIvaCtrl, this.cuentasIva);
    this.initCuentaAutocomplete(index, this.ctaRetencionCtrl, this.cuentasRetencion);
  }

  eliminarItem(index: number): void {
    this.items.removeAt(index);

    this.responsableCtrl.splice(index, 1);
    this.responsables.splice(index, 1);

    // ✅ limpiar también los arrays de cuentas por índice (evita corrimientos raros)
    this.ctaActivoCtrl.splice(index, 1);
    this.ctaDepreciacionCtrl.splice(index, 1);
    this.ctaGastoCtrl.splice(index, 1);
    this.ctaControlCtrl.splice(index, 1);
    this.ctaIvaCtrl.splice(index, 1);
    this.ctaRetencionCtrl.splice(index, 1);

    this.cuentasActivo.splice(index, 1);
    this.cuentasDepreciacion.splice(index, 1);
    this.cuentasGasto.splice(index, 1);
    this.cuentasControl.splice(index, 1);
    this.cuentasIva.splice(index, 1);
    this.cuentasRetencion.splice(index, 1);
  }

  // =========================================================
  // SELECCIONES AUTOCOMPLETE
  // =========================================================
  seleccionarProveedor(p: PersonaBusquedaDTO): void {
    this.header.get('idProveedor')?.setValue(p.idDatosPersonal);
    this.proveedorCtrl.setValue(p.nombreCompleto, { emitEvent: false });
    this.proveedores = [];
  }

  seleccionarResponsable(index: number, r: PersonaBusquedaDTO): void {
    const item = this.items.at(index) as FormGroup;
    item.get('idResponsable')?.setValue(r.idDatosPersonal);
    this.responsableCtrl[index].setValue(r.nombreCompleto, { emitEvent: false });
    this.responsables[index] = [];
  }

  seleccionarCuentaFactura(c: CuentaAutocompleteDTO): void {

    this.header.patchValue({
      idCuentaFactura: c.idCuenta
    });

    const label = `${c.codigoCuenta} — ${c.nombreCuenta}`;

    this.ctaFacturaCtrl.setValue(label, { emitEvent: false });

    // 👇 CLAVE: guardar para la vista previa
    this.cuentasSeleccionadas.set(c.idCuenta, label);

    this.cuentasFactura.length = 0;
  }

 seleccionarCuentaItem(
   index: number,
   campo: string,
   labelCampo: string,
   ctrls: FormControl[],
   listas: CuentaAutocompleteDTO[][],
   c: CuentaAutocompleteDTO
 ): void {

   const item = this.items.at(index) as FormGroup;

   const label = `${c.codigoCuenta} — ${c.nombreCuenta}`;

   item.get(campo)?.setValue(c.idCuenta);
   item.get(labelCampo)?.setValue(label);

   ctrls[index].setValue(label, { emitEvent: false });

   // 👇 CLAVE: guardar para vista previa
   this.cuentasSeleccionadas.set(c.idCuenta, label);

   listas[index].length = 0;
 }

  private initCuentaAutocomplete(
    index: number,
    ctrls: FormControl[],
    resultados: CuentaAutocompleteDTO[][]
  ): void {

    if (!ctrls[index]) ctrls[index] = new FormControl('');
    if (!resultados[index]) resultados[index] = [];

    ctrls[index].valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(texto => {

          const idAgencia = this.header.get('idAgencia')?.value;

          if (
            !idAgencia ||
            typeof texto !== 'string' ||
            texto.length < 2
          ) {
            return of([]);
          }

          // ✅ USO CORRECTO: AGENCIA + TEXTO
          return this.cuentasApi.buscar(idAgencia, texto);
        })
      )
      .subscribe(data => {
        resultados[index] = data;
      });
  }

  // =========================================================
  // GUARDAR
  // =========================================================
  guardar(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    const payload = {
      header: {
        idAgencia: raw.header.idAgencia!,
        fechaInclusion: raw.header.fechaInclusion!,
        tipoComprobante: raw.header.tipoComprobante!,
        numeroComprobante: raw.header.numeroComprobante || null,
        detalle: raw.header.detalle!,
        idProveedor: raw.header.idProveedor!,
        idCuentaFactura: raw.header.idCuentaFactura!,
        idTipoAdquisicion: raw.header.idTipoAdquisicion! // ✅ faltaba en payload
      },
      items: raw.items.map((it: any) => ({
        placaActivo: it.placaActivo!,
        nombreActivo: it.nombreActivo!,
        fechaGarantia: it.fechaGarantia || null,

        idFormaDepreciacion: it.idFormaDepreciacion!,
        mesesDepreciacion: it.mesesDepreciacion!,

        valorHistorico: it.valorHistorico!,
        valorIva: it.valorIva || 0,
        valorRetencion: it.valorRetencion || 0,

        idEstadoActivo: it.idEstadoActivo!,
        idTipoAdquisicion: it.idTipoAdquisicion!, // viene copiado desde header

        idUbicacion: it.idUbicacion!,
        idBloque: it.idBloque || null,

        idResponsable: it.idResponsable || null,
        idProveedor: it.idProveedor || null,

        idCuentaActivo: it.idCuentaActivo!,
        idCuentaDepreciacion: it.idCuentaDepreciacion!,
        idCuentaGasto: it.idCuentaGasto!,
        idCuentaControl: it.idCuentaControl!,
        idCuentaIvaActivo: it.idCuentaIvaActivo || null,
        idCuentaRetencion: it.idCuentaRetencion || null
      }))
    };

    this.api.ingresar(payload).subscribe({
      next: () => this.router.navigate(['/activos-fijos/activos'])
    });
  }

  cancelar(): void {
    this.router.navigate(['/activos-fijos/activos']);
  }

  // =========================================================
  // AGENCIA
  // =========================================================
  onChangeAgencia(idAgencia: number): void {

    const id = Number(idAgencia);

    if (!id) {
      this.tiposComprobantes = [];
      this.localizaciones = [];
      this.catalogoCuentas = [];
      this.cuentasById.clear();

      this.header.get('tipoComprobante')?.reset();
      this.header.get('numeroComprobante')?.reset();
      this.consecutivoSugerido = null;
      return;
    }

    // sincroniza agencia en el form
    this.header.get('idAgencia')?.setValue(id);

    // tipos de comprobante
    this.tiposComprobantesApi.listarPorAgencia(id).subscribe(d => {
      this.tiposComprobantes = d;
      this.header.get('tipoComprobante')?.reset();
      this.header.get('numeroComprobante')?.reset();
      this.consecutivoSugerido = null;
    });

    // localizaciones filtradas por agencia
    this.localizacionesApi.listar().subscribe(d => {
      this.localizaciones = d.filter(l => l.idAgencia === id);

      // limpiar ubicaciones ya seleccionadas
      this.items.controls.forEach(ctrl => ctrl.get('idUbicacion')?.reset());
    });

    // ✅ cargar catálogo de cuentas de la agencia (para vista previa)
    this.planCuentasApi.listar(id).subscribe(d => {
      this.catalogoCuentas = d || [];
      this.rebuildCuentasMap();
    });
  }

  // =========================================================
  // AUTOCOMPLETE CUENTAS (FACTURA)
  // =========================================================
  private autocompleteCuentas(
    ctrl: FormControl<string>,
    target: CuentaAutocompleteDTO[]
  ): void {

    ctrl.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(texto => {

        const idAgencia = this.header.get('idAgencia')?.value;

        if (
          !idAgencia ||
          typeof texto !== 'string' ||
          texto.length < 2
        ) {
          return of([]);
        }

        // ✅ CORRECTO: AGENCIA + TEXTO
        return this.cuentasApi.buscar(idAgencia, texto);
      })
    ).subscribe(data => {
      target.splice(0, target.length, ...data);
    });
  }
}
