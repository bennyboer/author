import {ChangeDetectionStrategy, Component, ElementRef, HostListener,} from "@angular/core";
import {map, Observable, tap} from "rxjs";
import {Theme, ThemeService} from "../../services";

/**
 * Dark mode toggle component.
 * Most code is taken from
 * https://github.com/Disimasa/svelte-dark-mode-toggle/tree/2e184f0e697b5000dba7fcad0c1948e3e1a25abb
 * which is licensed under the MIT license at the time of writing.
 *
 * License text taken from the repository:
 * -----------------------------------------------------------------------------
 * MIT License
 *
 * Copyright (c) 2023 Disimasa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * -----------------------------------------------------------------------------
 */
@Component({
  selector: 'app-dark-mode-toggle',
  templateUrl: './dark-mode-toggle.component.html',
  styleUrls: ['./dark-mode-toggle.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DarkModeToggleComponent {
  private isDarkMode: boolean = false;

  readonly isDarkMode$: Observable<boolean> = this.themeService.getTheme().pipe(
    map((theme) => theme === Theme.DARK),
    tap((isDarkMode) => {
      this.isDarkMode = isDarkMode;

      const element: HTMLElement = this.elementRef.nativeElement;
      if (isDarkMode) {
        element.classList.add('dark-mode');
      } else {
        element.classList.remove('dark-mode');
      }
    }),
  );

  constructor(
    private readonly themeService: ThemeService,
    private readonly elementRef: ElementRef,
  ) {}

  @HostListener('click')
  toggleDarkMode(): void {
    this.themeService.setTheme(this.isDarkMode ? Theme.LIGHT : Theme.DARK);
  }
}
