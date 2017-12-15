import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {SELECTION_TREE_TOGGLE_ENTRY, selectionTreeExpandToggle} from '../selectionTreeActions';
import {SelectionTreeState} from '../selectionTreeModels';

const configureMockStore = configureStore([thunk]);

describe('selectionTreeActions', () => {
  it('makes sure selectionTreeExpandToggle adds id to openListItems if not already in there', () => {
    const initialState: SelectionTreeState = {openListItems: [1, 2]};
    const store = configureMockStore({ui: {selectionTree: {...initialState}}});

    store.dispatch(selectionTreeExpandToggle(3));

    expect(store.getActions()).toEqual([
      {
        type: SELECTION_TREE_TOGGLE_ENTRY,
        payload: [1, 2, 3],
      },
    ]);
  });
  it('makes sure selectionTreeExpandToggle removes id to openListItems if already in there', () => {
    const initialState: SelectionTreeState = {openListItems: [1, 2, 3]};
    const store = configureMockStore({ui: {selectionTree: {...initialState}}});

    store.dispatch(selectionTreeExpandToggle(3));

    expect(store.getActions()).toEqual([
      {
        type: SELECTION_TREE_TOGGLE_ENTRY,
        payload: [1, 2],
      },
    ]);
  });
});
