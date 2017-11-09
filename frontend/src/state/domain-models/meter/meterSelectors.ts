// TODO: Perhaps move this to domainSelectors
import {Meter, MetersState} from './meterModels';
import {createSelector} from 'reselect';
import {getResultDomainModels} from '../domainModelsSelectors';
import {IdNamed, uuid} from '../../../types/Types';
import {normalize} from 'normalizr';
import {sidebarTreeSchema} from './meterSchema';
import {parameterNames} from '../../search/selection/selectionModels';
import {Address} from '../domainModels';

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

      if (!cities.has(city.id)) {
        sidebarTree.cities.push(sidebarItem({unit: city, parentType: '', parent: {id: '', name: ''}, selectable: true, childrenType: 'addressClusters'}));
        cities.add(city.id);
      }
      if (!addressClusters.has(cluster.id)) {
        sidebarTree.addressClusters.push(
          sidebarItem({unit: cluster, parentType: parameterNames.cities, parent: city, selectable: false, childrenType: parameterNames.addresses}),
        );
        sidebarTree.cities.map((cty) => {
          if (cty.id === city.id) {
            cty.childNodes.ids.push(cluster.id);
            return cty;
          } else {
            return cty;
          }
        });
        addressClusters.add(cluster.id);
      }
      tmp(sidebarTree, parameterNames.addresses, addresses, address, cluster, 'addressClusters', true, '');
    });
    return normalize(sidebarTree, sidebarTreeSchema);
  },
);

type Unit = IdNamed | Address;

interface SidebarItem {
  id: uuid;
  name: string;
  parent: {type: string; id: uuid};
  selectable: boolean;
  childNodes: {type: string; ids: uuid[]};
}

interface SidebarItemProps {
  unit: Unit;
  parentType: string;
  parent: Unit;
  selectable: boolean;
  childrenType: string;
}

interface TmpProps extends SidebarItemProps {
  sidebarTree: {[key: string]: SidebarItem[]};
  category: string;
  set: Set<uuid>;
}

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

const tmp = (sidebarTree: any, category: string, set: Set<uuid>, unit: Unit, parent: Unit, parentType: string, selectable: boolean, childrenType: string) => {
  if (!set.has(unit.id)) {
    sidebarTree[category].push(sidebarItem({unit, parentType, parent, selectable, childrenType}));
    sidebarTree[parentType].map((par) => {
      if (par.id === parent.id) {
        par.childNodes.ids.push(unit.id);
        return par;
      } else {
        return par;
      }
    });
    set.add(unit.id);
  }
};
