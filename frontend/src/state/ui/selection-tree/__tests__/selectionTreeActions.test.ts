import configureStore, {MockStore} from 'redux-mock-store';
import thunk from 'redux-thunk';
import {RootState} from '../../../../reducers/rootReducer';
import {uuid} from '../../../../types/Types';
import {UiState} from '../../uiReducer';
import {SELECTION_TREE_TOGGLE_ENTRY, selectionTreeToggleId} from '../selectionTreeActions';

describe('selectionTreeActions', () => {

  let store: MockStore<Partial<RootState>>;

  it('opens selection tree with given id', () => {
    store = makeStoreWithSelectedIds([]);

    store.dispatch(selectionTreeToggleId(1));

    expect(store.getActions()).toEqual([{
      type: SELECTION_TREE_TOGGLE_ENTRY,
      payload: [1],
    }]);
  });

  it('will remove when the selected item id is already selected', () => {
    store = makeStoreWithSelectedIds([1, 2]);

    store.dispatch(selectionTreeToggleId(1));

    expect(store.getActions()).toEqual([{
      type: SELECTION_TREE_TOGGLE_ENTRY,
      payload: [2],
    }]);
  });

  const makeStoreWithSelectedIds = (openListItems: uuid[]): MockStore<Partial<RootState>> => {
    const ui: Partial<UiState> = {selectionTree: {openListItems}};
    return configureStore([thunk])({ui});
  };

});
