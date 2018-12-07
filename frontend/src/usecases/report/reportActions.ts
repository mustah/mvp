import {toggle} from '../../helpers/collections';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {SelectionTree, SelectionTreeState} from '../../state/selection-tree/selectionTreeModels';
import {getSelectionTree} from '../../state/selection-tree/selectionTreeSelectors';
import {isSelectedCity, isSelectedMeter} from '../../state/ui/graph/measurement/measurementActions';
import {
  allQuantities,
  defaultQuantityForMedium,
  Medium,
  Quantity
} from '../../state/ui/graph/measurement/measurementModels';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {Dispatcher, OnPayloadAction, payloadActionOf, uuid} from '../../types/Types';
import {SelectedReportEntriesPayload} from './reportModels';

export const SET_SELECTED_ENTRIES = 'SET_SELECTED_ENTRIES';
export const TOGGLE_LINE = 'TOGGLE_LINE';

export const setSelectedEntries: OnPayloadAction<SelectedReportEntriesPayload> =
  payloadActionOf<SelectedReportEntriesPayload>(SET_SELECTED_ENTRIES);

export const toggleLine = payloadActionOf<uuid>(TOGGLE_LINE);

const mediaForSelection = (ids: uuid[], {entities: {meters, cities}}: SelectionTreeState): Set<Medium> => {
  const cityMedia: Medium[] = ids.filter(isSelectedCity)
    .map((cityId) => cities[cityId].medium)
    .reduce((all, current) => all.concat(current), []);

  const meterMedia: Medium[] = ids.filter(isSelectedMeter)
    .map((meterId) => meters[meterId].medium);

  return new Set([...cityMedia, ...meterMedia]);
};

interface DispatchWithinLimits {
  dispatch: Dispatcher;
  selectionTree: SelectionTreeState;
  previousIds: uuid[];
  ids: uuid[];
  selectedQuantities: Quantity[];
}

const dispatchIfWithinLimits = ({
  dispatch,
  selectionTree,
  previousIds,
  ids,
  selectedQuantities
}: DispatchWithinLimits) => {
  const limit: number = 20;
  const newAmount: number = ids.filter(isSelectedMeter).length;

  if (newAmount > limit) {
    dispatch(showFailMessage(firstUpperTranslated(
      'only {{limit}} meters can be selected at the same time', {limit},
    )));
    return;
  }

  const orderedMedia: Medium[] = Object.keys(allQuantities) as Medium[];
  const previousMedia: Set<Medium> = mediaForSelection(previousIds, selectionTree);
  const currentlyActiveMedia: Set<Medium> = mediaForSelection(ids, selectionTree);

  const activeMedia: Medium[] = orderedMedia
    .filter((medium) => previousMedia.has(medium) && currentlyActiveMedia.has(medium));

  const maxSelectedIndicators = 2;
  orderedMedia
    .filter((medium) => currentlyActiveMedia.has(medium))
    .forEach((activeMedium) => {
      if (activeMedia.length < maxSelectedIndicators) {
        activeMedia.push(activeMedium);
      }
    });

  const indicatorsToSelect: Medium[] = orderedMedia.filter((medium) => activeMedia.includes(medium));

  const quantitiesToSelect: Quantity[] = selectedQuantities.length
    ? selectedQuantities
    : Array.from(new Set(indicatorsToSelect.map(defaultQuantityForMedium)));

  dispatch(setSelectedEntries({ids, quantitiesToSelect, indicatorsToSelect}));
};

export const toggleSingleEntry = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {selectionTree, report: {selectedListItems}, ui: {indicator: {selectedQuantities}}} = getState();
    return dispatchIfWithinLimits({
      dispatch,
      selectionTree,
      previousIds: selectedListItems,
      ids: toggle(id, selectedListItems),
      selectedQuantities,
    });
  };

export const addToReport = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {selectionTree, report: {selectedListItems}, ui: {indicator: {selectedQuantities}}} = getState();
    if (!selectedListItems.includes(id)) {
      dispatchIfWithinLimits({
        dispatch,
        ids: [...selectedListItems, id],
        selectionTree,
        previousIds: selectedListItems,
        selectedQuantities,
      });
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
    const {report: {selectedListItems}, selectionTree, ui: {indicator: {selectedQuantities}}} = getState();
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

    clusters.forEach((clusterId: uuid) =>
      shouldShow ? listItems.add(clusterId) : listItems.delete(clusterId));

    addresses.forEach((address: uuid) => {
      shouldShow ? listItems.add(address) : listItems.delete(address);
      clustered.entities.addresses[address].meters.forEach((meterId: uuid) =>
        shouldShow ? listItems.add(meterId) : listItems.delete(meterId));
    });

    dispatchIfWithinLimits({
      dispatch,
      selectionTree,
      previousIds: selectedListItems,
      ids: Array.from(listItems),
      selectedQuantities,
    });
  };

export const selectEntryAdd = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {report: {selectedListItems}, selectionTree, ui: {indicator: {selectedQuantities}}} = getState();
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    const originalLength = newSelectedListItems.size;
    newSelectedListItems.add(id);
    if (newSelectedListItems.size > originalLength) {
      dispatchIfWithinLimits({
        dispatch,
        selectionTree,
        previousIds: selectedListItems,
        ids: Array.from(newSelectedListItems),
        selectedQuantities
      });
    }
  };
