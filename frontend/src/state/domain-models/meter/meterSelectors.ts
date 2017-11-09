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
        sidebarTree.cities.push(sidebarItem(city.id, city.name, '', '', true, 'addressClusters'));
        cities.add(city.id);
      }
      if (!addressClusters.has(cluster.id)) {
        sidebarTree.addressClusters.push(
          sidebarItem(cluster.id, cluster.name, parameterNames.cities, city.id, false, parameterNames.addresses),
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
      if (!addresses.has(address.id)) {
        sidebarTree.addresses.push(sidebarItem(address.id, address.name, 'addressClusters', cluster.id, true, ''));
        sidebarTree.addressClusters.map((cl) => {
          if (cl.id === cluster.id) {
            cl.childNodes.ids.push(address.id);
            return cl;
          } else {
            return cl;
          }
        });
        addresses.add(address.id);
      }

    });
    return normalize(sidebarTree, sidebarTreeSchema);
  },
);

interface SidebarItem {
  id: uuid;
  name: string;
  parent: {type: string; id: uuid};
  selectable: boolean;
  childNodes: {type: string; ids: uuid[]};
}

const sidebarItem =
  (id: uuid, name: string, pType: string, pId: uuid, selectable: boolean, cType: string): SidebarItem =>
    ({id, name, parent: {type: pType, id: pId}, selectable, childNodes: {type: cType, ids: []}});

const tmp = (parents: SidebarItem[], siblings: SidebarItem[], set: Set<uuid>, unit: IdNamed | Address, parent: IdNamed | Address, parentType: string, selectable: boolean, childType: string) => {
  if (!set.has(unit.id)) {
    siblings.push(sidebarItem(unit.id, unit.name, parentType, parent.id, selectable, childType));
    parents.map((un) => {
      if (un.id === parent.id) {
        un.childNodes.ids.push(unit.id);
        return un;
      } else {
        return un;
      }
    });
    set.add(unit.id);
  }
};
