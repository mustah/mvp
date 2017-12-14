import {uuid} from '../../types/Types';
import {createPayloadAction} from 'react-redux-typescript';
import {RootState} from '../../reducers/rootReducer';

export const SET_SELECTED_ENTRIES = 'SET_SELECTED_ENTRIES';

export const setSelectedEntries = createPayloadAction<string, uuid[]>(SET_SELECTED_ENTRIES);

export const selectEntryToggle = (id: uuid) =>
  (dispatch, getState: () => RootState): void => {
    const {selectedListItems} = getState().report;
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    newSelectedListItems.delete(id) ?
      dispatch(setSelectedEntries(Array.from(newSelectedListItems))) :
      dispatch(setSelectedEntries(Array.from(newSelectedListItems.add(id))));
  };

// TODO: Don't dispatch if already in selected
export const selectEntryAdd = (id: uuid) =>
  (dispatch, getState: () => RootState): void => {
    const {selectedListItems} = getState().report;
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    dispatch(setSelectedEntries(Array.from(newSelectedListItems.add(id))));
  };
