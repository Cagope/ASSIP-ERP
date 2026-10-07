export interface CajaOperacionPendiente {
  idMovimientoCaja: number;
  idProvision: number;
  idCaja: number;

  fechaContable: string;
  horaMovimiento: string;

  idOperacionCaja: number;
  codigoOperacion: string;
  nombreOperacion: string;

  naturaleza: 'INGRESO' | 'EGRESO';
  medioPago: string;
  valor: number;

  concepto: string;
  estado: 'PENDIENTE';

  moduloOrigen: string;
  procesoOrigen: string;
  tablaOrigen: string;
  idOrigen: number;
  codigoReferencia: string | null;

  tipoComprobante: string | null;
  numeroComprobante: string | null;

  idDatosPersonal: number;
  tipoDocumento: string;
  documento: string;
  nombreCompleto: string;
}
