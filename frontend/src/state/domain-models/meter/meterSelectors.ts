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
        sidebarTree.cities.push(sidebarItem(city, '', {id: '', name: ''}, true, 'addressClusters'));
        cities.add(city.id);
      }
      if (!addressClusters.has(cluster.id)) {
        sidebarTree.addressClusters.push(
          sidebarItem(cluster, parameterNames.cities, city, false, parameterNames.addresses),
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
      // if (!addresses.has(address.id)) {
      //   sidebarTree.addresses.push(sidebarItem(address.id, address.name, 'addressClusters', cluster.id, true, ''));
      //   sidebarTree.addressClusters.map((cl) => {
      //     if (cl.id === cluster.id) {
      //       cl.childNodes.ids.push(address.id);
      //       return cl;
      //     } else {
      //       return cl;
      //     }
      //   });
      //   addresses.add(address.id);
      // }
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
  (unit: Unit, pType: string, parent: Unit, selectable: boolean, cType: string): SidebarItem =>
    ({id: unit.id, name: unit.name, parent: {type: pType, id: parent.id}, selectable, childNodes: {type: cType, ids: []}});

const tmp = (sidebarTree: any, category: string, set: Set<uuid>, unit: Unit, parent: Unit, parentType: string, selectable: boolean, childType: string) => {
  if (!set.has(unit.id)) {
    sidebarTree[category].push(sidebarItem(unit, parentType, parent, selectable, childType));
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
