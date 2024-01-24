import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
} from '@angular/core';
import { AssetsService } from '../../assets.service';
import {
  BehaviorSubject,
  distinctUntilChanged,
  ReplaySubject,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { Option } from '../../../util';

@Component({
  selector: 'app-asset',
  templateUrl: './asset.component.html',
  styleUrls: ['./asset.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AssetComponent implements OnDestroy {
  @Input()
  width: number = 64;

  @Input()
  height: number = 64;

  @Input()
  set assetId(assetId: string) {
    Option.someOrNone(assetId).ifSome((assetId) => this.assetId$.next(assetId));
  }

  private readonly assetId$: Subject<string> = new ReplaySubject<string>(1);
  readonly asset$ = this.assetId$.pipe(
    distinctUntilChanged(),
    tap(() => this.loading$.next(true)),
    switchMap((assetId) => this.assetsService.fetch(assetId)),
    tap(() => this.loading$.next(false)),
  );
  readonly loading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    true,
  );

  constructor(private readonly assetsService: AssetsService) {}

  ngOnDestroy(): void {
    this.assetId$.complete();
    this.loading$.complete();
  }
}
