export class CanvasResizer {
  public static resizeCanvas(
    element: HTMLCanvasElement,
    width: number,
    height: number,
    devicePixelRatio?: number,
  ): void {
    /*
    We honor window.devicePixelRatio here to support high-DPI screens.
    To support High-DPI screens we will set the canvas element size twice:
      1. As style: width and height will be the same as in the container element bounds
      2. As attributes to the HTML canvas element: width and height need to be multiplied by
         window.devicePixelRatio (for example 2.0 for most SmartPhones and 4K screens).

    If we don't do this the canvas content will be rendered blurry on High-DPI screens/devices.
     */

    if (devicePixelRatio === undefined || devicePixelRatio === null) {
      devicePixelRatio = window.devicePixelRatio;
    }

    element.width = width * devicePixelRatio;
    element.height = height * devicePixelRatio;

    element.style.width = `${width}px`;
    element.style.height = `${height}px`;
  }
}
