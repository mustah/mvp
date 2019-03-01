import {toArray} from 'lodash';
import {createSelector} from 'reselect';
import {orUnknown} from '../../helpers/translations';
import {Identifiable, uuid} from '../../types/Types';
import {limit} from '../../usecases/report/reportActions';
import {LegendItem} from '../../usecases/report/reportModels';
import {ObjectsById} from '../domain-models/domainModels';
import {SelectionTreeItemType, SelectionTreeViewComposite} from '../ui/selection-tree/selectionTreeModels';
import {SelectionTreeEntities, SelectionTreeMeter, SelectionTreeState} from './selectionTreeModels';

export const getLegendItemsWithLimit = (meters?: ObjectsById<SelectionTreeMeter>): LegendItem[] =>
  meters
    ? toArray(meters).splice(0, limit)
      .map(({id, name: label, medium}: SelectionTreeMeter): LegendItem => ({
        id,
        label,
        type: medium,
        isHidden: false,
        quantities: []
      }))
    : [];

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
