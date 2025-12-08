import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { HeaderActionsComponent } from '../../../shared/header-actions/header-actions.component';
import { DatosPersonalesApi, DatosPersonales } from '../datos-personales/datos-personales.api';
import { AperturaCuentasApi } from './apertura-cuentas.api';

@Component({
  selector: 'app-apertura-cuentas-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderActionsComponent],
  templateUrl: './apertura-cuentas-list.component.html',
  styleUrls: ['./apertura-cuentas-list.component.scss']
})
export class AperturaCuentasListComponent implements OnInit {

  private readonly dpApi = inject(DatosPersonalesApi);
  private readonly api = inject(AperturaCuentasApi);
  private readonly router = inject(Router);

  personas: DatosPersonales[] = [];
  filtradas: DatosPersonales[] = [];

  cargando = false;
  filtro = '';
  error = '';

  pagina = 1;
  tamanoPagina = 10;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    this.dpApi.listar().subscribe({
      next: lista => {
        // ✅ Asignar la lista y ordenar por fechaActualizacion (más reciente primero)
        this.personas = (lista ?? []).sort((a, b) => {
          const fa = a.fechaActualizacion ? Date.parse(a.fechaActualizacion) : 0;
          const fb = b.fechaActualizacion ? Date.parse(b.fechaActualizacion) : 0;
          return fb - fa;
        });

        this.filtrar();
      },
      error: err => {
        console.error('❌ Error cargando datos personales:', err);
        this.error = 'Error cargando datos.';
      },
      complete: () => (this.cargando = false)
    });
  }

  filtrar(): void {
    const term = this.filtro.toLowerCase().trim();
    this.filtradas = !term
      ? this.personas
      : this.personas.filter(p =>
          `${p.documento} ${p.nombres} ${p.primerApellido} ${p.segundoApellido ?? ''}`
            .toLowerCase()
            .includes(term)
        );
    this.pagina = 1;
  }

  get paginadas(): DatosPersonales[] {
    const inicio = (this.pagina - 1) * this.tamanoPagina;
    return this.filtradas.slice(inicio, inicio + this.tamanoPagina);
  }

  totalPaginas(): number {
    return Math.ceil(this.filtradas.length / this.tamanoPagina);
  }

  cambiarPagina(p: number): void {
    if (p < 1 || p > this.totalPaginas()) return;
    this.pagina = p;
  }

  // ============================================================
  // 🔹 Gestionar: valida con backend y muestra motivo claro
  // ============================================================
  gestionar(persona: DatosPersonales): void {
    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error = 'Asociado inválido: no tiene idDatosPersonal definido.';
      return;
    }

    this.api.listarFormas(persona.idDatosPersonal).subscribe({
      next: formas => {
        if (!formas || formas.length === 0) {
          // ✅ Mensaje más claro y explicativo
          this.error =
            'No es posible abrir cuentas para este asociado. ' +
            'Posibles causas: ya tiene saldos activos en cuentas de otra agencia, ' +
            'no pertenece a ninguna de las agencias asignadas a su usuario, ' +
            'o su usuario no tiene agencias configuradas correctamente.';
          return;
        }

        this.router.navigate(
          ['/hoja-vida/apertura-cuentas/nuevo'],
          { queryParams: { idDatosPersonal: persona.idDatosPersonal } }
        );
      },
      error: err => {
        console.error('❌ Error validando reglas:', err);
        this.error = 'No se pudo validar la información del asociado para apertura de cuentas.';
      }
    });
  }
}
