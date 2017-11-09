import {normalize} from 'normalizr';
import {createSelector} from 'reselect';
import {IdNamed, uuid} from '../../../types/Types';
import {parameterNames} from '../../search/selection/selectionModels';
import {getResultDomainModels} from '../domainModelsSelectors';
import {Meter, MetersState, SidebarItem, SidebarItemProps, SidebarItemsProps} from './meterModels';
import {sidebarTreeSchema} from './meterSchema';

export const getMetersTotal = (state: MetersState): number => state.total;
export const getMeterEntities = (state: MetersState): {[key: string]: Meter} => state.entities.meters;

export const getSidebarTree = createSelector<MetersState, uuid[], {[key: string]: Meter}, any>(
  getResultDomainModels,
  getMeterEntities,
  (metersList: uuid[], meters: {[key: string]: Meter}) => {

    const sidebarTree: {[key: string]: SidebarItem[]} = {cities: [], addresses: [], addressClusters: []};
    const cities = new Set<uuid>();
    const addressClusters = new Set<uuid>();
    const addresses = new Set<uuid>();

    metersList.map((meterId: uuid) => {
      const {city, address} = meters[meterId];
      const clusterName = address.name[0];
      const clusterId = city.name + ':' + clusterName;
      const cluster: IdNamed = {id: clusterId, name: clusterName};

      sidebarItems(sidebarTree, {
        category: parameterNames.cities,
        set: cities,
        unit: city,
        parentType: '',
        parent: {id: '', name: ''},
        selectable: true,
        childrenType: 'addressClusters',
      });

      sidebarItems(sidebarTree, {
        category: 'addressClusters',
        set: addressClusters,
        unit: cluster,
        parentType: parameterNames.cities,
        parent: city,
        selectable: false,
        childrenType: parameterNames.addresses,
      });

      sidebarItems(sidebarTree, {
        category: parameterNames.addresses,
        set: addresses,
        unit: address,
        parentType: 'addressClusters',
        parent: cluster,
        selectable: true,
        childrenType: '',
      });
    });
    return normalize(sidebarTree, sidebarTreeSchema);
  },
);

const sidebarItem =
  (props: SidebarItemProps): SidebarItem => {
    return {
      id: props.unit.id,
      name: props.unit.name,
      parent: {type: props.parentType, id: props.parent.id},
      selectable: props.selectable,
      childNodes: {type: props.childrenType, ids: []},
    };
  };

const sidebarItems = (sidebarTreeUpdate: {[key: string]: SidebarItem[]}, props: SidebarItemsProps): void => {
  const {category, set, ...sidebarItemProps} = props;
  const {unit, parent, parentType} = props;
  if (!set.has(unit.id)) {

    sidebarTreeUpdate[category].push(sidebarItem(sidebarItemProps));
    set.add(unit.id);

    if (parentType !== '') {
      sidebarTreeUpdate[parentType].map((par) => {
        if (par.id === parent.id) {
          par.childNodes.ids.push(unit.id);
          return par;
        } else {
          return par;
        }
      });
    }
  }
};
