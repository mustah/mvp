import {uuid} from '../../types/Types';
import {createPayloadAction} from 'react-redux-typescript';
import {RootState} from '../../reducers/rootReducer';

export const SELECT_ENTRY_TOGGLE = 'SELECT_ENTRY_TOGGLE';

const toggleEntry = createPayloadAction<string, uuid[]>(SELECT_ENTRY_TOGGLE);

const filterOutId = (selected: uuid[], id: uuid): uuid[] => selected.filter(sel => sel !== id);

export const selectEntryToggle = (id: uuid) =>
  (dispatch, getState: () => RootState): void => {
    const {selectedListItems} = getState().report;
    const idRemovedFromOpenListItems = filterOutId(selectedListItems, id);
    const idWasRemoved = selectedListItems.length > idRemovedFromOpenListItems.length;
    idWasRemoved ?
      dispatch(toggleEntry(idRemovedFromOpenListItems)) :
      dispatch(toggleEntry([...selectedListItems, id]));
  };
