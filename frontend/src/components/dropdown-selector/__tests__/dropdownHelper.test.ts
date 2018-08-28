import {SelectionListItem} from '../../../state/user-selection/userSelectionModels';
import {toIdNamed} from '../../../types/Types';
import {replaceWhereId} from '../dropdownHelper';

describe('dropdownHelper', () => {

  describe('replaceWhereId', () => {
    const item1: SelectionListItem = {...toIdNamed('a'), selected: true};
    const item2: SelectionListItem = {...toIdNamed('b'), selected: false};
    const item3: SelectionListItem = {...toIdNamed('d'), selected: false};
    const newItem: SelectionListItem = {...toIdNamed('c'), selected: false};

    it('replace first in the lists', () => {
      const array: SelectionListItem[] = [item1, item2];

      const actual: SelectionListItem[] = replaceWhereId(array, newItem, 'a');

      expect(actual).toEqual([newItem, item2]);
    });

    it('replaces at the end of the list', () => {
      const array: SelectionListItem[] = [item1, item2, item3];

      const actual: SelectionListItem[] = replaceWhereId(array, newItem, 'd');

      expect(actual).toEqual([item1, item2, newItem]);
    });
  });
});
