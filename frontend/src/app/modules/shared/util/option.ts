export class Option<T> {
  private readonly value: T | null;

  private constructor(value: T | null) {
    this.value = value;
  }

  static some<T>(value: T): Option<T> {
    if (value === null || value === undefined) {
      throw new Error(
        'Expected value to be non-null and non-undefined, but got null or undefined',
      );
    }

    return new Option<T>(value);
  }

  static none<T>(): Option<T> {
    return new Option<T>(null);
  }

  static someOrNone<T>(value: T | null | undefined): Option<T> {
    if (value === null || value === undefined) {
      return Option.none<T>();
    }

    return Option.some(value);
  }

  isSome(): boolean {
    return this.value !== null;
  }

  isNone(): boolean {
    return !this.isSome();
  }

  map<U>(mapper: (value: T) => U): Option<U> {
    if (this.isNone()) {
      return Option.none<U>();
    }

    return Option.someOrNone(mapper(this.orElseThrow()));
  }

  flatMap<U>(mapper: (value: T) => Option<U>): Option<U> {
    if (this.isNone()) {
      return Option.none<U>();
    }

    return mapper(this.orElseThrow());
  }

  filter(predicate: (value: T) => boolean): Option<T> {
    if (this.isNone()) {
      return Option.none<T>();
    }

    if (predicate(this.orElseThrow())) {
      return Option.some(this.orElseThrow());
    }

    return Option.none<T>();
  }

  ifSome(consumer: (value: T) => void): void {
    if (this.isSome()) {
      consumer(this.orElseThrow());
    }
  }

  orElse(other: T): T {
    if (this.isNone()) {
      return other;
    }

    return this.orElseThrow();
  }

  orElseGet(other: () => T): T {
    if (this.isNone()) {
      return other();
    }

    return this.orElseThrow();
  }

  orElseThrow(): T {
    if (this.isNone()) {
      throw new Error('Expected value to be non-null and non-undefined');
    }

    return this.value as T;
  }

  or(option: Option<T>): Option<T> {
    if (this.isSome()) {
      return this;
    }

    return option;
  }

  equals(option: Option<T>): boolean {
    if (this.isNone() && option.isNone()) {
      return true;
    }

    if (this.isSome() && option.isSome()) {
      return this.orElseThrow() === option.orElseThrow();
    }

    return false;
  }
}
