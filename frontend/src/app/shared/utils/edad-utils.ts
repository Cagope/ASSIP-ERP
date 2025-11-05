/**
 * 📅 Utilidad global — Cálculo de edad
 * ------------------------------------------------------------
 * Permite calcular la edad de una persona a partir de su fecha de nacimiento.
 *
 * Uso:
 *   const edad = calcularEdad('2005-06-15'); // contra hoy
 *   const edad = calcularEdad('2005-06-15', '2025-01-01'); // contra fecha específica
 */

/**
 * Calcula la edad en años cumplidos.
 * @param fechaNacimiento Fecha de nacimiento (Date o string ISO)
 * @param fechaReferencia (opcional) Fecha contra la cual calcular la edad. Si no se suministra, se usa hoy.
 * @returns número entero de años cumplidos.
 */
export function calcularEdad(
  fechaNacimiento: string | Date,
  fechaReferencia?: string | Date
): number {
  if (!fechaNacimiento) return 0;

  const nacimiento = new Date(fechaNacimiento);
  const referencia = fechaReferencia ? new Date(fechaReferencia) : new Date();

  let edad = referencia.getFullYear() - nacimiento.getFullYear();
  const m = referencia.getMonth() - nacimiento.getMonth();

  if (m < 0 || (m === 0 && referencia.getDate() < nacimiento.getDate())) {
    edad--;
  }

  return edad >= 0 ? edad : 0;
}
