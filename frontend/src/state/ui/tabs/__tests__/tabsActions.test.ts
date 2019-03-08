import configureStore, {MockStore} from 'redux-mock-store';
import {UseCases} from '../../../../types/Types';
import {changeTab, changeTabGateway, changeTabMeter} from '../tabsActions';
import {TabName} from '../tabsModels';

describe('tabsActions', () => {
  let store: MockStore<any>;

  beforeEach(() => {
    store = configureStore()({});
  });

  describe('changeTab', () => {

    it('changes tab in collection use case', () => {
      const action = changeTabGateway(TabName.graph);

      store.dispatch(action);

      expect(store.getActions()).toEqual([
        changeTab({tab: TabName.graph, useCase: UseCases.collection}),
      ]);
    });

    it('changes tab in validation use case', () => {
      const action = changeTabMeter(TabName.list);

      store.dispatch(action);

      expect(store.getActions()).toEqual([
        changeTab({tab: TabName.list, useCase: UseCases.validation}),
      ]);
    });

  });

});
