import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {selectEntryAdd, selectEntryToggle, setSelectedEntries} from '../reportActions';
import {ReportState} from '../reportModels';

const configureMockStore = configureStore([thunk]);

describe('reportActions', () => {

  it('makes sure selectEntryToggle dispatch action to ADD "id" to selected entries if not already selected', () => {
    const initialState: ReportState = {selectedListItems: [2, 3]};
    const store = configureMockStore({report: {...initialState}});

    store.dispatch(selectEntryToggle(1));

    expect(store.getActions()).toEqual([
      setSelectedEntries([2, 3, 1]),
    ]);

  });
  it('makes sure selectEntryToggle dispatch action to REMOVE "id" from selected entries if already selected', () => {
    const initialState: ReportState = {selectedListItems: [1, 2, 3]};
    const store = configureMockStore({report: {...initialState}});

    store.dispatch(selectEntryToggle(1));

    expect(store.getActions()).toEqual([
      setSelectedEntries([2, 3]),
    ]);
  });
  it('test that selectEntryAdd adds "id" to selected if not there', () => {
    const initalState: ReportState = {selectedListItems: [1, 2]};
    const store = configureMockStore({report: {...initalState}});

    store.dispatch(selectEntryAdd(3));

    expect(store.getActions()).toEqual([
      setSelectedEntries([1, 2, 3]),
    ]);
  });
  it('test that selectEntryAdd do nothing when "id" already exist in selected', () => {
    const initalState: ReportState = {selectedListItems: [1, 2, 3]};
    const store = configureMockStore({report: {...initalState}});

    store.dispatch(selectEntryAdd(3));

    expect(store.getActions()).toEqual([
      setSelectedEntries([1, 2, 3]),
    ]);
  });
});
