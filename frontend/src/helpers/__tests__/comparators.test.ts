import {SelectionListItem} from '../../state/user-selection/userSelectionModels';
import {IdNamed} from '../../types/Types';
import {
  byNameAsc,
  selectedFirstThenUnknownByNameAsc,
  unknownNameFirstThenByNameAsc,
} from '../comparators';

describe('comparators', () => {

  describe('byNameAsc', () => {

    it('sort empty list', () => {
      expect([].sort(byNameAsc)).toEqual([]);
    });

    it('sort single item in the list', () => {
      const item: IdNamed = {id: 1, name: 'a'};

      expect([item].sort(byNameAsc)).toEqual([item]);
    });

    it('sorts two items in the list', () => {
      const item1: IdNamed = {id: 1, name: 'a'};
      const item2: IdNamed = {id: 2, name: 'b'};

      expect([item2, item1].sort(byNameAsc)).toEqual([item1, item2]);
    });

    it('sorts the list of items', () => {
      const item1: IdNamed = {id: 1, name: 'a'};
      const item2: IdNamed = {id: 2, name: 'b'};
      const item3: IdNamed = {id: 3, name: 'c'};
      const item4: IdNamed = {id: 4, name: 'd'};

      expect([item4, item2, item3, item1].sort(byNameAsc))
        .toEqual([item1, item2, item3, item4]);
    });

    it('puts the first found item with same name first', () => {
      const item1: IdNamed = {id: 1, name: 'a'};
      const item2: IdNamed = {id: 2, name: 'b'};
      const item3: IdNamed = {id: 3, name: 'ddd'};
      const item4: IdNamed = {id: 4, name: 'ddd'};

      expect([item4, item2, item3, item1].sort(byNameAsc))
        .toEqual([item1, item2, item4, item3]);

      expect([item3, item2, item4, item1].sort(byNameAsc))
        .toEqual([item1, item2, item3, item4]);
    });

  });

  describe('unknownNameFirstThenByNameAsc', () => {
    it('sort empty list', () => {
      expect([].sort(unknownNameFirstThenByNameAsc)).toEqual([]);
    });

    it('sort single item in the list', () => {
      const item: IdNamed = {id: 1, name: 'a'};

      expect([item].sort(unknownNameFirstThenByNameAsc)).toEqual([item]);
    });

    it('puts unknown name first in the list', () => {
      const item1: IdNamed = {id: 1, name: 'a'};
      const item2: IdNamed = {id: 2, name: 'unknown'};

      expect([item1, item2].sort(unknownNameFirstThenByNameAsc)).toEqual([item2, item1]);
    });

    it('puts all unknown names first in the list', () => {
      const item1: IdNamed = {id: 1, name: 'unknown'};
      const item2: IdNamed = {id: 2, name: 'a'};
      const item3: IdNamed = {id: 3, name: 'b'};
      const item4: IdNamed = {id: 4, name: 'unknown'};

      expect([item1, item2, item3, item4].sort(unknownNameFirstThenByNameAsc))
        .toEqual([item1, item4, item2, item3]);
    });

    it('sorts the list by name ascending when no items have name unknown', () => {
      const item1: IdNamed = {id: 1, name: 'a'};
      const item2: IdNamed = {id: 2, name: 'b'};
      const item3: IdNamed = {id: 3, name: 'c'};
      const item4: IdNamed = {id: 4, name: 'd'};

      expect([item4, item2, item3, item1].sort(unknownNameFirstThenByNameAsc))
        .toEqual([item1, item2, item3, item4]);
    });

  });

  describe('selectedFirstThenUnknownByNameAsc', () => {

    it('puts selected items first in the list', () => {
      const item1: SelectionListItem = {id: 1, name: 'a', selected: false};
      const item2: SelectionListItem = {id: 2, name: 'b', selected: true};

      expect([item1, item2].sort(selectedFirstThenUnknownByNameAsc)).toEqual([item2, item1]);
    });

    it('puts selected items first in the list', () => {
      const item1: SelectionListItem = {id: 1, name: 'a', selected: true};
      const item2: SelectionListItem = {id: 2, name: 'b', selected: false};
      const item3: SelectionListItem = {id: 3, name: 'c', selected: true};

      expect([item1, item2, item3].sort(selectedFirstThenUnknownByNameAsc)).toEqual([item1, item3, item2]);
    });

    it('sorts by name only if all items are not selected', () => {
      const item1: SelectionListItem = {id: 1, name: 'a', selected: false};
      const item2: SelectionListItem = {id: 2, name: 'b', selected: false};
      const item3: SelectionListItem = {id: 3, name: 'c', selected: false};

      expect([item2, item1, item3].sort(selectedFirstThenUnknownByNameAsc)).toEqual([item1, item2, item3]);
    });

    it('puts selected items first, then unknown, and last sort by name ascending', () => {
      const item1: SelectionListItem = {id: 1, name: 'a', selected: false};
      const item2: SelectionListItem = {id: 2, name: 'b', selected: true};
      const item3: SelectionListItem = {id: 3, name: 'c', selected: false};
      const item4: SelectionListItem = {id: 3, name: 'unknown', selected: false};
      const item5: SelectionListItem = {id: 3, name: 'd', selected: false};

      expect([item1, item2, item3, item4, item5].sort(selectedFirstThenUnknownByNameAsc))
        .toEqual([item2, item4, item1, item3, item5]);
    });
  });

});
