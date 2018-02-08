import {createPayloadAction} from 'react-redux-typescript';
import {GetState} from '../../reducers/rootReducer';
import {uuid} from '../../types/Types';

export const SET_SELECTED_ENTRIES = 'SET_SELECTED_ENTRIES';

const setSelectedEntries = createPayloadAction<string, uuid[]>(SET_SELECTED_ENTRIES);

export const selectEntryToggle = (id: uuid) =>
  (dispatch, getState: GetState): void => {
    const {selectedListItems} = getState().report;
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    newSelectedListItems.delete(id) ?
      dispatch(setSelectedEntries(Array.from(newSelectedListItems))) :
      dispatch(setSelectedEntries(Array.from(newSelectedListItems.add(id))));
  };

// TODO: Don't dispatch if already in selected
export const selectEntryAdd = (id: uuid) =>
  (dispatch, getState: GetState): void => {
    const {selectedListItems} = getState().report;
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    dispatch(setSelectedEntries(Array.from(newSelectedListItems.add(id))));
  };
