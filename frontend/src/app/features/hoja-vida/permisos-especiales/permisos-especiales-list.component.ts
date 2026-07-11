import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';

import {
  HeaderActionsComponent
} from '../../../shared/header-actions/header-actions.component';

import {
  DatosPersonalesApi,
  DatosPersonales
} from '../datos-personales/datos-personales.api';

import {
  PermisosEspecialesApi
} from './permisos-especiales.api';

import {
  PermisoEspecial
} from '../../../shared/models/permisos-especiales.model';

import {
  PermisosEspecialesPrintService
} from './permisos-especiales-print.service';

import {
  PermisosEspecialesExporterService
} from './permisos-especiales-exporter.service';

type PersonaConPermiso =
  DatosPersonales & {
    permisos: PermisoEspecial | null;
  };

type PermisoEspecialInforme =
  PermisoEspecial & {
    documento?: string;
    nombrePersona?: string;
  };

@Component({
  selector: 'app-permisos-especiales-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    HeaderActionsComponent
  ],
  templateUrl: './permisos-especiales-list.component.html',
  styleUrls: ['./permisos-especiales-list.component.scss']
})
export class PermisosEspecialesListComponent
  implements OnInit {

  private readonly dpApi =
    inject(DatosPersonalesApi);

  private readonly permisosApi =
    inject(PermisosEspecialesApi);

  private readonly router =
    inject(Router);

  private readonly printService =
    inject(PermisosEspecialesPrintService);

  private readonly exporter =
    inject(PermisosEspecialesExporterService);

  personas: PersonaConPermiso[] = [];
  filtradas: PersonaConPermiso[] = [];

  cargando = false;
  error = '';

  filtros = {
    documento: '',
    nombres: '',
    primerApellido: '',
    segundoApellido: ''
  };

  pagina = 1;
  tamanoPagina = 10;

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando = true;
    this.error = '';

    forkJoin({
      personas: this.dpApi.listar(),
      permisos: this.permisosApi.listar()
    })
      .pipe(
        finalize(() => {
          this.cargando = false;
        })
      )
      .subscribe({
        next: resultado => {
          const mapaPermisos =
            new Map<number, PermisoEspecial>();

          (resultado.permisos ?? []).forEach(
            permiso => {
              if (permiso.idDatosPersonal == null) {
                return;
              }

              mapaPermisos.set(
                permiso.idDatosPersonal,
                permiso
              );
            }
          );

          this.personas = (resultado.personas ?? [])
            .sort((a, b) => {
              const fechaA = a.fechaActualizacion
                ? Date.parse(a.fechaActualizacion)
                : 0;

              const fechaB = b.fechaActualizacion
                ? Date.parse(b.fechaActualizacion)
                : 0;

              return fechaB - fechaA;
            })
            .map(persona => ({
              ...persona,
              permisos:
                persona.idDatosPersonal != null
                  ? mapaPermisos.get(
                      persona.idDatosPersonal
                    ) ?? null
                  : null
            }));

          this.buscar();
        },
        error: err => {
          console.error(
            'Error cargando permisos especiales:',
            err
          );

          this.error =
            'No fue posible cargar los permisos especiales.';

          this.personas = [];
          this.filtradas = [];
          this.pagina = 1;
        }
      });
  }

  buscar(): void {
    const documento =
      this.normalizarTexto(
        this.filtros.documento
      );

    const nombres =
      this.normalizarTexto(
        this.filtros.nombres
      );

    const primerApellido =
      this.normalizarTexto(
        this.filtros.primerApellido
      );

    const segundoApellido =
      this.normalizarTexto(
        this.filtros.segundoApellido
      );

    this.filtradas = this.personas.filter(
      persona => {
        const documentoPersona =
          this.normalizarTexto(
            persona.documento
          );

        const nombresPersona =
          this.normalizarTexto(
            persona.nombres
          );

        const primerApellidoPersona =
          this.normalizarTexto(
            persona.primerApellido
          );

        const segundoApellidoPersona =
          this.normalizarTexto(
            persona.segundoApellido
          );

        return (
          (
            !documento
            || documentoPersona.includes(
              documento
            )
          )
          &&
          (
            !nombres
            || nombresPersona.includes(
              nombres
            )
          )
          &&
          (
            !primerApellido
            || primerApellidoPersona.includes(
              primerApellido
            )
          )
          &&
          (
            !segundoApellido
            || segundoApellidoPersona.includes(
              segundoApellido
            )
          )
        );
      }
    );

    this.pagina = 1;
    this.error = '';
  }

  limpiar(): void {
    this.filtros = {
      documento: '',
      nombres: '',
      primerApellido: '',
      segundoApellido: ''
    };

    this.filtradas = [...this.personas];
    this.pagina = 1;
    this.error = '';
  }

  get paginadas(): PersonaConPermiso[] {
    const inicio =
      (this.pagina - 1) * this.tamanoPagina;

    return this.filtradas.slice(
      inicio,
      inicio + this.tamanoPagina
    );
  }

  totalPaginas(): number {
    return Math.max(
      1,
      Math.ceil(
        this.filtradas.length
        / this.tamanoPagina
      )
    );
  }

  cambiarPagina(pagina: number): void {
    if (
      pagina < 1
      || pagina > this.totalPaginas()
    ) {
      return;
    }

    this.pagina = pagina;
  }

  gestionar(
    persona: DatosPersonales,
    permisos?: PermisoEspecial | null
  ): void {

    this.error = '';

    if (!persona.idDatosPersonal) {
      this.error =
        'Asociado inválido: no tiene idDatosPersonal.';

      return;
    }

    if (permisos?.idPermisoEspecial) {
      this.router.navigate([
        '/hoja-vida/permisos-especiales',
        permisos.idPermisoEspecial,
        'editar'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/hoja-vida/permisos-especiales/nuevo'
      ],
      {
        queryParams: {
          idDatosPersonal:
            persona.idDatosPersonal
        }
      }
    );
  }

  imprimir(): void {
    const datos =
      this.construirDatosInforme();

    if (datos.length === 0) {
      alert(
        'No hay permisos especiales para imprimir.'
      );

      return;
    }

    this.printService.imprimir(datos);
  }

  exportar(): void {
    const datos =
      this.construirDatosInforme();

    if (datos.length === 0) {
      alert(
        'No hay permisos especiales para exportar.'
      );

      return;
    }

    this.exporter.exportarExcel(datos);
  }

  private construirDatosInforme():
    PermisoEspecialInforme[] {

    return this.filtradas
      .filter(
        (
          persona
        ): persona is PersonaConPermiso & {
          permisos: PermisoEspecial;
        } => persona.permisos != null
      )
      .map(persona => ({
        ...persona.permisos,
        documento: persona.documento,
        nombrePersona: [
          persona.nombres,
          persona.primerApellido,
          persona.segundoApellido
        ]
          .filter(Boolean)
          .join(' ')
      }));
  }

  private normalizarTexto(
    valor: string | number | null | undefined
  ): string {

    return String(valor ?? '')
      .trim()
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '');
  }
}
