import {Predicate} from '../../types/Types';
import {Maybe} from '../Maybe';

describe('Maybe', () => {

  const mapErrorMessage = 'Unable to get value of Type.nothing';

  describe('can create from null or undefined', () => {

    it('creates from null', () => {
      expect(Maybe.maybe(null).isNothing()).toBe(true);
    });

    it('creates from undefined', () => {
      expect(Maybe.maybe().isNothing()).toBe(true);
      expect(Maybe.maybe(undefined).isNothing()).toBe(true);
    });

    it('create type of nothing', () => {
      expect(Maybe.nothing().isNothing()).toBe(true);
    });
  });

  describe('creates with value and will be defined', () => {

    it('creates with a string value', () => {
      expect(Maybe.maybe<string>('something').isDefined()).toBe(true);
    });
  });

  describe('get', () => {

    it('gets defined value', () => {
      expect(Maybe.maybe(1).get()).toBe(1);
      expect(Maybe.maybe(0).get()).toBe(0);
      expect(Maybe.maybe(false).get()).toBe(false);
      expect(Maybe.maybe({id: 1}).get()).toEqual({id: 1});
    });

    it('throw when there is no value defined', () => {
      expect(() => Maybe.maybe(null).get()).toThrow(mapErrorMessage);
    });
  });

  describe('map', () => {

    it('will throw when mapping over type of nothing', () => {
      expect(Maybe.nothing<number>().map((n: number) => n + 1).isNothing()).toBe(true);
    });

    it('will map over value', () => {
      expect(Maybe.maybe(1).map((value: number) => value + 1).get()).toBe(2);
    });
  });

  describe('flatMap', () => {

    it('will flatten out Maybes', () => {
      const value = Maybe.just(42)
        .flatMap((value: number) => Maybe.just<string>('foo: ' + value))
        .get();
      expect(value).toBe('foo: 42');
    });

    it('does not flatten when not defined', () => {
      const nothing = Maybe.nothing()
        .flatMap((value: number) => Maybe.just<string>('foo: ' + value))
        .isNothing();
      expect(nothing).toBe(true);
    });
  });

  describe('orElse', () => {

    it('return the value when it is defined', () => {
      expect(Maybe.just(2).orElse(42)).toBe(2);
    });

    it('return the fallback value when there is no value', () => {
      expect(Maybe.nothing<number>().orElse(42)).toBe(42);
      expect(Maybe.maybe<number>(null).orElse(42)).toBe(42);
      expect(Maybe.maybe<number>(undefined).orElse(42)).toBe(42);
    });
  });

  describe('orElseGet', () => {

    it('return the value when it is defined', () => {
      expect(Maybe.just(2).orElseGet(() => 42)).toBe(2);
    });

    it('return the fallback value when there is no value', () => {
      expect(Maybe.nothing<number>().orElseGet(() => 42)).toBe(42);
      expect(Maybe.maybe<number>(null).orElseGet(() => 42)).toBe(42);
      expect(Maybe.maybe<number>(undefined).orElseGet(() => 42)).toBe(42);
    });
  });

  describe('filter', () => {
    const predicate: Predicate<number> = (value: number) => value > 40;

    it('includes everything that passes the predicate test', () => {
      expect(Maybe.just(42).filter(predicate).get()).toBe(42);
    });

    it('does not include if the predicate is not fulfilled', () => {
      expect(Maybe.just(1).filter(predicate).isNothing()).toBe(true);
    });

    it('does not include if the maybe is of type nothing', () => {
      expect(Maybe.nothing<number>().filter(predicate).isNothing()).toBe(true);
    });
  });

});
