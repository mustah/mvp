import {createPayloadAction} from 'react-redux-typescript';
import {Medium} from '../../components/indicators/indicatorWidgetModels';
import {toggle} from '../../helpers/collections';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {SelectionTree, SelectionTreeState} from '../../state/selection-tree/selectionTreeModels';
import {getSelectionTree} from '../../state/selection-tree/selectionTreeSelectors';
import {isSelectedCity, isSelectedMeter} from '../../state/ui/graph/measurement/measurementActions';
import {
  allQuantities,
  defaultQuantityForMedium,
  Quantity,
} from '../../state/ui/graph/measurement/measurementModels';
import {showFailMessage} from '../../state/ui/message/messageActions';
import {OnPayloadAction, uuid} from '../../types/Types';

export const SET_SELECTED_ENTRIES = 'SET_SELECTED_ENTRIES';

export interface SelectedEntriesPayload {
  ids: uuid[];
  indicatorsToSelect: Medium[];
  quantitiesToSelect: Quantity[];
}

export const setSelectedEntries: OnPayloadAction<SelectedEntriesPayload> =
  createPayloadAction<string, SelectedEntriesPayload>(SET_SELECTED_ENTRIES);

const mediaForSelection = (ids: uuid[], selectionTree: SelectionTreeState): Set<Medium> => {
  const {entities: {meters, cities}}: SelectionTreeState = selectionTree;

  const cityMedia: Medium[] = ids.filter(isSelectedCity)
    .map((cityId) => cities[cityId].medium)
    .reduce((all, current) => all.concat(current), []);

  const meterMedia: Medium[] = ids.filter(isSelectedMeter)
    .map((meterId) => meters[meterId].medium);

  return new Set([...cityMedia, ...meterMedia]);
};

const dispatchIfWithinLimits = ({
  dispatch,
  selectionTree,
  previouslySelected,
  ids,
}) => {
  const limit: number = 20;
  const newAmount: number = ids.filter(isSelectedMeter).length;

  if (newAmount > limit) {
    dispatch(showFailMessage(firstUpperTranslated(
      'only {{limit}} meters can be selected at the same time', {limit},
    )));
    return;
  }

  const orderedMedia: Medium[] = Object.keys(allQuantities) as Medium[];
  const previousMedia: Set<Medium> = mediaForSelection(
    previouslySelected,
    selectionTree,
  );

  const currentlyActiveMedia: Set<Medium> = mediaForSelection(ids, selectionTree);

  const activeIndicators: Medium[] =
    orderedMedia.filter((medium: Medium) => previousMedia.has(medium) && currentlyActiveMedia.has(medium));

  const maxSelectedIndicators = 2;
  orderedMedia
    .filter((medium: Medium) => currentlyActiveMedia.has(medium))
    .forEach((activeMedium: Medium) => {
      if (activeIndicators.length < maxSelectedIndicators) {
        activeIndicators.push(activeMedium);
      }
    });

  const orderedActiveIndicators: Medium[] = orderedMedia.filter(
    (medium: Medium) => activeIndicators.includes(medium),
  );

  const uniqueQuantities: Quantity[] = Array.from(new Set(orderedActiveIndicators.map(defaultQuantityForMedium)));

  dispatch(setSelectedEntries({
    ids,
    quantitiesToSelect: uniqueQuantities,
    indicatorsToSelect: orderedActiveIndicators,
  }));
};

export const toggleSingleEntry = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {selectionTree, report: {selectedListItems}} = getState();
    return dispatchIfWithinLimits({
      dispatch,
      selectionTree,
      previouslySelected: selectedListItems,
      ids: toggle(id, selectedListItems),
    });
  };

export const addToReport = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {selectionTree, report: {selectedListItems}} = getState();
    if (!selectedListItems.includes(id)) {
      dispatchIfWithinLimits({
        dispatch,
        ids: [...selectedListItems, id],
        selectionTree,
        previouslySelected: selectedListItems,
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
      previouslySelected: selectedListItems,
      ids: Array.from(listItems),
    });
  };

export const selectEntryAdd = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {report: {selectedListItems}, selectionTree} = getState();
    const newSelectedListItems = new Set<uuid>(selectedListItems);
    const originalLength = newSelectedListItems.size;
    newSelectedListItems.add(id);
    if (newSelectedListItems.size > originalLength) {
      dispatchIfWithinLimits({
        dispatch,
        selectionTree,
        previouslySelected: selectedListItems,
        ids: Array.from(newSelectedListItems),
      });
    }
  };
