import {createSelector} from 'reselect';
import {uuid} from '../../types/Types';
import {limit} from '../../usecases/report/reportActions';
import {ObjectsById} from '../domain-models/domainModels';
import {isSelectedCity, isSelectedMeter} from '../ui/graph/measurement/measurementActions';
import {allQuantities, Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {ThresholdQuery} from '../user-selection/userSelectionModels';
import {
  SelectedTreeEntities,
  SelectionTreeEntities,
  SelectionTreeMeter,
  SelectionTreeState,
} from './selectionTreeModels';

export const getMedia = createSelector<SelectedTreeEntities, uuid[], SelectionTreeEntities, Set<Medium>>(
  ({selectedListItems}) => selectedListItems,
  ({entities}) => entities,
  (ids: uuid[], {cities, meters}: SelectionTreeEntities) => {
    const meterMedia: Medium[] = ids
      .filter(isSelectedMeter)
      .filter((id: uuid) => meters[id] !== undefined)
      .map((id: uuid) => meters[id].medium);

    const cityMedia: Medium[] = ids
      .filter(isSelectedCity)
      .filter((id: uuid) => cities[id] !== undefined)
      .map((id: uuid): Medium[] => cities[id].medium)
      .reduce((acc: Medium[], current: Medium[]): Medium[] => acc.concat(current), []);

    return new Set([...meterMedia, ...cityMedia]);
  },
);

export const getThresholdMedia = createSelector<ThresholdQuery | undefined, Quantity, Set<Medium>>(
  (threshold: ThresholdQuery) => threshold && threshold.quantity,
  (quantity) => {
    if (quantity) {
      return new Set<Medium>(Object.keys(allQuantities)
        .map((medium) => (medium as Medium))
        .filter((medium) => Array.from(allQuantities[medium]).includes(quantity))
      );
    } else {
      return new Set<Medium>();
    }
  },
);

export const getMeterIdsWithLimit = (meters?: ObjectsById<SelectionTreeMeter>): uuid[] =>
  meters ? Object.keys(meters).splice(0, limit) : [];

export const getMeterIds = createSelector<SelectionTreeState, ObjectsById<SelectionTreeMeter>, uuid[]>(
  (state) => state.entities.meters,
  (meters) => getMeterIdsWithLimit(meters)
);
