import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// ✅ Subformularios (pasos)
import { DatosPersonalesUpsertComponent } from '../datos-personales/datos-personales-upsert.component';
import { UbicacionesUpsertComponent } from '../ubicaciones/ubicaciones-upsert.component';
import { LaboralesUpsertComponent } from '../laborales/laborales-upsert.component';
import { FinancierosUpsertComponent } from '../financieros/financieros-upsert.component';
import { DatosFamiliaresUpsertComponent } from '../datos-familiares/datos-familiares-upsert.component';
import { ReferenciasPersonalesUpsertComponent } from '../referencias-personales/referencias-personales-upsert.component';
import { SarlaftUpsertComponent } from '../sarlaft/sarlaft-upsert.component';
import { PermisosEspecialesUpsertComponent } from '../permisos-especiales/permisos-especiales-upsert.component';
import { ViewChild } from '@angular/core';

@Component({
  selector: 'app-formulario-integral-wizard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DatosPersonalesUpsertComponent,
    UbicacionesUpsertComponent,
    LaboralesUpsertComponent,
    FinancierosUpsertComponent,
    DatosFamiliaresUpsertComponent,
    ReferenciasPersonalesUpsertComponent,
    SarlaftUpsertComponent,
    PermisosEspecialesUpsertComponent
  ],
  templateUrl: './formulario-integral-wizard.component.html',
  styleUrls: ['./formulario-integral-wizard.component.scss']
})
export class FormularioIntegralWizardComponent {
  @ViewChild(UbicacionesUpsertComponent) formUbicaciones!: UbicacionesUpsertComponent;
  @ViewChild(LaboralesUpsertComponent) formLaborales!: LaboralesUpsertComponent;
  @ViewChild(FinancierosUpsertComponent) formFinancieros!: FinancierosUpsertComponent;
  @ViewChild(DatosFamiliaresUpsertComponent) formDatosFamiliares!: DatosFamiliaresUpsertComponent;
  @ViewChild(ReferenciasPersonalesUpsertComponent) formReferenciasPersonales!: ReferenciasPersonalesUpsertComponent;
  @ViewChild(SarlaftUpsertComponent) formSarlaft!: SarlaftUpsertComponent;
  @ViewChild(PermisosEspecialesUpsertComponent) formPermisosEspeciales!: PermisosEspecialesUpsertComponent;


  /** Paso actual */
  pasoActual = 1;

  /** 🔗 ID principal creado en paso 1 */
  idDatosPersonalCreado?: number;

  /** Estado de validez por paso */
  formValidoPorPaso: boolean[] = Array(8).fill(false);

  /** Lista de pasos */
  pasos = [
    'Datos Personales',
    'Ubicaciones',
    'Información Laboral',
    'Datos Económicos',
    'Datos Familiares',
    'Referencias Personales',
    'SARLAFT',
    'Permisos Especiales'
  ];

  // ================================================================
  // 🔹 Recepción del ID creado en el paso 1
  // ================================================================
  onRegistroCreado(id: number): void {
    this.idDatosPersonalCreado = id;
    console.log('🟢 ID de DatosPersonales creado y recibido en wizard:', id);

    // ✅ Marcar paso 1 como completo y avanzar automáticamente al paso 2
    this.formValidoPorPaso[0] = true;
    this.pasoActual = 2;
  }

  // ================================================================
  // 🔹 Actualización de estado de validez de cada paso
  // ================================================================
  actualizarEstadoPaso(paso: number, esValido: boolean): void {
    this.formValidoPorPaso[paso - 1] = esValido;

    console.log(`📡 Paso ${paso}: formulario válido =`, esValido);
    console.table(this.formValidoPorPaso);
  }

  // ================================================================
  // 🔹 Navegación entre pasos
  // ================================================================
  siguiente(): void {
    if (this.pasoActual < this.pasos.length) {
      if (this.formValidoPorPaso[this.pasoActual - 1]) {
        this.pasoActual++;
      } else {
        alert('⚠️ Complete correctamente el formulario antes de continuar.');
      }
    }
  }

  anterior(): void {
    if (this.pasoActual > 1) {
      this.pasoActual--;
    }
  }

  irA(paso: number): void {
    if (paso <= this.pasos.length) {
      this.pasoActual = paso;
    }
  }

  // ================================================================
  // 🔹 Validación global
  // ================================================================
  todosValidos(): boolean {
    return this.formValidoPorPaso.every(v => v === true);
  }

  // ================================================================
  // 🔹 Finalizar
  // ================================================================
  async finalizar(): Promise<void> {
    if (!this.todosValidos()) {
      alert('⚠️ Aún hay pasos incompletos o inválidos.');
      console.warn('❌ Faltan pasos válidos:', this.formValidoPorPaso);
      return;
    }

    try {
      console.log('💾 Iniciando guardado integral de hoja de vida...');

      // 🟢 Guardar en secuencia (sin alterar navegación)
      await this.formUbicaciones?.guardar();
      await this.formLaborales?.guardar();
      await this.formFinancieros?.guardar();
      await this.formDatosFamiliares?.guardar();
      await this.formReferenciasPersonales?.guardar();
      await this.formSarlaft?.guardar();
      await this.formPermisosEspeciales?.guardar();

      alert('✅ Todos los registros fueron guardados correctamente.');
      console.log('🎯 Proceso integral completado: todas las tablas sincronizadas.');
    } catch (error) {
      console.error('❌ Error durante el guardado integral:', error);
      alert('⚠️ Ocurrió un error al guardar algunos registros. Revise la consola.');
    }
  }

}
