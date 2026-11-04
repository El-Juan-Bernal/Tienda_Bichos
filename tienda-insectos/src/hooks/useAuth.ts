import { useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { tokenRequest } from '../config/authConfig';

export function useAuth() {
  const { instance, accounts, inProgress } = useMsal();
  const account = accounts[0];
  const bloqueado = inProgress !== InteractionStatus.None;
  const roles = (account?.idTokenClaims as { roles?: string[] } | undefined)?.roles;
  const isAdmin = roles?.includes('Admin') ?? false;

  async function entrar() {
    await instance.loginPopup({ ...tokenRequest, prompt: 'select_account' });
  }

  async function salir() {
    if (account) {
      await instance.logoutPopup({ account });
      sessionStorage.clear();
    }
  }

  return { instance, account, bloqueado, isAdmin, entrar, salir };
}

