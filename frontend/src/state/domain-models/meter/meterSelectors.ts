
// TODO: Perhaps move this to domainSelectors
import {Meter, MetersState} from './meterModels';
import {createSelector} from 'reselect';
import {getResultDomainModels} from '../domainModelsSelectors';
import {uuid} from '../../../types/Types';
import {normalize} from 'normalizr';
import {sidebarTreeSchema} from './meterSchema';
import {parameterNames} from '../../search/selection/selectionModels';

export const getMetersTotal = (state: MetersState): number => state.total;
export const getMeterEntities = (state: MetersState): {[key: string]: Meter} => state.entities.meters;

export const getSidebarTree = createSelector<MetersState, uuid[], {[key: string]: Meter}, any>(
    getResultDomainModels,
    getMeterEntities,
    (metersList: uuid[], meters: {[key: string]: Meter}) => {

      const sidebarTree: {[key: string]: any[]} = {cities: [], addresses: [], addressClusters: []};
      const cities = new Set();
      const addressClusters = new Set();
      const addresses = new Set();

      metersList.map((meterId: uuid) => {
      const {city, address} = meters[meterId];
      if (!cities.has(city)) {
        sidebarTree.cities.push(sidebarItem(city.id, city.name, '', '', true, 'addressClusters'));
        cities.add(city);
      }
      const clusterId = city.name + ':' + address.name[0];
      if (!addressClusters.has(clusterId)) {
        sidebarTree.addressClusters.push(
          sidebarItem(clusterId, address.name[0], parameterNames.cities, city.id, false, parameterNames.addresses),
        );
        sidebarTree.cities.map((cty) => {
          if (cty.id === city.id) {
            cty.childNodes.ids.push(clusterId);
            return cty;
          } else {
            return cty;
          }
        });
        addressClusters.add(clusterId);
      }
      if (!addresses.has(address.id)) {
        sidebarTree.addresses.push(sidebarItem(address.id, address.name, 'addressClusters', clusterId, true, ''));
        sidebarTree.addressClusters.map((cl) => {
          if (cl.id === clusterId) {
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

const sidebarItem =
  (id: uuid, name: string, pType: string, pId: uuid, selectable: boolean, cType: string) =>
    ({id, name, parent: {type: pType, id: pId}, selectable, childNodes: {type: cType, ids: []}});
