import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  Renderer2,
} from '@angular/core';
import { Anchor, Bounds, CanvasResizer, Option } from '../../util';
import { asyncScheduler, Subject, takeUntil, throttleTime } from 'rxjs';

export type RenderingRoutine = (
  ctx: CanvasRenderingContext2D,
  viewport: Bounds,
) => void;
export interface CanvasListenerResult {
  consumed: boolean;
}
export type CanvasMouseListener = (
  event: CanvasMouseEvent,
) => CanvasListenerResult;

export interface CanvasMouseEvent {
  event: MouseEvent;
  anchor: Anchor;
  isOnCanvas: boolean;
}

type MouseEventListener = (event: MouseEvent) => void;
type MouseWheelListener = (event: WheelEvent) => void;

const RENDERING_THROTTLE_MILLIS = 10;

@Component({
  selector: 'app-canvas',
  templateUrl: './canvas.component.html',
  styleUrls: ['./canvas.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CanvasComponent implements AfterViewInit, OnDestroy {
  @Input()
  set renderingRoutine(value: RenderingRoutine) {
    this.renderingConsumer = value;
    this.scheduleRepaint();
  }

  @Input()
  set mouseMove(value: CanvasMouseListener) {
    this.canvasMouseMoveListener = value;
  }

  @Input()
  set mouseDown(value: CanvasMouseListener) {
    this.canvasMouseDownListener = value;
  }

  @Input()
  set mouseUp(value: CanvasMouseListener) {
    this.canvasMouseUpListener = value;
  }

  @Input()
  set zoom(value: number) {
    if (value <= 0.0) {
      return;
    }

    if (value === this.zoomFactor) {
      return;
    }

    this.zoomFactor = value;
    this.scheduleRepaint();
  }

  @Input()
  zooming: boolean = false;

  @Input()
  panning: boolean = false;

  @Input()
  debug: boolean = false;

  private renderingConsumer: RenderingRoutine = () => {};
  private canvasMouseDownListener: CanvasMouseListener = () => ({
    consumed: false,
  });
  private canvasMouseMoveListener: CanvasMouseListener = () => ({
    consumed: false,
  });
  private canvasMouseUpListener: CanvasMouseListener = () => ({
    consumed: false,
  });

  private container: Option<HTMLElement> = Option.none();
  private canvas: Option<HTMLCanvasElement> = Option.none();
  private ctx: Option<CanvasRenderingContext2D> = Option.none();

  private mouseDownListener: Option<MouseEventListener> = Option.none();
  private mouseMoveListener: Option<MouseEventListener> = Option.none();
  private mouseUpListener: Option<MouseEventListener> = Option.none();
  private mouseWheelListener: Option<MouseWheelListener> = Option.none();

  private zoomFactor: number = 1.0;

  private panStart: Option<Anchor> = Option.none();
  private panMovement: Option<Anchor> = Option.none();
  private translation: Anchor = Anchor.zero();

  private resizeObserver: Option<ResizeObserver> = Option.none();
  private readonly resizeThrottleEvents$: Subject<void> = new Subject<void>();

  private requestAnimationFrameId: Option<number> = Option.none();
  private readonly repaintEvents$: Subject<void> = new Subject<void>();
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
  ) {}

  get zoom(): number {
    return this.zoomFactor;
  }

  ngAfterViewInit() {
    const container = this.createCanvasContainer();
    const canvas = this.createCanvas(container);

    this.setupCanvasResizing(container, canvas);
    this.setupRepainting();
    this.setupMouseListeners(container);

    this.scheduleRepaint();
  }

  ngOnDestroy() {
    this.destroyCanvasResizing();
    this.destroyRepainting();
    this.destroyMouseListeners();

    this.destroy$.next();
    this.destroy$.complete();
  }

  scheduleRepaint() {
    this.repaintEvents$.next();
  }

  private createCanvasContainer(): HTMLElement {
    const container = this.renderer.createElement('div');

    container.style.position = 'relative';
    container.style.width = '100%';
    container.style.height = '100%';

    this.elementRef.nativeElement.appendChild(container);
    this.container = Option.some(container);

    return container;
  }

  private createCanvas(container: HTMLElement): HTMLCanvasElement {
    const canvas = this.renderer.createElement('canvas');

    // Set position absolute to prevent resize events to occur due to canvas element resizing
    canvas.style.position = 'absolute';

    this.renderer.appendChild(container, canvas);

    this.canvas = Option.some(canvas);
    this.ctx = Option.some(canvas.getContext('2d'));

    return canvas;
  }

  private setupCanvasResizing(
    container: HTMLElement,
    canvas: HTMLCanvasElement,
  ) {
    const bounds: DOMRect = container.getBoundingClientRect();

    CanvasResizer.resizeCanvas(canvas, bounds.width, bounds.height);

    const resizeObserver = new ResizeObserver(() => {
      this.resizeThrottleEvents$.next();
    });
    resizeObserver.observe(container);
    this.resizeObserver = Option.some(resizeObserver);

    this.resizeThrottleEvents$
      .asObservable()
      .pipe(
        throttleTime(RENDERING_THROTTLE_MILLIS, asyncScheduler, {
          leading: false,
          trailing: true,
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.resizeCanvas());
  }

  private resizeCanvas() {
    this.canvas.ifSome((canvas) => {
      const bounds: DOMRect = this.container
        .orElseThrow()
        .getBoundingClientRect();

      CanvasResizer.resizeCanvas(canvas, bounds.width, bounds.height);

      this.scheduleRepaint();
    });
  }

  private destroyCanvasResizing() {
    this.resizeThrottleEvents$.complete();
    this.resizeObserver.ifSome((observer) => observer.disconnect());
  }

  private setupRepainting() {
    this.repaintEvents$
      .asObservable()
      .pipe(
        throttleTime(RENDERING_THROTTLE_MILLIS, asyncScheduler, {
          leading: false,
          trailing: true,
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.repaintOnNextAnimationFrame());
  }

  private destroyRepainting() {
    this.repaintEvents$.complete();
  }

  private setupMouseListeners(container: HTMLElement) {
    const mouseUpListener: MouseEventListener = (event) =>
      this.onMouseUp(event);
    const mouseMoveListener: MouseEventListener = (event) =>
      this.onMouseMove(event);
    const mouseDownListener: MouseEventListener = (event) =>
      this.onMouseDown(event);
    const mouseWheelListener: MouseWheelListener = (event) =>
      this.onMouseWheel(event);

    window.addEventListener('mouseup', mouseUpListener);
    window.addEventListener('mousemove', mouseMoveListener);
    container.addEventListener('mousedown', mouseDownListener);
    container.addEventListener('wheel', mouseWheelListener);
  }

  private destroyMouseListeners() {
    this.mouseUpListener.ifSome((listener) =>
      window.removeEventListener('mouseup', listener),
    );
    this.mouseMoveListener.ifSome((listener) =>
      window.removeEventListener('mousemove', listener),
    );
    this.mouseDownListener.ifSome((listener) =>
      this.container.ifSome((container) =>
        container.removeEventListener('mousedown', listener),
      ),
    );
    this.mouseWheelListener.ifSome((listener) =>
      this.container.ifSome((container) =>
        container.removeEventListener('wheel', listener),
      ),
    );
  }

  private onMouseUp(event: MouseEvent) {
    const { consumed } = this.canvasMouseUpListener(
      this.toCanvasMouseEvent(event),
    );
    if (consumed) {
      return;
    }

    if (this.isPanning()) {
      this.updatePanningTranslation(event);

      this.panStart = Option.none();
      this.panMovement = Option.none();
    }
  }

  private onMouseMove(event: MouseEvent) {
    if (this.isPanning()) {
      this.updatePanningTranslation(event);
      return;
    }

    this.canvasMouseMoveListener(this.toCanvasMouseEvent(event));
  }

  private onMouseDown(event: MouseEvent) {
    const { consumed } = this.canvasMouseDownListener(
      this.toCanvasMouseEvent(event),
    );
    if (consumed) {
      return;
    }

    if (this.panning) {
      this.panStart = Option.some(this.getMouseOffset(event));
      this.panMovement = Option.some(Anchor.zero());
    }
  }

  private onMouseWheel(event: WheelEvent) {
    if (this.zooming) {
      event.preventDefault();

      const oldMouseOffset = this.getMouseOffset(event);

      const zoomAmount = this.calculateZoomAmount(this.zoomFactor);
      this.zoom =
        this.zoomFactor + (event.deltaY > 0 ? -zoomAmount : zoomAmount);
      const newMouseOffset = this.getMouseOffset(event);

      this.translation = this.translation.translate(
        newMouseOffset.x - oldMouseOffset.x,
        newMouseOffset.y - oldMouseOffset.y,
      );
    }
  }

  private calculateZoomAmount(zoomFactor: number) {
    /*
    Depending on the current [zoomFactor] we want the zooming amount to be more or less aggressive.
    For example when zooming in a lot, a constant zoom amount would lead to a very slow zooming experience,
    while contrary when zooming out a lot, a constant zoom amount would lead to a very fast zooming experience.
    We start with a constant zoom amount of 0.2 when the zoom factor is 1.0, increasing it when the zoom factor
    increases and decreasing it when it decreases.
     */

    const baseZoomFactor = 0.2;
    return baseZoomFactor * zoomFactor;
  }

  private updatePanningTranslation(event: MouseEvent) {
    this.panStart.ifSome((panStart) => {
      const offset = this.getMouseOffset(event);
      const dx = offset.x - panStart.x;
      const dy = offset.y - panStart.y;

      const movement = new Anchor({ x: dx, y: dy });

      this.panMovement.ifSome((panMovement) => {
        this.translation = this.translation.translate(
          movement.x - panMovement.x,
          movement.y - panMovement.y,
        );
      });
      this.panMovement = Option.some(movement);

      this.scheduleRepaint();
    });
  }

  private toCanvasMouseEvent(event: MouseEvent): CanvasMouseEvent {
    const target = event.target;
    const isOnCanvas = this.container
      .map((container) => container.contains(target as Node))
      .orElse(false);

    return {
      event,
      anchor: this.getMousePositionInCanvas(event),
      isOnCanvas,
    };
  }

  private getMousePositionInCanvas(event: MouseEvent): Anchor {
    const offset = this.getMouseOffset(event);
    const translation = this.translation;

    return new Anchor({
      x: offset.x - translation.x,
      y: offset.y - translation.y,
    });
  }

  private getMouseOffset(event: MouseEvent | Touch): Anchor {
    return this.canvas
      .map((canvas) => canvas.getBoundingClientRect())
      .map(
        (bounds) =>
          new Anchor({
            x: (event.clientX - bounds.left) / this.zoomFactor,
            y: (event.clientY - bounds.top) / this.zoomFactor,
          }),
      )
      .orElse(Anchor.zero());
  }

  private isPanning(): boolean {
    return this.panning && this.panStart.isSome();
  }

  private getViewPort(): Bounds {
    return this.canvas
      .map((canvas) => {
        const width = canvas.width / this.zoomFactor;
        const height = canvas.height / this.zoomFactor;
        const top = -this.translation.y;
        const left = -this.translation.x;

        return new Bounds({
          top,
          left,
          width,
          height,
        });
      })
      .orElse(Bounds.zero());
  }

  private repaintOnNextAnimationFrame() {
    this.requestAnimationFrameId.ifSome((id) =>
      window.cancelAnimationFrame(id),
    );

    const id = window.requestAnimationFrame(() => {
      this.requestAnimationFrameId = Option.none();
      this.repaint();
    });
    this.requestAnimationFrameId = Option.some(id);
  }

  private repaint() {
    const startTime = performance.now();

    this.ctx.ifSome((ctx) => {
      ctx.restore();
      ctx.save();

      ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

      const zoom = devicePixelRatio * this.zoomFactor;
      ctx.scale(zoom, zoom);

      ctx.translate(this.translation.x, this.translation.y);

      this.renderingConsumer(ctx, this.getViewPort());
    });

    if (this.debug) {
      const endTime = performance.now();
      const duration = endTime - startTime;
      console.debug(`Repaint took ${duration}ms`);
    }
  }
}
