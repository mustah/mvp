import {normalize} from 'normalizr';
import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../../types/Types';
import {ParameterName} from '../../search/selection/selectionModels';
import {getResultDomainModels} from '../domainModelsSelectors';
import {
  Meter,
  MetersState,
  SelectionTreeData,
  SelectionTreeItem,
  SelectionTreeItemProps,
  SelectionTreeItemsProps,
} from './meterModels';
import {selectionTreeSchema} from './meterSchema';
import {DomainModel} from '../domainModels';

export const getMetersTotal = (state: MetersState): number => state.total;
export const getMeterEntities = (state: MetersState): DomainModel<Meter> => state.entities;

export const getSelectionTree = createSelector<MetersState, uuid[], {[key: string]: Meter}, SelectionTreeData>(
  getResultDomainModels,
  getMeterEntities,
  (metersList: uuid[], metersLookup: {[key: string]: Meter}) => {

    const selectionTree: {[key: string]: SelectionTreeItem[]} = {
      cities: [], addresses: [], addressClusters: [], meters: [],
    };
    const cities = new Set<uuid>();
    const addressClusters = new Set<uuid>();
    const addresses = new Set<uuid>();
    const meters = new Set<uuid>();

    metersList.map((meterId: uuid) => {
      const {city, address, facility} = metersLookup[meterId];
      const clusterName = address.name[0];
      const clusterId = city.name + ':' + clusterName;
      const cluster: IdNamed = {id: clusterId, name: clusterName};
      const meter: IdNamed = {id: meterId as string, name: facility as string};

      selectionTreeItems(selectionTree, {
        category: ParameterName.cities,
        set: cities,
        unit: city,
        parentType: '',
        parent: {id: '', name: ''},
        selectable: true,
        childrenType: 'addressClusters',
      });

      selectionTreeItems(selectionTree, {
        category: 'addressClusters',
        set: addressClusters,
        unit: cluster,
        parentType: ParameterName.cities,
        parent: city,
        selectable: false,
        childrenType: ParameterName.addresses,
      });

      selectionTreeItems(selectionTree, {
        category: ParameterName.addresses,
        set: addresses,
        unit: address,
        parentType: 'addressClusters',
        parent: cluster,
        selectable: true,
        childrenType: 'meters',
      });

      selectionTreeItems(selectionTree, {
        category: 'meters',
        set: meters,
        unit: meter,
        parentType: ParameterName.addresses,
        parent: address,
        selectable: true,
        childrenType: '',
      });
    });
    // TODO: Perhaps move this moderation into the selectionTreeItemsList to speed up performance.
    selectionTree.addressClusters.map((item) => {
      item.name = item.name + '...(' + item.childNodes.ids.length + ')';
    });

    return normalize(selectionTree, selectionTreeSchema);
  },
);

const selectionTreeItem =
  (props: SelectionTreeItemProps): SelectionTreeItem => {
    return {
      id: props.unit.id,
      name: props.unit.name,
      parent: {type: props.parentType, id: props.parent.id},
      selectable: props.selectable,
      childNodes: {type: props.childrenType, ids: []},
    };
  };

const selectionTreeItems = (selectionTree: {[key: string]: SelectionTreeItem[]}, props: SelectionTreeItemsProps) => {
  const {category, set, ...selectionTreeItemProps} = props;
  const {unit, parent, parentType} = props;
  if (!set.has(unit.id)) {

    selectionTree[category].push(selectionTreeItem(selectionTreeItemProps));
    set.add(unit.id);

    if (parentType !== '') {
      selectionTree[parentType].map((par) => {
        if (par.id === parent.id) {
          par.childNodes.ids.push(unit.id);
        }
      });
    }
  }
};

const getMeterStatusOverview = (meterStatus: uuid) => createSelector<MetersState, uuid[], DomainModel<Meter>, uuid[]>(
  getResultDomainModels,
  getMeterEntities,
  (meters: uuid[], meterLookup: DomainModel<Meter>) =>
    meters.filter(meterId => meterLookup[meterId].status.id === meterStatus),
);
