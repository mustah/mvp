import {addSelectedIds} from '../selectionTreeActions';
import {selectionTree} from '../selectionTreeReducer';

describe('selectionTreeReducer', () => {

  it('just adds to open items array', () => {
    const state = selectionTree(null!, addSelectedIds([1, 2, 3]));

    expect(state).toEqual({openListItems: [1, 2, 3]});
  });

  it('will replace previous selected item with the new ones', () => {
    let state = selectionTree(null!, addSelectedIds([1, 2, 3]));

    state = selectionTree(state, addSelectedIds([42]));

    expect(state).toEqual({openListItems: [42]});
  });

  it('can add empty list of ids', () => {
    let state = selectionTree(null!, addSelectedIds([1, 2, 3]));

    state = selectionTree(state, addSelectedIds([]));

    expect(state).toEqual({openListItems: []});
  });
});
