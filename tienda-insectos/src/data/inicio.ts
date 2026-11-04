export interface SlideInicio {
  etiqueta: string;
  titulo: string;
  texto: string;
  imagenUrl: string;
}

export interface Recomendado {
  id: number;
  nombre: string;
  descripcion: string;
  imagenUrl: string;
}

/** Productos fijos que se muestran si todavía no hay destacados reales. */
export const recomendados: Recomendado[] = [
  {
    id: 1,
    nombre: 'Mantis Religiosa',
    imagenUrl: 'https://misanimales.com/wp-content/uploads/2020/03/mantis-religiosa-defensa-1024x678.jpg',
    descripcion: 'Un cazador paciente, fascinante de observar en su terrario.',
  },
  {
    id: 2,
    nombre: 'Mariposa Morpho Azul',
    imagenUrl: 'https://img.freepik.com/fotos-premium/mariposa-morfo-azul-sus-alas-abiertas-hoja_673637-801.jpg?w=2000',
    descripcion: 'Su vuelo deja un destello azul imposible de ignorar.',
  },
  {
    id: 3,
    nombre: 'Escarabajo Hércules',
    imagenUrl: 'https://2.bp.blogspot.com/-jhC3EGqWyLA/WJXcSqvN7xI/AAAAAAAAAbY/hJTXely3t4Y78ur1ZVobfFeCg7V0GHH8QCLcB/s1600/2017-04-2--10-18-21.png',
    descripcion: 'Uno de los favoritos por su tamaño y sus cuernos imponentes.',
  },
  {
    id: 4,
    nombre: 'Burrito',
    imagenUrl: 'https://proyectodescartes.org/iCartesiLibri/materiales_didacticos/Coleopteros_escarabajos/imagenes/cap4/43.png',
    descripcion: 'Uno de los más dóciles y fácil de cuidar, ideal para principiantes.',
  },
];

/** Slides del carrusel. Cuando tengas fotos grandes, cambia solo las imagenUrl. */
export const slidesInicio: SlideInicio[] = [
  {
    etiqueta: 'Tienda online',
    titulo: 'Insectos fascinantes, directo a tu hogar',
    texto: 'Ejemplares cuidadosamente seleccionados para coleccionistas y curiosos.',
    imagenUrl: recomendados[0].imagenUrl,
  },
  {
    etiqueta: 'Voladores',
    titulo: 'Color y vuelo en movimiento',
    texto: 'Mariposas y otros ejemplares que llenan de vida cualquier espacio.',
    imagenUrl: recomendados[1].imagenUrl,
  },
  {
    etiqueta: 'Terrestres',
    titulo: 'Fuerza y carácter en miniatura',
    texto: 'Escarabajos y arácnidos, ideales para empezar tu colección.',
    imagenUrl: recomendados[2].imagenUrl,
  },
  {
    etiqueta: 'Para principiantes',
    titulo: 'Fáciles de cuidar, fáciles de querer',
    texto: 'Especies dóciles que no exigen experiencia previa.',
    imagenUrl: recomendados[3].imagenUrl,
  },
];

export const categoriasBanner = [
  { nombre: 'Terrestres', texto: 'Escarabajos, mantis y arañas que recorren el suelo.' },
  { nombre: 'Voladores', texto: 'Mariposas y otros ejemplares de vuelo espectacular.' },
  { nombre: 'Acuáticos', texto: 'Especies que viven entre el agua y las plantas.' },
];

