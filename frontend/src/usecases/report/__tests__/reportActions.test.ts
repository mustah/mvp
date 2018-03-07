import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';
import {selectEntryAdd, selectEntryToggle, SET_SELECTED_ENTRIES} from '../reportActions';
import {ReportState} from '../reportModels';

const configureMockStore = configureStore([thunk]);

describe('reportActions', () => {

  it('makes sure selectEntryToggle dispatch action to ADD "id" to selected entries if not already selected', () => {
    const initialState: ReportState = {selectedListItems: [2, 3]};
    const store = configureMockStore({report: {...initialState}});

    store.dispatch(selectEntryToggle(1));

    expect(store.getActions()).toEqual([
      {
        type: SET_SELECTED_ENTRIES,
        payload: [2, 3, 1],
      },
    ]);

  });
  it('makes sure selectEntryToggle dispatch action to REMOVE "id" from selected entries if already selected', () => {
    const initialState: ReportState = {selectedListItems: [1, 2, 3]};
    const store = configureMockStore({report: {...initialState}});

    store.dispatch(selectEntryToggle(1));

    expect(store.getActions()).toEqual([
      {
        type: SET_SELECTED_ENTRIES,
        payload: [2, 3],
      },
    ]);
  });
  it('test that selectEntryAdd adds "id" to selected if not there', () => {
    const initialState: ReportState = {selectedListItems: [1, 2]};
    const store = configureMockStore({report: {...initialState}});

    store.dispatch(selectEntryAdd(3));

    expect(store.getActions()).toEqual([
      {
        type: SET_SELECTED_ENTRIES,
        payload: [1, 2, 3],
      },
    ]);
  });
  it('test that selectEntryAdd do nothing when "id" already exist in selected', () => {
    const initialState: ReportState = {selectedListItems: [1, 2, 3]};
    const store = configureMockStore({report: {...initialState}});

    store.dispatch(selectEntryAdd(3));

    expect(store.getActions()).toEqual([
      {
        type: SET_SELECTED_ENTRIES,
        payload: [1, 2, 3],
      },
    ]);
  });
});
