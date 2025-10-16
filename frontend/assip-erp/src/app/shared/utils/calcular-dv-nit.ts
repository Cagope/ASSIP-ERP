/**
 * Cálculo del dígito de verificación NIT (Colombia, DIAN).
 *
 * Regla:
 *  1) Tomar el NIT en dígitos (sin DV).
 *  2) Multiplicar de derecha a izquierda por los pesos:
 *     [3,7,13,17,19,23,29,37,41,43,47,53,59,67,71]
 *  3) Sumar productos, calcular (suma % 11).
 *  4) DV = (res > 1) ? 11 - res : res
 *
 * @param nit Cadena numérica del NIT SIN dígito de verificación (solo dígitos).
 * @returns DV como string ("0".."9") o null si la entrada es inválida.
 */
export function calcularDvNit(nit: string): string | null {
  if (typeof nit !== 'string' || nit.length === 0) return null;
  if (!/^\d+$/.test(nit)) return null;

  // Pesos DIAN aplicados de derecha a izquierda
  const pesos = [3,7,13,17,19,23,29,37,41,43,47,53,59,67,71];

  let suma = 0;
  const rev = nit.split('').reverse();
  for (let i = 0; i < rev.length; i++) {
    const d = Number(rev[i]);
    const p = pesos[i] ?? 0;
    suma += d * p;
  }

  const res = suma % 11;
  const dv = res > 1 ? 11 - res : res;
  return String(dv);
}

/**
 * Valida que el DV provisto coincide con el calculado para el NIT.
 * @param nit Cadena numérica del NIT SIN DV.
 * @param dv  DV como string ("0".."9" o "10").
 * @returns true si coincide, false si no; null si la entrada es inválida.
 */
export function validarDvNit(nit: string, dv: string): boolean | null {
  const calc = calcularDvNit(nit);
  if (calc === null) return null;
  if (!/^\d{1,2}$/.test(dv)) return null;
  return calc === dv;
}
