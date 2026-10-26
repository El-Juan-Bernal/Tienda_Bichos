import { useState, useEffect } from 'react';
import { useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { tokenRequest } from './authConfig';
import { obtenerToken } from './token';
import { obtenerProductos } from './api';
import type { Producto } from './api';
import './App.css';
import AdminPanel from './components/AdminPanel';

type Vista = 'inicio' | 'catalogo' | 'carrito' | 'admin';
type Filtro = 'TODOS' | 'VOLADOR' | 'TERRESTRE' | 'ACUATICO';

interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}

interface PayloadOrden {
  usuario?: string;
  pasarelaSeleccionada: string;
  items: { productoId: number; cantidad: number }[];
  montoTotal: number;
}

async function httpPujarOrden(payload: PayloadOrden, token: string) {
  const base = import.meta.env.VITE_BFF_BASE_URL?.replace(/\/$/, '') || 'http://localhost:8080';
  return fetch(`${base}/api/pedidos`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(payload),
  });
}

async function httpActualizarEstadoPedido(pedidoId: number, nuevoEstado: string, token: string) {
  const base = import.meta.env.VITE_BFF_BASE_URL?.replace(/\/$/, '') || 'http://localhost:8080';
  return fetch(`${base}/api/pedidos/${pedidoId}/estado`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ nuevoEstado }),
  });
}

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
  const isAdmin = (account?.idTokenClaims as { roles?: string[] })?.roles?.includes('Admin') || false;

  const [vista, setVista] = useState<Vista>(() => {
    return (sessionStorage.getItem('vista_actual') as Vista) || 'inicio';
  });

  const [salida, setSalida] = useState('');
  const [productos, setProductos] = useState<Producto[]>([]);
  const [productosCargados, setProductosCargados] = useState(false);
  const [cargandoProductos, setCargandoProductos] = useState(false);
  const [errorProductos, setErrorProductos] = useState('');
  const [filtro, setFiltro] = useState<Filtro>('TODOS');
  const [carrito, setCarrito] = useState<ItemCarrito[]>(() => {
    const guardado = localStorage.getItem('carrito_insectos');
    return guardado ? JSON.parse(guardado) : [];
  });

  const [productoSeleccionado, setProductoSeleccionado] = useState<Producto | null>(null);
  const [cantidadModal, setCantidadModal] = useState<number>(1);
  const [mostrarModalPago, setMostrarModalPago] = useState(false);
  const [procesandoPago, setProcesandoPago] = useState(false);

  useEffect(() => {
    sessionStorage.setItem('vista_actual', vista);
    if (vista === 'catalogo' && !productosCargados && account) {
      void cargarProductosCatalogo();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vista, productosCargados, account]);

  useEffect(() => {
    localStorage.setItem('carrito_insectos', JSON.stringify(carrito));
  }, [carrito]);

  async function cargarProductosCatalogo() {
    setCargandoProductos(true);
    setErrorProductos('');
    try {
      const datos = await obtenerProductos(instance, account);
      const activos = datos.filter((p) => p.activo !== false);
      setProductos(activos);
      setProductosCargados(true);
    } catch (error) {
      setErrorProductos(error instanceof Error ? error.message : String(error));
    } finally {
      setCargandoProductos(false);
    }
  }

  const abrirDetalleProducto = (p: Producto) => {
    setProductoSeleccionado(p);
    setCantidadModal(1);
  };

  const agregarAlCarritoConCantidad = (producto: Producto, cantidadAñadir: number) => {
    const itemExistente = carrito.find((item) => item.producto.id === producto.id);
    const cantidadActualEnCarrito = itemExistente ? itemExistente.cantidad : 0;
    const stockDisponible = producto.stock ?? 0;

    if (cantidadActualEnCarrito + cantidadAñadir > stockDisponible) {
      alert(`No puedes agregar más unidades. El stock máximo disponible para este ejemplar es de ${stockDisponible} unidades.`);
      return;
    }

    setCarrito((prev) => {
      if (itemExistente) {
        return prev.map((item) =>
          item.producto.id === producto.id
            ? { ...item, cantidad: item.cantidad + cantidadAñadir }
            : item
        );
      }
      return [...prev, { producto, cantidad: cantidadAñadir }];
    });

    alert(`¡Se han agregado ${cantidadAñadir} unidad(es) de "${producto.nombre}" al carrito!`);
    setProductoSeleccionado(null);
  };

  const cambiarCantidadCarrito = (id: number, delta: number) => {
    const itemActual = carrito.find((i) => i.producto.id === id);
    if (!itemActual) return;
    const nuevaCantidad = itemActual.cantidad + delta;
    const stockDisponible = itemActual.producto.stock ?? 0;

    if (nuevaCantidad > stockDisponible) {
      alert(`Stock máximo alcanzado (${stockDisponible} unidades disponibles).`);
      return;
    }

    if (nuevaCantidad <= 0) {
      eliminarItemCarrito(id);
      return;
    }

    setCarrito((prev) =>
      prev.map((item) =>
        item.producto.id === id ? { ...item, cantidad: nuevaCantidad } : item
      )
    );
  };

  const eliminarItemCarrito = (id: number) => {
    setCarrito((prev) => prev.filter((item) => item.producto.id !== id));
  };

  const totalItemsCarrito = carrito.reduce((acc, item) => acc + item.cantidad, 0);
  const montoTotalCarrito = carrito.reduce((acc, item) => {
    const desc = item.producto.porcentajeDescuento || 0;
    const precioFinal = desc > 0 ? Math.round(item.producto.precio * (1 - desc / 100)) : item.producto.precio;
    return acc + precioFinal * item.cantidad;
  }, 0);

  const ejecutarPagoReal = async (pasarela: string) => {
    setProcesandoPago(true);
    try {
      const tokenResponse: any = await obtenerToken(instance, account);
      const token = typeof tokenResponse === 'string'
        ? tokenResponse
        : tokenResponse?.accessToken || '';

      const payloadOrden: PayloadOrden = {
        usuario: account?.username,
        pasarelaSeleccionada: pasarela,
        items: carrito.map((i) => ({
          productoId: i.producto.id,
          cantidad: i.cantidad,
        })),
        montoTotal: montoTotalCarrito,
      };

      const respuesta = await httpPujarOrden(payloadOrden, token);

      if (!respuesta.ok) {
        const msgError = await respuesta.text();
        alert(`No se pudo completar el pedido: ${msgError}`);
        return;
      }

      const pedidoCreado = await respuesta.json();
      const pedidoId = pedidoCreado.id;

      const respuestaPatch = await httpActualizarEstadoPedido(pedidoId, 'ACEPTADO', token);

      if (!respuestaPatch.ok) {
        alert('El pedido se creó, pero hubo un error al confirmar el pago en el servidor.');
        return;
      }

      alert(`¡Pago aprobado mediante ${pasarela}! \nSu compra ha sido realizada con éxito\nSu boleta será enviada a su correo.`);
      setCarrito([]);
      localStorage.removeItem('carrito_insectos');
      setMostrarModalPago(false);
      await cargarProductosCatalogo();
      setVista('catalogo');
    } catch (error) {
      console.error('Error al procesar pago:', error);
      alert('Error de conexión con el servidor al procesar el pago.');
    } finally {
      setProcesandoPago(false);
    }
  };

  async function entrar() {
    await instance.loginPopup({ ...tokenRequest, prompt: 'select_account' });
  }

  async function obtenerTokenClick() {
    if (!account) return;
    setSalida('');
    try {
      const token: any = await obtenerToken(instance, account);
      if (!token.accessToken) throw new Error('No se obtuvo access token');
      setSalida('Token de API obtenido. Vence: ' + (token.expiresOn?.toLocaleString() ?? 'Consultar metadatos'));
    } catch (error) {
      setSalida(error instanceof Error ? error.message : String(error));
    }
  }

  async function salir() {
    if (account) {
      await instance.logoutPopup({ account });
      sessionStorage.clear();
    }
  }

  const productosFiltrados = filtro === 'TODOS' ? productos : productos.filter((p) => p.categoria === filtro);

  return (
    <div className="pagina">
      <div className="barra-superior">
        {!account ? (
          <button type="button" className="btn-mini" disabled={bloqueado} onClick={() => void entrar()}>
            Iniciar sesión
          </button>
        ) : (
          <div className="barra-superior-acciones">
            <span className="usuario-actual">{account.username}</span>
            <button type="button" className="btn-mini" disabled={bloqueado} onClick={() => void obtenerTokenClick()}>
              Obtener token API
            </button>
            <button type="button" className="btn-mini" disabled={bloqueado} onClick={() => void salir()}>
              Cerrar sesión
            </button>
          </div>
        )}
      </div>

      <header className="menu-principal">
        <div className="logo">Tienda de Insectos H&J</div>
        <nav className="navegacion">
          <button type="button" className={vista === 'inicio' ? 'nav-link activo' : 'nav-link'} onClick={() => setVista('inicio')}>
            Inicio
          </button>
          <button type="button" className={vista === 'catalogo' ? 'nav-link activo' : 'nav-link'} onClick={() => setVista('catalogo')} disabled={!account}>
            Catálogo
          </button>
          {isAdmin && (
            <button type="button" className={vista === 'admin' ? 'nav-link activo' : 'nav-link'} onClick={() => setVista('admin')}>
              Panel Admin
            </button>
          )}
        </nav>
        {account && (
          <button type="button" className="carrito-badge-container" onClick={() => setVista('carrito')}>
            <span>Carrito ({totalItemsCarrito})</span>
          </button>
        )}
      </header>

      {salida && <p className="mensaje-token">{salida}</p>}

      <main className="contenido">
        {!account && <p className="aviso-login">Inicia sesión para explorar el catálogo de insectos.</p>}

        {vista === 'inicio' && (
          <>
            <section className="banner">
              <h1>Insectos fascinantes, directo a tu hogar</h1>
              <p>Ejemplares cuidadosamente seleccionados para coleccionistas y curiosos.</p>
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
                <button type="button" key={f} className={filtro === f ? 'filtro activo' : 'filtro'} onClick={() => setFiltro(f)}>
                  {etiquetasFiltro[f]}
                </button>
              ))}
            </div>
            {cargandoProductos && <p>Cargando catálogo...</p>}
            {errorProductos && <p className="error">{errorProductos}</p>}
            <div className="grilla-catalogo">
              {productosFiltrados.map((p) => {
                const descuento = p.porcentajeDescuento || 0;
                const tieneDescuento = descuento > 0;
                const precioFinal = tieneDescuento ? Math.round(p.precio * (1 - descuento / 100)) : p.precio;
                return (
                  <div className="tarjeta-producto-claro" key={p.id}>
                    {tieneDescuento && <span className="badge-descuento">-{descuento}%</span>}
                    <button
                      type="button"
                      className="btn-apertura-card"
                      onClick={() => abrirDetalleProducto(p)}
                      style={{
                        background: 'none',
                        border: 'none',
                        padding: 0,
                        cursor: 'pointer',
                        textAlign: 'center',
                        width: '100%',
                        fontFamily: 'inherit',
                      }}
                    >
                      <img src={p.imagenUrl} alt={p.nombre} />
                      <h3>{p.nombre}</h3>
                      <p className="descripcion">{p.descripcion}</p>
                      <div className="contenedor-precio">
                        {tieneDescuento ? (
                          <>
                            <span className="precio-original">{formatoPrecio.format(p.precio)}</span>
                            <span className="precio-oferta">{formatoPrecio.format(precioFinal)}</span>
                          </>
                        ) : (
                          <p className="precio">{formatoPrecio.format(p.precio)}</p>
                        )}
                      </div>
                      <p className="stock">Stock: {p.stock}</p>
                    </button>
                    <button
                      type="button"
                      className="btn-agregar-carrito"
                      onClick={() => agregarAlCarritoConCantidad(p, 1)}
                    >
                      Agregar al carrito
                    </button>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {vista === 'carrito' && (
          <section className="carrito-seccion">
            <h2>Tu Carrito de Compras</h2>
            {carrito.length === 0 ? (
              <p className="aviso-login">Tu carrito está vacío. ¡Explora el catálogo y añade ejemplares!</p>
            ) : (
              <div className="carrito-contenedor-detalles">
                <div className="carrito-lista">
                  {carrito.map((item) => {
                    const desc = item.producto.porcentajeDescuento || 0;
                    const precioFinal = desc > 0 ? Math.round(item.producto.precio * (1 - desc / 100)) : item.producto.precio;
                    return (
                      <div key={item.producto.id} className="carrito-item-row">
                        <img src={item.producto.imagenUrl} alt={item.producto.nombre} className="carrito-thumb" />
                        <div className="carrito-item-info">
                          <h4>{item.producto.nombre}</h4>
                          <p>Precio unitario: {formatoPrecio.format(precioFinal)}</p>
                        </div>
                        <div className="carrito-controles-cantidad">
                          <button type="button" onClick={() => cambiarCantidadCarrito(item.producto.id, -1)}>-</button>
                          <span>{item.cantidad}</span>
                          <button type="button" onClick={() => cambiarCantidadCarrito(item.producto.id, 1)}>+</button>
                        </div>
                        <p className="carrito-item-subtotal">{formatoPrecio.format(precioFinal * item.cantidad)}</p>
                        <button type="button" className="btn-eliminar-item" onClick={() => eliminarItemCarrito(item.producto.id)}>X</button>
                      </div>
                    );
                  })}
                </div>
                <div className="carrito-resumen">
                  <h3>Resumen de Compra</h3>
                  <div className="resumen-linea">
                    <span>Total productos:</span>
                    <span>{totalItemsCarrito} unidades</span>
                  </div>
                  <div className="resumen-linea total">
                    <span>Total a Pagar:</span>
                    <span>{formatoPrecio.format(montoTotalCarrito)}</span>
                  </div>
                  <button type="button" className="btn-guardar" onClick={() => setMostrarModalPago(true)}>
                    Proceder al Pago
                  </button>
                </div>
              </div>
            )}
          </section>
        )}

        {vista === 'admin' && (
          isAdmin ? <AdminPanel /> : <h2 style={{ textAlign: 'center', marginTop: '2rem', color: '#ef4444' }}>Acceso Denegado: No tienes permisos de administrador.</h2>
        )}
      </main>

      {productoSeleccionado && (
        <div className="modal-overlay">
          <div className="modal-contenido">
            <button type="button" className="modal-cerrar" onClick={() => setProductoSeleccionado(null)}>X</button>
            <div className="modal-grid">
              <img src={productoSeleccionado.imagenUrl} alt={productoSeleccionado.nombre} />
              <div className="modal-info">
                <h2>{productoSeleccionado.nombre}</h2>
                <span className="badge-categoria">{productoSeleccionado.categoria}</span>
                <p className="modal-descripcion-completa">{productoSeleccionado.descripcion}</p>
                <div className="contenedor-precio modal-precio">
                  {productoSeleccionado.porcentajeDescuento && productoSeleccionado.porcentajeDescuento > 0 ? (
                    <>
                      <span className="precio-original">{formatoPrecio.format(productoSeleccionado.precio)}</span>
                      <span className="precio-oferta">
                        {formatoPrecio.format(Math.round(productoSeleccionado.precio * (1 - productoSeleccionado.porcentajeDescuento / 100)))}
                      </span>
                    </>
                  ) : (
                    <p className="precio">{formatoPrecio.format(productoSeleccionado.precio)}</p>
                  )}
                </div>
                <p className="stock" style={{ marginBottom: '15px' }}>Stock disponible: {productoSeleccionado.stock} unidades</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '20px' }}>
                  <span style={{ fontSize: '14px', fontWeight: 600 }}>Cantidad:</span>
                  <div className="carrito-controles-cantidad">
                    <button
                      type="button"
                      onClick={() => setCantidadModal((prev) => Math.max(1, prev - 1))}
                    >
                      -
                    </button>
                    <span style={{ padding: '0 10px' }}>{cantidadModal}</span>
                    <button
                      type="button"
                      onClick={() => {
                        const stockMax = productoSeleccionado.stock ?? 0;
                        if (cantidadModal < stockMax) {
                          setCantidadModal((prev) => prev + 1);
                        } else {
                          alert(`Has alcanzado el límite de stock disponible (${stockMax} unidades).`);
                        }
                      }}
                    >
                      +
                    </button>
                  </div>
                </div>
                <button
                  type="button"
                  className="btn-guardar"
                  onClick={() => agregarAlCarritoConCantidad(productoSeleccionado, cantidadModal)}
                >
                  Agregar al carrito ({cantidadModal})
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {mostrarModalPago && (
        <div className="modal-overlay">
          <div className="modal-contenido modal-pago">
            <h2>Selecciona tu Pasarela de Pago</h2>
            <p className="subtitulo-pago">Monto total a cancelar: <strong>{formatoPrecio.format(montoTotalCarrito)}</strong></p>
            {procesandoPago ? (
              <p className="cargando-pago">Procesando pago de forma segura y generando su comprobante...</p>
            ) : (
              <div className="pasarelas-grid">
                <button type="button" className="btn-pasarela" onClick={() => void ejecutarPagoReal('Webpay Plus')}>
                  <img src="/webpay.png" alt="Webpay Plus" />
                  <span>Webpay Plus</span>
                </button>
                <button type="button" className="btn-pasarela" onClick={() => void ejecutarPagoReal('Mercado Pago')}>
                  <img src="/mercadopago.png" alt="Mercado Pago" />
                  <span>Mercado Pago</span>
                </button>
                <button type="button" className="btn-pasarela" onClick={() => void ejecutarPagoReal('Khipu')}>
                  <img src="/khipu.png" alt="Khipu" />
                  <span>Khipu</span>
                </button>
              </div>
            )}
            <button type="button" className="btn-cancelar-pago" disabled={procesandoPago} onClick={() => setMostrarModalPago(false)}>
              Cancelar
            </button>
          </div>
        </div>
      )}

      <footer className="pie-pagina">
        <h4 className="footer-titulo-principal">Tienda de Insectos H&J</h4>
        <div className="footer-contenido">
          <div className="footer-izq">
            <p>Proyecto académico Cloud Native</p>
            <p>Profesor: Alejandro Sepúlveda</p>
            <p>Autores: Hileok Hernández y Juan Bernal</p>
            <p>Sede Concepción</p>
          </div>
          <div className="footer-der">
            <img src="/logo_duocuc.png" alt="Logo Duoc UC" className="footer-logo" />
          </div>
        </div>
        <div className="footer-abajo">
          <p>&copy; {new Date().getFullYear()} Todos los derechos reservados.</p>
        </div>
      </footer>
    </div>
  );
}

