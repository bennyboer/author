import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Option } from '../../modules/shared';

export class Navigation {
  readonly backLink: Option<string>;
  readonly relativeToRoute: ActivatedRoute;
  readonly label: string;
  readonly items: NavigationItem[];

  constructor(props: {
    relativeToRoute: ActivatedRoute;
    backLink: Option<string>;
    label: string;
    items: NavigationItem[];
  }) {
    this.relativeToRoute = props.relativeToRoute;
    this.backLink = props.backLink;
    this.label = props.label;
    this.items = props.items;
  }
}

export class NavigationItem {
  readonly label: string;
  readonly link: string;

  constructor(props: { label: string; link: string }) {
    this.label = props.label;
    this.link = props.link;
  }
}

@Injectable()
export class NavigationService {
  private readonly navigations: Navigation[] = [];
  private readonly currentNavigation$: BehaviorSubject<Navigation | null> =
    new BehaviorSubject<Navigation | null>(null);

  constructor(private readonly router: Router) {}

  pushNavigation(
    relativeToRoute: ActivatedRoute,
    backLink: Option<string>,
    label: string,
    items: NavigationItem[],
  ): void {
    const navigation = new Navigation({
      relativeToRoute,
      backLink,
      label,
      items,
    });

    this.navigations.push(navigation);
    this.currentNavigation$.next(navigation);
  }

  popNavigation(): void {
    this.navigations.pop();
    this.currentNavigation$.next(
      this.navigations.length > 0
        ? this.navigations[this.navigations.length - 1]
        : null,
    );
  }

  getCurrentNavigation(): Observable<Option<Navigation>> {
    return this.currentNavigation$
      .asObservable()
      .pipe(map((nav) => Option.someOrNone(nav)));
  }

  navigateBack(): void {
    const currentNavigation = this.currentNavigation$.value;
    Option.someOrNone(currentNavigation).ifSome((nav) => {
      nav.backLink.ifSome((link) =>
        this.router.navigate([link], {
          relativeTo: nav.relativeToRoute,
        }),
      );
    });
  }
}
