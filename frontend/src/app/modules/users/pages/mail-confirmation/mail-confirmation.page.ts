import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, delay, Subject, takeUntil, throwError } from 'rxjs';
import { RemoteUsersService } from '../../store';

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
    private readonly remoteUsersService: RemoteUsersService,
  ) {}

  ngOnInit(): void {
    const userId = this.route.snapshot.params['userId'];
    const mail = this.route.snapshot.queryParams['mail'];
    const token = this.route.snapshot.queryParams['token'];

    this.remoteUsersService
      .confirmMail(userId, mail, token)
      .pipe(
        delay(1000), // Not too fast
        catchError((e) => {
          this.router.navigate(['./failed'], {
            relativeTo: this.route,
          });
          return throwError(() => e);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.router.navigate(['./success'], {
          relativeTo: this.route,
        });
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
