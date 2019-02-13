import {createSelector} from 'reselect';
import {orUnknown} from '../../helpers/translations';
import {Identifiable, uuid} from '../../types/Types';
import {limit} from '../../usecases/report/reportActions';
import {ObjectsById} from '../domain-models/domainModels';
import {isSelectedCity, isSelectedMeter} from '../ui/graph/measurement/measurementActions';
import {allQuantities, Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../ui/selection-tree/selectionTreeModels';
import {ThresholdQuery} from '../user-selection/userSelectionModels';
import {
  SelectedTreeEntities,
  SelectionTreeEntities,
  SelectionTreeMeter,
  SelectionTreeState,
} from './selectionTreeModels';

// TODO[!must!] maybe remove later
export const getEnabledMedia = createSelector<SelectedTreeEntities, uuid[], SelectionTreeEntities, Set<Medium>>(
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

export const getThresholdMedia = createSelector<ThresholdQuery | undefined, Quantity, Medium[]>(
  (threshold: ThresholdQuery) => threshold && threshold.quantity,
  (quantity) => {
    if (quantity) {
      return Array.from(new Set<Medium>(Object.keys(allQuantities)
        .map((medium) => (medium as Medium))
        .filter((medium) => Array.from(allQuantities[medium]).includes(quantity))
      ));
    } else {
      return [];
    }
  },
);

export const getMeterIdsWithLimit = (meters?: ObjectsById<SelectionTreeMeter>): uuid[] =>
  meters ? Object.keys(meters).splice(0, limit) : [];

interface ItemProps extends Identifiable {
  entities: SelectionTreeEntities;
}

const makeItems = (
  props: ItemProps,
  ids: uuid[],
  onRenderItem: (props: ItemProps) => SelectionTreeViewComposite,
): SelectionTreeViewComposite[] =>
  ids.sort().map((id: uuid) => onRenderItem({...props, id}));

const makeTreeViewAddresses = (props: ItemProps): SelectionTreeViewComposite => {
  const {id, entities: {addresses}} = props;
  const address = addresses[id];
  return {
    id,
    text: orUnknown(address.name),
    type: SelectionTreeItemType.address,
    items: makeItems(props, address.meters, makeTreeViewMeter),
  };
};

const makeTreeViewMeter = ({id, entities: {meters}}: ItemProps): SelectionTreeViewComposite => {
  const {name: text} = meters[id];
  return {id, text, type: SelectionTreeItemType.meter, items: []};
};

const makeCityTreeViewItems = (props: ItemProps): SelectionTreeViewComposite => {
  const {id, entities: {cities}} = props;
  const city = cities[id];
  return {
    id,
    text: orUnknown(city.name),
    type: SelectionTreeItemType.city,
    items: makeItems(props, city.addresses, makeTreeViewAddresses),
  };
};

export const getSelectionTreeViewItems =
  createSelector<SelectionTreeState, SelectionTreeState, SelectionTreeViewComposite[]>(
    (selectionTree) => selectionTree,
    (selectionTree) => selectionTree.result
      .cities.map((id: uuid) => makeCityTreeViewItems({id, entities: selectionTree.entities}))
  );
