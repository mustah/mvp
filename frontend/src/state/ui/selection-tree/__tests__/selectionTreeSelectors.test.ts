import {SelectionTreeUiState} from '../selectionTreeModels';
import {getOpenListItems} from '../selectionTreeSelectors';

describe('selectionTreeSelectors', () => {

  it('can handle an empty selection', () => {
    const selectionTree: SelectionTreeUiState = {
      openListItems: [],
    };

    expect(getOpenListItems(selectionTree).size).toBe(0);
  });

  it('can keep track of opened items', () => {
    const selectionTree: SelectionTreeUiState = {
      openListItems: [1, 2, 3, 4],
    };

    expect(getOpenListItems(selectionTree).size).toBe(4);
  });

  it('can handle duplicated selected items', () => {
    const selectionTree: SelectionTreeUiState = {
      openListItems: [1, 2, 4, 4, 4, 4],
    };

    expect(getOpenListItems(selectionTree).size).toBe(3);
  });

});
