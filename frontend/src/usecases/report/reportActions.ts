import {TemporalResolution} from '../../components/dates/dateModels';
import {toggle} from '../../helpers/collections';
import {isDefined} from '../../helpers/commonUtils';
import {GetState} from '../../reducers/rootReducer';
import {firstUpperTranslated} from '../../services/translationService';
import {ObjectsById} from '../../state/domain-models/domainModels';
import {SelectionTreeMeter, SelectionTreeState} from '../../state/selection-tree/selectionTreeModels';
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

export const SELECT_RESOLUTION = 'SELECT_RESOLUTION';
export const SET_SELECTED_ENTRIES = 'SET_SELECTED_ENTRIES';
export const TOGGLE_LINE = 'TOGGLE_LINE';

export const setSelectedEntries: OnPayloadAction<SelectedReportEntriesPayload> =
  payloadActionOf<SelectedReportEntriesPayload>(SET_SELECTED_ENTRIES);

export const selectResolution: OnPayloadAction<TemporalResolution> =
  payloadActionOf<TemporalResolution>(SELECT_RESOLUTION);

export const toggleLine = payloadActionOf<uuid>(TOGGLE_LINE);

const mediaForSelection = (ids: uuid[], {entities: {meters, cities}}: SelectionTreeState): Set<Medium> => {
  const cityMedia: Medium[] = ids.filter(isSelectedCity)
    .map((cityId) => cities[cityId].medium)
    .reduce((all, current) => all.concat(current), []);

  const meterMedia: Medium[] = ids.filter(isSelectedMeter)
    .map((meterId) => meters[meterId].medium);

  return new Set<Medium>([...cityMedia, ...meterMedia]);
};

const isKnownMedium = (medium: Medium): boolean => medium !== Medium.unknown;

const hasKnownMedia = (id: uuid, meters: ObjectsById<SelectionTreeMeter>): boolean =>
  isKnownMedium(meters[id].medium);

interface DispatchWithinLimits {
  dispatch: Dispatcher;
  selectionTree: SelectionTreeState;
  previousIds: uuid[];
  ids: uuid[];
  selectedQuantities: Quantity[];
}

export const limit: number = 130;

const maxSelectedIndicators = 2;

const dispatchIfWithinLimits = ({
  dispatch,
  selectionTree,
  previousIds,
  ids,
  selectedQuantities
}: DispatchWithinLimits) => {
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
    if (!selectedListItems.includes(id) && hasKnownMedia(id, selectionTree.entities.meters)) {
      dispatchIfWithinLimits({
        dispatch,
        ids: [...selectedListItems, id],
        selectionTree,
        previousIds: selectedListItems,
        selectedQuantities,
      });
    }
  };

type GroupingLevel = 'cities' | 'addresses';

// this is what you get when you model a DAG in a flat way..
const levelFromId = (id: string): GroupingLevel => {
  const levels: number = (id.match(/,/g) || []).length;
  return levels === 1 ? 'cities' : 'addresses';
};

export const toggleGroupItems = (id: uuid) =>
  (dispatch, getState: GetState) => {
    const {report: {selectedListItems}, selectionTree, ui: {indicator: {selectedQuantities}}} = getState();

    const listItems: Set<uuid> = new Set(selectedListItems);
    listItems.has(id) ? listItems.delete(id) : listItems.add(id);
    const shouldShow: boolean = listItems.has(id);

    const addresses: uuid[] = [];

    if (levelFromId(id as string) === 'addresses') {
      addresses.push(id);
    }

    addresses.forEach((id: uuid) => {
      shouldShow ? listItems.add(id) : listItems.delete(id);
      selectionTree.entities.addresses[id].meters.forEach((meterId: uuid) =>
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

export const showMetersInGraph = (meterIds: uuid[]) =>
  (dispatch, getState: GetState) => {
    const {report: {selectedListItems}, selectionTree, ui: {indicator: {selectedQuantities}}} = getState();

    const ids = meterIds.map((id) => selectionTree.entities.meters[id])
      .filter(isDefined)
      .filter(({medium}) => isKnownMedium(medium))
      .map(({id}) => id);

    dispatchIfWithinLimits({
      dispatch,
      ids: Array.from(new Set<uuid>(ids)),
      selectionTree,
      previousIds: selectedListItems,
      selectedQuantities,
    });
  };

const emptyReportState = {ids: [], indicatorsToSelect: [], quantitiesToSelect: []};

export const clearSelectedListItems = () =>
  (dispatch) => dispatch(setSelectedEntries(emptyReportState));
