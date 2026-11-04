/** URL base del BFF, sin barra final. Único lugar donde se lee la variable. */
export const BFF_BASE_URL: string = (
  import.meta.env.VITE_BFF_BASE_URL || 'http://localhost:8080'
).replace(/\/$/, '');

