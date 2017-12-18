import {Action, uuid} from '../../../../types/Types';
import {SELECTION_TREE_TOGGLE_ENTRY} from '../selectionTreeActions';
import {SelectionTreeState} from '../selectionTreeModels';
import {selectionTree} from '../selectionTreeReducer';

describe('selectionTreeReducer', () => {
  it('makes sure the selectedListItems is set to payload', () => {
    const state: SelectionTreeState = {openListItems: [4, 5]};
    const payload: uuid[] = [1, 2, 3];
    const action: Action<uuid[]> =  {type: SELECTION_TREE_TOGGLE_ENTRY, payload};

    expect(selectionTree(state, action)).toEqual({
      ...state,
      openListItems: payload,
    });
  });
});
