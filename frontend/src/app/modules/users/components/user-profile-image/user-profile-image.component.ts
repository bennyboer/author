import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  HostBinding,
  HostListener,
  Input,
  OnDestroy,
  Output,
} from '@angular/core';
import { ReplaySubject, Subject, switchMap } from 'rxjs';
import { Option } from '../../../shared';
import { UsersService } from '../../store';

@Component({
  selector: 'app-user-profile-image',
  templateUrl: './user-profile-image.component.html',
  styleUrls: ['./user-profile-image.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfileImageComponent implements OnDestroy {
  @Input()
  set userId(userId: string | null) {
    Option.someOrNone(userId).ifSome((uId) => this.userId$.next(uId));
  }

  @Input()
  diameter: number = 64;

  @Input()
  editable: boolean = false;

  @Output()
  readonly clicked: EventEmitter<void> = new EventEmitter<void>();

  private readonly userId$: Subject<string> = new ReplaySubject<string>(1);
  readonly imageId$ = this.userId$.pipe(
    switchMap((userId) => this.usersService.getUserImageId(userId)),
  );

  constructor(private readonly usersService: UsersService) {}

  ngOnDestroy(): void {
    this.userId$.complete();
  }

  @HostBinding('class.editable')
  get isEditable(): boolean {
    return this.editable;
  }

  @HostBinding('style.width.px')
  get width(): number {
    return this.diameter;
  }

  @HostBinding('style.height.px')
  get height(): number {
    return this.diameter;
  }

  @HostListener('click')
  onClick(): void {
    if (this.editable) {
      this.clicked.emit();
    }
  }
}
