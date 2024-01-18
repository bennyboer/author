import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { filter, switchMap, tap } from 'rxjs';
import { inject } from '@angular/core';
import { LoginService } from '../store';

export const loggedInGuard: CanActivateFn = (
  _route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const router = inject(Router);
  const loginService = inject(LoginService);

  return loginService.isLoading().pipe(
    filter((isLoading) => !isLoading),
    switchMap(() => loginService.isLoggedIn()),
    tap((isLoggedIn) => {
      if (!isLoggedIn) {
        loginService.redirectAfterLogin(state.url);
        router.navigate(['/login']);
      }
    }),
  );
};
