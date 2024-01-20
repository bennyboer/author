import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { filter, of, switchMap, tap } from 'rxjs';
import { inject } from '@angular/core';
import { LoginService } from '../store';

const EXCLUDED_PATHS = [
  {
    containsAll: ['/users', '/mail/confirmation'],
  },
];

export const loggedInGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const currentUrl = state.url;
  const isExcluded = EXCLUDED_PATHS.some((excludedPath) =>
    excludedPath.containsAll.every((path) => {
      return currentUrl.includes(path);
    }),
  );

  if (isExcluded) {
    return of(true);
  }

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
