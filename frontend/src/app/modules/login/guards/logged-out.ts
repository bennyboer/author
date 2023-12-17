import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { combineLatest, filter, map, tap } from 'rxjs';
import { inject } from '@angular/core';
import { LoginService } from '../store';

export const loggedOutGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const router = inject(Router);
  const loginService = inject(LoginService);

  return combineLatest([
    loginService.isLoading(),
    loginService.isLoggedIn(),
  ]).pipe(
    map(([isLoading, isLoggedIn]) => ({
      isLoading,
      isLoggedIn,
    })),
    filter(({ isLoading }) => !isLoading),
    map(({ isLoggedIn }) => !isLoggedIn),
    tap((isLoggedOut) => {
      if (!isLoggedOut) {
        router.navigate(['/']);
      }
    }),
  );
};
