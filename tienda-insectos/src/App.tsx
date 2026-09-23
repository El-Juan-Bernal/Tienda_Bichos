import { useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { tokenRequest } from './authConfig';
import { obtenerToken } from './token';
import { obtenerProductos } from './api';
import type { Producto } from './api';
import './App.css';

type Vista = 'inicio' | 'catalogo';
type Filtro = 'TODOS' | 'VOLADOR' | 'TERRESTRE' | 'ACUATICO';

const recomendados = [
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

const formatoPrecio = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
});

const etiquetasFiltro: Record<Filtro, string> = {
  TODOS: 'Todos',
  VOLADOR: 'Voladores',
  TERRESTRE: 'Terrestres',
  ACUATICO: 'Acuáticos',
};

export default function App() {
  const { instance, accounts, inProgress } = useMsal();
  const account = accounts[0];
  const bloqueado = inProgress !== InteractionStatus.None;

  const [vista, setVista] = useState<Vista>('inicio');
  const [salida, setSalida] = useState('');
  const [productos, setProductos] = useState<Producto[]>([]);
  const [productosCargados, setProductosCargados] = useState(false);
  const [cargandoProductos, setCargandoProductos] = useState(false);
  const [errorProductos, setErrorProductos] = useState('');
  const [filtro, setFiltro] = useState<Filtro>('TODOS');

  async function entrar() {
    await instance.loginPopup({
      ...tokenRequest,
      prompt: 'select_account',
    });
  }

  async function obtenerTokenClick() {
    if (!account) return;
    setSalida('');
    try {
      const token = await obtenerToken(instance, account);
      if (!token.accessToken) {
        throw new Error('No se obtuvo access token');
      }
      setSalida(
        'Token de API obtenido. Vence: ' +
          (token.expiresOn?.toLocaleString() ?? 'Consultar metadatos'),
      );
    } catch (error) {
      setSalida(error instanceof Error ? error.message : String(error));
    }
  }

  async function salir() {
    if (account) {
      await instance.logoutPopup({ account });
    }
  }

  async function irACatalogo() {
    setVista('catalogo');
    if (productosCargados || !account) return;

    setCargandoProductos(true);
    setErrorProductos('');
    try {
      const datos = await obtenerProductos(instance, account);
      setProductos(datos);
      setProductosCargados(true);
    } catch (error) {
      setErrorProductos(
        error instanceof Error ? error.message : String(error),
      );
    } finally {
      setCargandoProductos(false);
    }
  }

  const productosFiltrados =
    filtro === 'TODOS'
      ? productos
      : productos.filter((p) => p.categoria === filtro);

  return (
    <div className="pagina">
      <div className="barra-superior">
        {!account ? (
          <button
            className="btn-mini"
            disabled={bloqueado}
            onClick={() => void entrar()}
          >
            Iniciar sesión
          </button>
        ) : (
          <div className="barra-superior-acciones">
            <span className="usuario-actual">{account.username}</span>
            <button
              className="btn-mini"
              disabled={bloqueado}
              onClick={() => void obtenerTokenClick()}
            >
              Obtener token API
            </button>
            <button
              className="btn-mini"
              disabled={bloqueado}
              onClick={() => void salir()}
            >
              Cerrar sesión
            </button>
          </div>
        )}
      </div>

      <header className="menu-principal">
        <div className="logo">🐞 Tienda Insectos HH-JB</div>
        <nav className="navegacion">
          <button
            className={vista === 'inicio' ? 'nav-link activo' : 'nav-link'}
            onClick={() => setVista('inicio')}
          >
            Inicio
          </button>
          <button
            className={vista === 'catalogo' ? 'nav-link activo' : 'nav-link'}
            onClick={() => void irACatalogo()}
            disabled={!account}
          >
            Catálogo
          </button>
        </nav>
      </header>

      {salida && <p className="mensaje-token">{salida}</p>}

      <main className="contenido">
        {!account && (
          <p className="aviso-login">
            Inicia sesión para explorar el catálogo de insectos.
          </p>
        )}

        {vista === 'inicio' && (
          <>
            <section className="banner">
              <h1>Insectos fascinantes, directo a tu hogar</h1>
              <p>
                Ejemplares cuidadosamente seleccionados para coleccionistas y
                curiosos.
              </p>
            </section>

            <section className="recomendados">
              <h2>Insectos recomendados</h2>
              <div className="grilla-recomendados">
                {recomendados.map((r) => (
                  <div className="tarjeta-recomendado" key={r.id}>
                    <img src={r.imagenUrl} alt={r.nombre} />
                    <h3>{r.nombre}</h3>
                    <p>{r.descripcion}</p>
                  </div>
                ))}
              </div>
            </section>
          </>
        )}

        {vista === 'catalogo' && (
          <section className="catalogo">
            <div className="filtros">
              {(Object.keys(etiquetasFiltro) as Filtro[]).map((f) => (
                <button
                  key={f}
                  className={filtro === f ? 'filtro activo' : 'filtro'}
                  onClick={() => setFiltro(f)}
                >
                  {etiquetasFiltro[f]}
                </button>
              ))}
            </div>

            {cargandoProductos && <p>Cargando catálogo...</p>}
            {errorProductos && <p className="error">{errorProductos}</p>}

            <div className="grilla-catalogo">
              {productosFiltrados.map((p) => (
                <div className="tarjeta-producto" key={p.id}>
                  <img src={p.imagenUrl} alt={p.nombre} />
                  <h3>{p.nombre}</h3>
                  <p className="descripcion">{p.descripcion}</p>
                  <p className="precio">{formatoPrecio.format(p.precio)}</p>
                  <p className="stock">Stock: {p.stock}</p>
                </div>
              ))}
            </div>
          </section>
        )}
      </main>

      <footer className="pie-pagina">
        <p>Tienda Insectos HH-JB — Proyecto académico Duoc UC</p>
      </footer>
    </div>
  );
}
