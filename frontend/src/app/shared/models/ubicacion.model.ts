import { EntidadBase } from './entidad-base.model';

/**
 * 🌍 Mixin de ubicación geográfica
 * Se reutiliza en tablas con país, departamento, ciudad, zona o subzona.
 */
export interface ConUbicacionGeografica {
  idPais?: number;
  idDepartamento?: number;
  idCiudad?: number;
  idZona?: number;
  idSubZona?: number;
}

/**
 * 📍 Entidad Ubicación
 * Combina datos de dirección y contacto con la información geográfica.
 */
export interface Ubicacion extends EntidadBase, ConUbicacionGeografica {
  idUbicacion?: number;
  idDatosPersonal?: number;
  direccion: string;
  barrio?: string;
  telefono?: string;
  celularUno?: string;
  celularDos?: string;
  correo?: string;
}
