import {createPayloadAction} from 'react-redux-typescript';
import {toggle} from '../../helpers/collections';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {SelectionTree} from '../../state/selection-tree/selectionTreeModels';
import {getSelectionTree} from '../../state/selection-tree/selectionTreeSelectors';
import {isSelectedMeter} from '../../state/ui/graph/measurement/measurementActions';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {uuid} from '../../types/Types';

export const SET_SELECTED_ENTRIES = 'SET_SELECTED_ENTRIES';

export const setSelectedEntries = createPayloadAction<string, uuid[]>(SET_SELECTED_ENTRIES);

const dispatchIfWithinLimits = (dispatch, ids: uuid[]) => {
  const limit: number = 20;
  const newAmount: number = ids.filter(isSelectedMeter).length;

  if (newAmount > limit) {
    dispatch(showFailMessage(firstUpperTranslated(
      'only {{limit}} meters can be selected at the same time', {limit},
    )));
  } else {
    dispatch(setSelectedEntries(ids));
  }
};

export const toggleSingleEntry = (id: uuid) =>
  (dispatch, getState: GetState) =>
    dispatchIfWithinLimits(dispatch, toggle(id, getState().report.selectedListItems));

export const addToReport = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const alreadyInReport: uuid[] = getState().report.selectedListItems;
    if (!alreadyInReport.includes(id)) {
      dispatch(setSelectedEntries([...alreadyInReport, id]));
    }
  };

type Level = 'clusters' | 'cities' | 'addresses';

// this is what you get when you model a DAG in a flat way..
const levelFromId = (id: string): Level => {
  if (id.indexOf(':') !== -1) {
    return 'clusters';
  }
  const levels: number = (id.match(/,/g) || []).length;
  return levels === 1 ? 'cities' : 'addresses';
};

export const toggleIncludingChildren = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {report: {selectedListItems}, selectionTree} = getState();
    const clustered: SelectionTree = getSelectionTree(selectionTree);

    const listItems: Set<uuid> = new Set(selectedListItems);
    listItems.has(id) ? listItems.delete(id) : listItems.add(id);
    const shouldShow: boolean = listItems.has(id);

    const level: Level = levelFromId(id as string);

    let addresses: uuid[] = [];
    const clusters: uuid[] = [];

    if (level === 'clusters') {
      clusters.push(id);
      addresses = addresses.concat(clustered.entities.clusters[id].addresses);
    }

    if (level === 'addresses') {
      addresses.push(id);
    }

    clusters.forEach((clusterId: uuid) => {
      shouldShow ? listItems.add(clusterId) : listItems.delete(clusterId);
    });

    addresses.map((address: uuid) => {
      shouldShow ? listItems.add(address) : listItems.delete(address);
      clustered.entities.addresses[address].meters.map((meterId: uuid) => {
        shouldShow ? listItems.add(meterId) : listItems.delete(meterId);
      });
    });

    dispatchIfWithinLimits(dispatch, Array.from(listItems));
  };

export const selectEntryAdd = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {selectedListItems} = getState().report;
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    const originalLength = newSelectedListItems.size;
    newSelectedListItems.add(id);
    if (newSelectedListItems.size > originalLength) {
      dispatchIfWithinLimits(dispatch, Array.from(newSelectedListItems));
    }
  };
