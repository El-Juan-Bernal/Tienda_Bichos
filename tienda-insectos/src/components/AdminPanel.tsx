import { useState, useEffect } from 'react';
import { useMsal } from '@azure/msal-react';
import { obtenerToken } from '../token';
import './AdminPanel.css';

interface FormularioProducto {
  idForm: number;
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number | '';
  stock: number | '';
  imagenUrl: string;
}

interface ProductoPayload {
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number;
  stock: number;
  porcentajeDescuento: number;
  imagenUrl: string;
  activo: boolean;
}

interface ProductoEdit {
  id: number;
  nombre: string;
  descripcion: string;
  categoria: string;
  precio: number;
  stock: number;
  porcentajeDescuento: number;
  imagenUrl: string;
  activo?: boolean;
}

export default function AdminPanel() {
  const { instance, accounts } = useMsal();
  const account = accounts[0];
  const API_URL = 'http://localhost:8080/api/productos';

  const [pestaña, setPestaña] = useState<'agregar' | 'editar' | 'descuentos'>('agregar');

  const [formularios, setFormularios] = useState<FormularioProducto[]>([
    { idForm: Date.now(), nombre: '', descripcion: '', categoria: '', precio: '', stock: '', imagenUrl: '' },
  ]);

  const [productos, setProductos] = useState<ProductoEdit[]>([]);
  const [productoEdicion, setProductoEdicion] = useState<ProductoEdit | null>(null);

  useEffect(() => {
    if (pestaña === 'editar' || pestaña === 'descuentos') {
      void cargarProductos();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pestaña]);

  async function extraerToken(): Promise<string> {
    const tokenResponse: unknown = await obtenerToken(instance, account);
    if (typeof tokenResponse === 'string') {
      return tokenResponse;
    }
    if (tokenResponse && typeof tokenResponse === 'object' && 'accessToken' in tokenResponse) {
      return String((tokenResponse as { accessToken: unknown }).accessToken);
    }
    return '';
  }

  async function cargarProductos() {
    try {
      const token = await extraerToken();
      const response = await fetch(API_URL, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        const data: ProductoEdit[] = await response.json();
        setProductos(data);
      } else {
        const errText = await response.text();
        console.error('Error al obtener productos:', response.status, errText);
      }
    } catch (error) {
      console.error('Error de red al cargar productos:', error);
    }
  }

  const bloquearNegativosTeclado = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === '-' || e.key === '+' || e.key === 'e' || e.key === 'E') {
      e.preventDefault();
    }
  };

  const agregarFormulario = () => {
    setFormularios([
      ...formularios,
      { idForm: Date.now(), nombre: '', descripcion: '', categoria: '', precio: '', stock: '', imagenUrl: '' },
    ]);
  };

  const eliminarFormulario = (idForm: number) => {
    setFormularios(formularios.filter((f) => f.idForm !== idForm));
  };

  const handleInputChange = (idForm: number, campo: keyof FormularioProducto, valor: string) => {
    setFormularios(
      formularios.map((f) => {
        if (f.idForm !== idForm) return f;

        if (campo === 'precio' || campo === 'stock') {
          if (valor === '') return { ...f, [campo]: '' };
          const num = Math.max(0, Number(valor));
          return { ...f, [campo]: num };
        }

        return { ...f, [campo]: valor };
      })
    );
  };

  const validarFormularios = (): string | null => {
    for (let i = 0; i < formularios.length; i++) {
      const f = formularios[i];
      if (!f.nombre.trim() || !f.categoria || f.precio === '' || f.stock === '') {
        return `Por favor completa todos los campos obligatorios en el Producto ${i + 1}.`;
      }
      if (Number(f.precio) < 0 || Number(f.stock) < 0) {
        return `El precio y el stock no pueden ser negativos en el Producto ${i + 1}.`;
      }
      if (f.descripcion.length > 1000) {
        return `La descripción del Producto ${i + 1} excede los 1000 caracteres permitidos.`;
      }
    }
    return null;
  };

  const limpiarFormularios = () => {
    setFormularios([
      { idForm: Date.now(), nombre: '', descripcion: '', categoria: '', precio: '', stock: '', imagenUrl: '' },
    ]);
  };

  const guardarUnoPorUno = async (payload: ProductoPayload[], token: string): Promise<boolean> => {
    for (const item of payload) {
      const resIndividual = await fetch(API_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(item),
      });
      if (!resIndividual.ok) {
        const errDetalle = await resIndividual.text();
        alert(`Error al guardar "${item.nombre}" (Código ${resIndividual.status}): ${errDetalle}`);
        return false;
      }
    }
    return true;
  };

  const guardarMasivo = async () => {
    const errorValidacion = validarFormularios();
    if (errorValidacion) {
      alert(errorValidacion);
      return;
    }

    const payload: ProductoPayload[] = formularios.map((f) => ({
      nombre: f.nombre.trim(),
      descripcion: f.descripcion.trim(),
      categoria: f.categoria,
      precio: Number(f.precio),
      stock: Number(f.stock),
      porcentajeDescuento: 0,
      imagenUrl: f.imagenUrl.trim(),
      activo: true,
    }));

    try {
      const token = await extraerToken();
      const response = await fetch(`${API_URL}/masivo`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (response.status === 404 || response.status === 405) {
        const exitoIndividual = await guardarUnoPorUno(payload, token);
        if (exitoIndividual) {
          alert('¡Productos guardados exitosamente en la base de datos!');
          limpiarFormularios();
        }
        return;
      }

      if (response.ok) {
        alert('¡Productos guardados exitosamente en la base de datos!');
        limpiarFormularios();
      } else {
        const errorText = await response.text();
        alert(`No se pudo guardar en BD (Error ${response.status}): ${errorText || 'Revisa los logs del BFF/Backend'}`);
      }
    } catch (error) {
      console.error('Error al guardar masivo:', error);
      alert('Error de conexión con el servidor BFF. Verifica que esté corriendo en el puerto 8080.');
    }
  };

  const actualizarProducto = async () => {
    if (!productoEdicion) return;
    if (productoEdicion.precio < 0 || productoEdicion.stock < 0) {
      alert('El precio y el stock no pueden ser valores negativos.');
      return;
    }

    try {
      const token = await extraerToken();
      const response = await fetch(`${API_URL}/${productoEdicion.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(productoEdicion),
      });
      if (response.ok) {
        alert(`Producto "${productoEdicion.nombre}" actualizado correctamente.`);
        setProductoEdicion(null);
        await cargarProductos();
      } else {
        const err = await response.text();
        alert(`Error al actualizar (Código ${response.status}): ${err}`);
      }
    } catch (error) {
      console.error('Error al actualizar:', error);
    }
  };

  const guardarDescuento = async (producto: ProductoEdit) => {
    try {
      const token = await extraerToken();
      const response = await fetch(`${API_URL}/${producto.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(producto),
      });
      if (response.ok) {
        alert(`¡Descuento de ${producto.porcentajeDescuento}% guardado en BD para "${producto.nombre}"!`);
        await cargarProductos();
      } else {
        const err = await response.text();
        alert(`El servidor rechazó el cambio (Código ${response.status}): ${err}`);
      }
    } catch (error) {
      console.error('Error al guardar descuento:', error);
      alert('Error de red al intentar actualizar el descuento.');
    }
  };

  const cambiarDescuentoLocal = (id: number, valor: string) => {
    const num = valor === '' ? 0 : Math.min(100, Math.max(0, Number(valor)));
    setProductos(productos.map((p) => (p.id === id ? { ...p, porcentajeDescuento: num } : p)));
  };

  const toggleEstadoProducto = async (p: ProductoEdit) => {
    const nuevoEstado = p.activo === false ? true : false;
    try {
      const token = await extraerToken();
      const response = await fetch(`${API_URL}/${p.id}/estado`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ activo: nuevoEstado }),
      });
      if (response.ok) {
        setProductos(
          productos.map((item) => (item.id === p.id ? { ...item, activo: nuevoEstado } : item))
        );
      } else {
        const err = await response.text();
        alert(`No se pudo cambiar el estado (Código ${response.status}): ${err}`);
      }
    } catch (error) {
      console.error('Error al cambiar estado:', error);
    }
  };

  return (
    <div className="admin-panel-container">
      <div className="admin-tabs">
        <button
          className={`tab-btn ${pestaña === 'agregar' ? 'active' : ''}`}
          onClick={() => setPestaña('agregar')}
        >
          Agregar producto
        </button>
        <button
          className={`tab-btn ${pestaña === 'editar' ? 'active' : ''}`}
          onClick={() => {
            setPestaña('editar');
            setProductoEdicion(null);
          }}
        >
          Editar producto
        </button>
        <button
          className={`tab-btn ${pestaña === 'descuentos' ? 'active' : ''}`}
          onClick={() => setPestaña('descuentos')}
        >
          Ofertas y Descuentos
        </button>
      </div>

      {/* PESTAÑA 1: AGREGAR PRODUCTO */}
      {pestaña === 'agregar' && (
        <div className="admin-content">
          <h2 className="admin-title">Panel de Administración - Carga de Productos</h2>
          {formularios.map((form, index) => (
            <div key={form.idForm} className="admin-form-box">
              <div className="admin-form-header">
                <h3>Producto {index + 1}</h3>
                {index > 0 && (
                  <button className="btn-eliminar" onClick={() => eliminarFormulario(form.idForm)}>
                    🗑️ Eliminar formulario
                  </button>
                )}
              </div>

              <div className="form-group">
                <label htmlFor={`nombre-${form.idForm}`} className="admin-label">
                  Nombre del insecto
                </label>
                <input
                  id={`nombre-${form.idForm}`}
                  type="text"
                  className="admin-input"
                  placeholder="Ej: Araña pollito"
                  value={form.nombre}
                  onChange={(e) => handleInputChange(form.idForm, 'nombre', e.target.value)}
                />
              </div>

              <div className="form-group">
                <div className="label-con-contador">
                  <label htmlFor={`descripcion-${form.idForm}`} className="admin-label">
                    Descripción
                  </label>
                  <span className={`contador-caracteres ${form.descripcion.length > 1000 ? 'exceso' : ''}`}>
                    {form.descripcion.length}/1000 Máximo 1000 caracteres
                  </span>
                </div>
                <textarea
                  id={`descripcion-${form.idForm}`}
                  className="admin-textarea"
                  placeholder="Descripción detallada del ejemplar"
                  rows={4}
                  maxLength={1000}
                  value={form.descripcion}
                  onChange={(e) => handleInputChange(form.idForm, 'descripcion', e.target.value)}
                />
              </div>

              <div className="form-group">
                <label htmlFor={`categoria-${form.idForm}`} className="admin-label">
                  Categoría
                </label>
                <select
                  id={`categoria-${form.idForm}`}
                  className="admin-input"
                  value={form.categoria}
                  onChange={(e) => handleInputChange(form.idForm, 'categoria', e.target.value)}
                >
                  <option value="">Seleccione Categoría</option>
                  <option value="TERRESTRE">TERRESTRE</option>
                  <option value="VOLADOR">VOLADOR</option>
                  <option value="ACUATICO">ACUÁTICO</option>
                </select>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label htmlFor={`precio-${form.idForm}`} className="admin-label">
                    Precio ($ CLP)
                  </label>
                  <input
                    id={`precio-${form.idForm}`}
                    type="number"
                    min="0"
                    onKeyDown={bloquearNegativosTeclado}
                    className="admin-input"
                    placeholder="Ej: 15000"
                    value={form.precio}
                    onChange={(e) => handleInputChange(form.idForm, 'precio', e.target.value)}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor={`stock-${form.idForm}`} className="admin-label">
                    Stock disponible (Unidades)
                  </label>
                  <input
                    id={`stock-${form.idForm}`}
                    type="number"
                    min="0"
                    onKeyDown={bloquearNegativosTeclado}
                    className="admin-input"
                    placeholder="Ej: 10"
                    value={form.stock}
                    onChange={(e) => handleInputChange(form.idForm, 'stock', e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor={`imagen-${form.idForm}`} className="admin-label">
                  URL de la imagen
                </label>
                <input
                  id={`imagen-${form.idForm}`}
                  type="text"
                  className="admin-input"
                  placeholder="https://..."
                  value={form.imagenUrl}
                  onChange={(e) => handleInputChange(form.idForm, 'imagenUrl', e.target.value)}
                />
              </div>
            </div>
          ))}
          <div className="admin-actions">
            <button className="btn-agregar-otro" onClick={agregarFormulario}>
              + Agregar otro producto
            </button>
            <button className="btn-guardar" onClick={guardarMasivo}>
              Guardar Todo en BD
            </button>
          </div>
        </div>
      )}


      {/* PESTAÑA 2: EDITAR PRODUCTO */}
      {pestaña === 'editar' && (
        <div className="admin-content">
          <h2 className="admin-title">Gestión de Inventario</h2>
          {productoEdicion ? (
            <div className="admin-form-box">
              <div className="edit-header-preview">
                <img
                  src={productoEdicion.imagenUrl || 'https://via.placeholder.com/80?text=Sin+Foto'}
                  alt={productoEdicion.nombre}
                  className="producto-thumb-large"
                />
                <h3>Editando: {productoEdicion.nombre}</h3>
              </div>

              <div className="form-group">
                <label htmlFor="edit-nombre" className="admin-label">
                  Nombre del insecto
                </label>
                <input
                  id="edit-nombre"
                  type="text"
                  className="admin-input"
                  value={productoEdicion.nombre}
                  onChange={(e) => setProductoEdicion({ ...productoEdicion, nombre: e.target.value })}
                />
              </div>

              <div className="form-group">
                <div className="label-con-contador">
                  <label htmlFor="edit-descripcion" className="admin-label">
                    Descripción
                  </label>
                  <span className="contador-caracteres">Máximo 1000 caracteres</span>
                </div>
                <textarea
                  id="edit-descripcion"
                  className="admin-textarea"
                  rows={4}
                  maxLength={1000}
                  value={productoEdicion.descripcion}
                  onChange={(e) => setProductoEdicion({ ...productoEdicion, descripcion: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label htmlFor="edit-categoria" className="admin-label">
                  Categoría
                </label>
                <select
                  id="edit-categoria"
                  className="admin-input"
                  value={productoEdicion.categoria}
                  onChange={(e) => setProductoEdicion({ ...productoEdicion, categoria: e.target.value })}
                >
                  <option value="TERRESTRE">TERRESTRE</option>
                  <option value="VOLADOR">VOLADOR</option>
                  <option value="ACUATICO">ACUÁTICO</option>
                </select>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label htmlFor="edit-precio" className="admin-label">
                    Precio ($ CLP)
                  </label>
                  <input
                    id="edit-precio"
                    type="number"
                    min="0"
                    onKeyDown={bloquearNegativosTeclado}
                    className="admin-input"
                    value={productoEdicion.precio === 0 ? '' : productoEdicion.precio}
                    onChange={(e) => {
                      const val = e.target.value;
                      setProductoEdicion({
                        ...productoEdicion,
                        precio: val === '' ? 0 : Math.max(0, Number(val)),
                      });
                    }}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="edit-stock" className="admin-label">
                    Stock disponible
                  </label>
                  <input
                    id="edit-stock"
                    type="number"
                    min="0"
                    onKeyDown={bloquearNegativosTeclado}
                    className="admin-input"
                    value={productoEdicion.stock === 0 ? '' : productoEdicion.stock}
                    onChange={(e) => {
                      const val = e.target.value;
                      setProductoEdicion({
                        ...productoEdicion,
                        stock: val === '' ? 0 : Math.max(0, Number(val)),
                      });
                    }}
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="edit-imagen" className="admin-label">
                  URL de la imagen
                </label>
                <input
                  id="edit-imagen"
                  type="text"
                  className="admin-input"
                  value={productoEdicion.imagenUrl}
                  onChange={(e) => setProductoEdicion({ ...productoEdicion, imagenUrl: e.target.value })}
                />
              </div>

              <div className="admin-actions">
                <button type="button" className="btn-guardar" onClick={actualizarProducto}>
                  Guardar Cambios
                </button>
                <button type="button" className="btn-cancelar" onClick={() => setProductoEdicion(null)}>
                  Cancelar
                </button>
              </div>
            </div>
          ) : (
            <div className="productos-lista">
              <div className="lista-header-info">
                <span>Habilitado / Deshabilitado</span>
              </div>
              {productos.map((p) => {
                const activo = p.activo !== false;
                return (
                  <div
                    key={p.id}
                    className={`producto-item ${!activo ? 'producto-inactivo' : ''}`}
                  >
                    <div className="producto-info-left">
                      <img
                        src={p.imagenUrl || 'https://via.placeholder.com/60?text=Insecto'}
                        alt={p.nombre}
                        className="producto-thumb"
                        onError={(e) => {
                          (e.currentTarget as HTMLImageElement).src = 'https://via.placeholder.com/60?text=Sin+Imagen';
                        }}
                      />
                      <div className="producto-texto-lista">
                        <span className="producto-nombre-item">{p.nombre}</span>
                        <span className="producto-meta-item">
                          Categoría: {p.categoria} | Precio: ${p.precio.toLocaleString('es-CL')} | Stock: {p.stock}
                          {!activo && <strong style={{ color: '#ef4444', marginLeft: '8px' }}>(Deshabilitado)</strong>}
                        </span>
                      </div>
                    </div>

                    <div className="producto-acciones-derecha">
                      <label className="switch" aria-label={`Cambiar estado de ${p.nombre}`}>
                        <input
                          type="checkbox"
                          checked={activo}
                          onChange={() => void toggleEstadoProducto(p)}
                        />
                        <span className="slider round"></span>
                      </label>
                      <button type="button" className="btn-editar" onClick={() => setProductoEdicion(p)}>
                        ✏️ Editar
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}


      {/* PESTAÑA 3: OFERTAS Y DESCUENTOS */}
      {pestaña === 'descuentos' && (
        <div className="admin-content">
          <h2 className="admin-title">Gestión de Ofertas y Descuentos</h2>
          <div className="productos-lista">
            {productos.map((p) => {
              const precioFinal = Math.round(p.precio * (1 - (p.porcentajeDescuento || 0) / 100));
              return (
                <div key={p.id} className="producto-item">
                  <div className="producto-info-left">
                    <img
                      src={p.imagenUrl || 'https://via.placeholder.com/60?text=Insecto'}
                      alt={p.nombre}
                      className="producto-thumb"
                      onError={(e) => {
                        (e.currentTarget as HTMLImageElement).src = 'https://via.placeholder.com/60?text=Sin+Imagen';
                      }}
                    />
                    <div className="producto-texto-lista">
                      <span className="producto-nombre-item">{p.nombre}</span>
                      <span className="producto-meta-item">
                        Precio Base: ${p.precio.toLocaleString('es-CL')} →{' '}
                        <strong>Precio Oferta: ${precioFinal.toLocaleString('es-CL')}</strong>
                      </span>
                    </div>
                  </div>

                  <div className="descuento-controles">
                    <div className="descuento-input-group">
                      <label htmlFor={`descuento-${p.id}`} className="admin-label-inline">
                        % Descuento (0-100)
                      </label>
                      <input
                        id={`descuento-${p.id}`}
                        type="number"
                        min="0"
                        max="100"
                        onKeyDown={bloquearNegativosTeclado}
                        className="admin-input-small"
                        value={p.porcentajeDescuento === 0 ? '' : p.porcentajeDescuento}
                        placeholder="0"
                        onChange={(e) => cambiarDescuentoLocal(p.id, e.target.value)}
                      />
                    </div>
                    <button className="btn-guardar-sm" onClick={() => guardarDescuento(p)}>
                      Aplicar
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

