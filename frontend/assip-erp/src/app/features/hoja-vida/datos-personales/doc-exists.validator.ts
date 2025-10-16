import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { of } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { DatosPersonalesApi } from './datos-personales.api';

export function docExistsValidator(
  api: DatosPersonalesApi,
  getCurrentId: () => number | null
): AsyncValidatorFn {
  return (control: AbstractControl) => {
    // Solo a nivel FormGroup y solo cuando es NUEVO
    if (!control || typeof (control as any).get !== 'function') {
      return of<ValidationErrors | null>(null);
    }
    if (getCurrentId() != null) {
      return of<ValidationErrors | null>(null);
    }

    const rawTipo = (control.get('tipoDocumento')?.value ?? '').toString();
    const rawDoc  = (control.get('documento')?.value ?? '').toString();

    const tipo = rawTipo.trim().toUpperCase();
    const doc  = rawDoc.trim(); // documento ya viene numérico, no cambies may/min

    if (!tipo || !doc) return of<ValidationErrors | null>(null);

    return api.list({ q: doc, size: 50 }).pipe(
      // DEBUG opcional:
      // tap(page => console.log('[docExists] q=', doc, 'tipo=', tipo, 'res=', page)),
      map(page => {
        const exists = (page?.content ?? []).some(it => {
          const itTipo = (it.tipoDocumento ?? '').toString().trim().toUpperCase();
          const itDoc  = (it.documento ?? '').toString().trim();
          return itTipo === tipo && itDoc === doc;
        });
        return exists ? { docDuplicado: true } : null;
      }),
      catchError(() => of<ValidationErrors | null>(null))
    );
  };
}
