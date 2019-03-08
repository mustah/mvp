import configureStore, {MockStore} from 'redux-mock-store';
import thunk from 'redux-thunk';
import {RootState} from '../../../../reducers/rootReducer';
import {uuid} from '../../../../types/Types';
import {UiState} from '../../uiReducer';
import {selectedIds, toggleExpanded} from '../selectionTreeActions';

describe('selectionTreeActions', () => {

  let store: MockStore<Partial<RootState>>;

  const makeStoreWithSelectedIdsFrom = (openListItems: uuid[]): MockStore<Partial<RootState>> => {
    const ui: Partial<UiState> = {selectionTree: {openListItems}};
    return configureStore([thunk])({ui});
  };

  describe('toggleExpanded', () => {

    it('opens selection tree with given id', () => {
      store = makeStoreWithSelectedIdsFrom([]);

      store.dispatch(toggleExpanded(1));

      expect(store.getActions()).toEqual([selectedIds([1])]);
    });

    it('will remove when the selected item id is already selected', () => {
      store = makeStoreWithSelectedIdsFrom([1, 2]);

      store.dispatch(toggleExpanded(1));

      expect(store.getActions()).toEqual([selectedIds([2])]);
    });
  });

});
