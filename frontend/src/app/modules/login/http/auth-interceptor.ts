import { Injectable, OnDestroy } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { LoginService } from '../store';
import {
  catchError,
  map,
  Observable,
  Subject,
  takeUntil,
  throwError,
} from 'rxjs';
import { Option } from '../../shared';

@Injectable()
export class AuthInterceptor implements HttpInterceptor, OnDestroy {
  private token: Option<string> = Option.none();

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly loginService: LoginService) {
    this.loginService
      .getToken()
      .pipe(
        map((token) => token.map((t) => t.getValue())),
        takeUntil(this.destroy$),
      )
      .subscribe((token) => (this.token = token));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    const updatedRequest = this.token
      .filter(() => !request.headers.has('UNAUTHORIZED'))
      .map((token) =>
        request.clone({
          headers: request.headers.set('Authorization', `Bearer ${token}`),
        }),
      )
      .orElse(request);

    return next.handle(updatedRequest).pipe(
      catchError((error) => {
        const statusCode = error.status;
        if (statusCode === 401) {
          this.loginService.logout();
        }

        return throwError(() => error);
      }),
    );
  }
}
