import {toggle} from '../collections';

describe('collections', () => {

  describe('toggle', () => {

    it('adds element if not present', () => {
      const actual: number[] = toggle(1, [2, 3]);
      const expected: number[] = [2, 3, 1];
      expect(actual).toEqual(expected);
    });

    it('removes element if present', () => {
      const actual: number[] = toggle(1, [1, 2, 3]);
      const expected: number[] = [2, 3];
      expect(actual).toEqual(expected);
    });

    it('removes duplicates, as implementation detail', () => {
      const actual: number[] = toggle(1, [1, 2, 2, 2, 2, 3]);
      const expected: number[] = [2, 3];
      expect(actual).toEqual(expected);
    });

  });

});
