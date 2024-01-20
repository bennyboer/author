import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { environment } from '../../../../../environments';
import { catchError, Subject, takeUntil, throwError } from 'rxjs';

interface ConfirmMailRequest {
  mail: string;
  token: string;
}

@Component({
  selector: 'app-mail-confirmation-page',
  templateUrl: './mail-confirmation.page.html',
  styleUrls: ['./mail-confirmation.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailConfirmationPage implements OnInit, OnDestroy {
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly http: HttpClient,
  ) {}

  ngOnInit(): void {
    const userId = this.route.snapshot.queryParams['userId'];
    const mail = this.route.snapshot.queryParams['mail'];
    const token = this.route.snapshot.queryParams['token'];

    const request: ConfirmMailRequest = {
      mail,
      token,
    };

    this.http
      .post<void>(`${environment.apiUrl}/users/${userId}/mail/confirm`, request)
      .pipe(
        catchError((e) => {
          this.router.navigateByUrl('/users/mail/confirmation/failed');
          return throwError(() => e);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.router.navigateByUrl('/users/mail/confirmation/success');
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
