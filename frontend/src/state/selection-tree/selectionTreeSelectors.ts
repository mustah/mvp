import {createSelector} from 'reselect';
import {orUnknown} from '../../helpers/translations';
import {uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {
  AddressWithMeters,
  CityWithClusters,
  ClusterWithAddresses,
  SelectionTree,
  SelectionTreeEntities,
  SelectionTreeResult,
  SelectionTreeState,
} from './selectionTreeModels';

const addOrInitialCluster = (
  address: AddressWithMeters,
  cityId: uuid,
  cityClusters: ObjectsById<ClusterWithAddresses>,
): ObjectsById<ClusterWithAddresses> => {

  const translatedName = orUnknown(address.name);
  const firstLetter = translatedName[0];
  const clusterId = `${cityId}:${firstLetter}`;
  const cityClusterName = `${firstLetter.toUpperCase()}...`;
  const cityCluster = cityClusters[clusterId];

  return {
    ...cityClusters,
    [clusterId]: cityCluster
      ? {
        ...cityCluster,
        name: `${cityClusterName}(${cityCluster.addresses.length + 1})`,
        addresses: [...cityCluster.addresses, address.id],
      }
      : {id: clusterId, name: `${cityClusterName}(1)`, addresses: [address.id]},
  };
};

export const getSelectionTree =
  createSelector<SelectionTreeState, SelectionTreeResult, SelectionTreeEntities, SelectionTree>(
    (state) => state.result,
    (state) => state.entities,
    ({cities: cityIds}: SelectionTreeResult, {cities, addresses, ...otherEntities}: SelectionTreeEntities) => {

      const citiesWithClusters: ObjectsById<CityWithClusters> = {};

      const createClustersByCity = (cityId: uuid): ObjectsById<ClusterWithAddresses> => {
        const city = cities[cityId];
        const cityClusters: ObjectsById<ClusterWithAddresses> = city.addresses
          .map((id) => addresses[id])
          .reduce((clusters, address) => addOrInitialCluster(address, city.id, clusters), {});

        citiesWithClusters[cityId] = {
          id: city.id,
          name: city.name,
          clusters: Object.keys(cityClusters),
        };

        return cityClusters;
      };

      const clusters: ObjectsById<ClusterWithAddresses> = cityIds.map(createClustersByCity)
        .reduce((prev, curr) => ({...prev, ...curr}), {});

      return {
        entities: {
          ...otherEntities,
          addresses,
          cities: citiesWithClusters,
          clusters,
        },
        result: {cities: cityIds},
      };
    },
  );
