import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
} from '@angular/core';
import {
  animationFrameScheduler,
  BehaviorSubject,
  fromEvent,
  map,
  Observable,
  Subject,
  takeUntil,
  throttleTime,
} from 'rxjs';
import { NavigationService } from '../../services';
import { ActivatedRoute, Router } from '@angular/router';

interface DisplayableNavigationItem {
  readonly label: string;
  readonly link: string;
  readonly relativeToRoute: ActivatedRoute;
}

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavigationComponent implements AfterViewInit, OnDestroy {
  overflow$: Subject<boolean> = new BehaviorSubject<boolean>(false);

  label$: Observable<string> = this.navigationService
    .getCurrentNavigation()
    .pipe(map((navigation) => navigation.map((nav) => nav.label).orElse('')));

  canNavigateBack$: Observable<boolean> = this.navigationService
    .getCurrentNavigation()
    .pipe(
      map((navigation) =>
        navigation.map((nav) => nav.backLink.isSome()).orElse(false),
      ),
    );

  items$: Observable<DisplayableNavigationItem[]> = this.navigationService
    .getCurrentNavigation()
    .pipe(
      map((navigation) =>
        navigation
          .map((nav) =>
            nav.items.map((item) => ({
              label: item.label,
              link: item.link,
              relativeToRoute: nav.relativeToRoute,
            })),
          )
          .orElse([]),
      ),
    );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly navigationService: NavigationService,
    private readonly router: Router,
    private readonly elementRef: ElementRef,
  ) {}

  ngAfterViewInit(): void {
    fromEvent(window, 'resize')
      .pipe(throttleTime(0, animationFrameScheduler), takeUntil(this.destroy$))
      .subscribe(() => this.checkForOverflow());

    setTimeout(() => this.checkForOverflow(), 0);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  navigateToItem(item: DisplayableNavigationItem): void {
    this.router.navigate([item.link], { relativeTo: item.relativeToRoute });
  }

  navigateBack(): void {
    this.navigationService.navigateBack();
  }

  private checkForOverflow(): void {
    const element: HTMLElement = this.elementRef.nativeElement;
    const isOverflowing = element.scrollWidth > element.clientWidth;
    this.overflow$.next(isOverflowing);
  }
}
