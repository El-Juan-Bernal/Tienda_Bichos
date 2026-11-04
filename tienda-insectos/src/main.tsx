import { createRoot } from 'react-dom/client';
import { PublicClientApplication } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import { msalConfig } from './config/authConfig';
import App from './App';
import './styles/variables.css';
import './styles/base.css';
import './styles/layout.css';
import './styles/inicio.css';
import './styles/catalogo.css';
import './styles/modal.css';
import './styles/carrito.css';
import './styles/pedidos.css';
import './styles/admin.css';
import './styles/toast.css';

const msal = new PublicClientApplication(msalConfig);

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element not found');
}

createRoot(rootElement).render(
  <MsalProvider instance={msal}>
    <App />
  </MsalProvider>,
);

