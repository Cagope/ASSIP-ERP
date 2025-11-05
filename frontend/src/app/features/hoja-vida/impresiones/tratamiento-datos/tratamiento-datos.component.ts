import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { DatosPersonalesApi } from '../../datos-personales/datos-personales.api';
import { PermisosEspecialesApi } from '../../permisos-especiales/permisos-especiales.api';

@Component({
  selector: 'app-tratamiento-datos',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tratamiento-datos.component.html',
  styleUrls: ['./tratamiento-datos.component.scss']
})
export class TratamientoDatosComponent implements OnInit {

  // ===========================================================
  // 🔧 Dependencias
  // ===========================================================
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly datosApi = inject(DatosPersonalesApi);
  private readonly permisosApi = inject(PermisosEspecialesApi);

  // ===========================================================
  // 📦 Estado
  // ===========================================================
  persona = signal<any | null>(null);
  permisos = signal<any | null>(null);
  cargandoPermisos = signal(false);
  errPermisos = signal<string | null>(null);
  today = new Date();

  // Simulación de empresa (ya que no usas EmpresaService)
  empresa = signal({
    logoUrl: 'assets/logo-web.png',
    razonSocial: 'ERP ASSIP SOLIDARIA Y FINANCIERA',
    documento: '901234567',
    digitoVerificacion: '1',
    nombreComercial: 'ASSIP ERP'
  });

  // ===========================================================
  // 🚀 Inicialización
  // ===========================================================
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.cargarPersona(id);
    }
  }

  // ===========================================================
  // 📥 Cargar persona desde API
  // ===========================================================
  private cargarPersona(id: number): void {
    this.datosApi.listar().subscribe({
      next: (data: any[]) => {
        const p = data.find(d => d.idDatosPersonal === id);
        this.persona.set(p ?? null);
        this.cargarPermisos(id);
      },
      error: () => console.error('Error cargando datos personales')
    });
  }

  // ===========================================================
  // 🔁 Cargar permisos especiales
  // ===========================================================
  private cargarPermisos(id: number): void {
    this.cargandoPermisos.set(true);
    this.permisosApi.listar().subscribe({
      next: (data: any[]) => {
        const encontrado = data.find(p => p.idDatosPersonal === id);
        this.permisos.set(encontrado ?? null);
      },
      error: () => this.errPermisos.set('Error al cargar los permisos'),
      complete: () => this.cargandoPermisos.set(false)
    });
  }

  // ===========================================================
  // 🖨️ Funciones generales
  // ===========================================================
  onBack(): void {
    this.router.navigate(['/hoja-vida/impresiones/afiliacion-list']);
  }

  onPrint(): void {
    window.print();
  }

  toLogoUrl(url: string): string {
    return url?.startsWith('http') ? url : url || 'assets/logo-web.png';
  }

  // ===========================================================
  // 📄 Contador de páginas (pantalla)
  // ===========================================================
  pageNow(): number {
    return 1;
  }

  pageCount(): number {
    return 1;
  }

  // ===========================================================
  // ✅ Utilidad
  // ===========================================================
  siNo(valor: boolean | null | undefined): string {
    return valor ? 'Sí' : 'No';
  }
}
