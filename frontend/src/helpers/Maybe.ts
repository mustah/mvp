import {Predicate} from '../types/Types';

const enum Type {
  nothing,
  just,
}

interface MaybeApi<T> {
  isNothing: () => boolean;
  isJust: () => boolean;
  do: <U>(f: (value: T) => void) => void;
  map: <U>(f: (value: T) => U) => Maybe<U>;
  flatMap: <U>(f: (value: T) => Maybe<U>) => Maybe<U>;
  get: () => T;
  orElse: (value: T) => T;
  orElseGet: (f: () => T) => T;
  filter: (predicate: Predicate<T>) => Maybe<T>;
}

export class Maybe<T> implements MaybeApi<T> {

  static maybe<T>(value?: T | null): Maybe<T> {
    return value === null || value === undefined
      ? new Maybe<T>(Type.nothing)
      : new Maybe<T>(Type.just, value);
  }

  static just<T>(value: T): Maybe<T> {
    return new Maybe<T>(Type.just, value);
  }

  static nothing<T>(): Maybe<T> {
    return new Maybe<T>(Type.nothing);
  }

  private constructor(private readonly type: Type, private readonly value?: T) {
    if (type === Type.nothing && value !== undefined) {
      throw new Error('Maybe cannot be of type nothing and have a value!');
    }
  }

  isNothing(): boolean {
    return this.type === Type.nothing;
  }

  isJust(): boolean {
    return this.type === Type.just;
  }

  do(f: (value: T) => void): void {
    if (this.isJust()) {
      f(this.get());
    }
  }

  map<U>(f: (value: T) => U): Maybe<U> {
    return this.isJust()
      ? Maybe.maybe<U>(f(this.get()))
      : Maybe.nothing<U>();
  }

  flatMap<U>(f: (value: T) => Maybe<U>): Maybe<U> {
    return this.isJust()
      ? f(this.get())
      : Maybe.nothing<U>();
  }

  get(): T {
    if (this.type === Type.nothing) {
      throw new Error('Unable to get value of Type.nothing');
    }
    return this.value!;
  }

  orElse(value: T): T {
    return this.isJust() ? this.get() : value;
  }

  getOrElseUndefined(): T | undefined {
    return this.isJust() ? this.get() : undefined;
  }

  getOrElseNull(): T | null {
    return this.isJust() ? this.get() : null;
  }

  orElseGet(f: () => T): T {
    return this.isJust() ? this.get() : f();
  }

  filter(predicate: Predicate<T>): Maybe<T> {
    return this.isJust() && predicate(this.get())
      ? this
      : Maybe.nothing<T>();
  }
}
