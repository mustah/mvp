import {ObjectsById} from '../../state/domain-models/domainModels';
import {Identifiable} from '../../types/Types';
import {groupById, toggle} from '../collections';

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

});
