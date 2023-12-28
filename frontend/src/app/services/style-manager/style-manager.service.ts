import { Injectable } from '@angular/core';

/*
Most code taken from https://github.com/angular/material.angular.io/blob/main/src/app/shared/style-manager/style-manager.ts
which is licensed under the MIT license.

License text:
-------------------------------------------------------------------------------
The MIT License

Copyright (c) 2021 Google LLC.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-------------------------------------------------------------------------------
 */

const getLinkElementForKey = (key: string) => {
  return getExistingLinkElementByKey(key) || createLinkElementWithKey(key);
};

const getExistingLinkElementByKey = (key: string) => {
  return document.head.querySelector(
    `link[rel="stylesheet"].${getClassNameForKey(key)}`,
  );
};

const createLinkElementWithKey = (key: string) => {
  const linkElement = document.createElement('link');

  linkElement.setAttribute('rel', 'stylesheet');
  linkElement.classList.add(getClassNameForKey(key));

  document.head.appendChild(linkElement);

  return linkElement;
};

const getClassNameForKey = (key: string) => {
  return `style-manager-${key}`;
};

@Injectable({
  providedIn: 'root',
})
export class StyleManagerService {
  setStyle(key: string, href: string) {
    getLinkElementForKey(key).setAttribute('href', href);
  }

  removeStyle(key: string) {
    const existingLinkElement = getExistingLinkElementByKey(key);
    if (existingLinkElement) {
      document.head.removeChild(existingLinkElement);
    }
  }
}
