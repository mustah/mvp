import {ObjectsById} from '../../state/domain-models/domainModels';
import {Identifiable} from '../../types/Types';
import {groupById, removeAtIndex, toggle} from '../collections';

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

  describe('groupById', () => {

    it('handles an empty list', () => {
      const actual: ObjectsById<Identifiable> = groupById([]);
      const expected: ObjectsById<Identifiable> = {};
      expect(actual).toEqual(expected);
    });

    it('groups by id for non-empty list', () => {
      const actual: ObjectsById<Identifiable> = groupById([{id: 3}, {id: 4}]);
      const expected: ObjectsById<Identifiable> = {3: {id: 3}, 4: {id: 4}};
      expect(actual).toEqual(expected);
    });

  });

  describe('removeAtIndex', () => {

    it('does not remove from index -1', () => {
      const array = [1, 2, 3];
      expect(removeAtIndex(array, -1)).toEqual(array);
    });

    it('does not remove when index is out of bounds', () => {
      const array = [1, 2, 3];
      expect(removeAtIndex(array, 10)).toEqual(array);
    });

    it('removes at index', () => {
      const array = [1, 2, 3];
      expect(removeAtIndex(array, 1)).toEqual([1, 3]);
    });

    it('removes last item', () => {
      const array = [1, 2, 3];
      expect(removeAtIndex(array, 2)).toEqual([1, 2]);
    });

    it('removes first item', () => {
      const array = [1, 2, 3];
      expect(removeAtIndex(array, 0)).toEqual([2, 3]);
    });
  });

});
