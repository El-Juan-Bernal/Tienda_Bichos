export type TipoToast = 'exito' | 'error' | 'aviso' | 'info';

export interface ToastData {
  id: number;
  tipo: TipoToast;
  titulo: string;
  mensaje?: string;
  duracion: number;
}

export type MostrarToast = (
  tipo: TipoToast,
  titulo: string,
  mensaje?: string,
  duracion?: number,
) => void;

