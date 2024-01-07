import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject, takeUntil } from 'rxjs';
import { StyleManagerService } from '../style-manager/style-manager.service';

export enum Theme {
  LIGHT = 'LIGHT',
  DARK = 'DARK',
}

const THEME_NAME_LOOKUP = {
  [Theme.LIGHT]: 'light',
  [Theme.DARK]: 'dark',
};

@Injectable()
export class ThemeService implements OnDestroy {
  private readonly theme$: BehaviorSubject<Theme> = new BehaviorSubject<Theme>(
    Theme.LIGHT,
  );
  private readonly destroy$: Subject<void> = new Subject<void>();

  private darkModeMediaQuery!: MediaQueryList;
  private darkModeEventListener!: (event: MediaQueryListEvent) => void;

  constructor(private readonly styleManager: StyleManagerService) {
    this.theme$
      .pipe(takeUntil(this.destroy$))
      .subscribe((theme) => this.applyTheme(theme));

    this.listenToSystemPreferences();
    this.applyTheme(this.theme$.value);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    this.deregisterSystemPreferencesListener();
  }

  getTheme(): Observable<Theme> {
    return this.theme$.asObservable();
  }

  setTheme(theme: Theme): void {
    this.theme$.next(theme);
  }

  private listenToSystemPreferences() {
    this.darkModeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    this.darkModeEventListener = (event: MediaQueryListEvent) => {
      if (event.matches) {
        this.setTheme(Theme.DARK);
      } else {
        this.setTheme(Theme.LIGHT);
      }
    };
    this.darkModeMediaQuery.addEventListener(
      'change',
      this.darkModeEventListener,
    );

    if (this.darkModeMediaQuery.matches) {
      this.setTheme(Theme.DARK);
    } else {
      this.setTheme(Theme.LIGHT);
    }
  }

  private deregisterSystemPreferencesListener() {
    this.darkModeMediaQuery.removeEventListener(
      'change',
      this.darkModeEventListener,
    );
  }

  private applyTheme(theme: Theme): void {
    this.removeTheme();

    this.styleManager.setStyle('theme', `${THEME_NAME_LOOKUP[theme]}.css`);
  }

  private removeTheme(): void {
    this.styleManager.removeStyle('theme');
  }
}
