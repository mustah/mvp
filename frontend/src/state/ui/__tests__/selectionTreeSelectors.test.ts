import {SelectionTreeState} from '../selection-tree/selectionTreeModels';
import {getOpenListItems} from '../selection-tree/selectionTreeSelectors';

describe('selectionTreeSelectors', () => {

  it('can handle an empty selection', () => {
    const selectionTree: SelectionTreeState = {
      openListItems: [],
    };
    const openItems = getOpenListItems(selectionTree);
    expect(openItems.size).toBe(0);
  });

  it('can keep track of opened items', () => {
    const selectionTree: SelectionTreeState = {
      openListItems: [1, 2, 3, 4],
    };
    const openItems = getOpenListItems(selectionTree);
    expect(openItems.size).toBe(4);
  });

  it('can handle duplicated selected items', () => {
    const selectionTree: SelectionTreeState = {
      openListItems: [1, 2, 4, 4, 4, 4],
    };
    const openItems = getOpenListItems(selectionTree);
    expect(openItems.size).toBe(3);
  });

});
