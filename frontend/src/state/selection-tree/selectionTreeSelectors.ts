import {createSelector} from 'reselect';
import {orUnknown} from '../../helpers/translations';
import {uuid} from '../../types/Types';
import {Query} from '../../usecases/search/searchModels';
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

export type SelectionTreeQueried = SelectionTreeState & Query;

const matches = (query: string | undefined) => {
  if (!query || query.trim().length < 3) {
    return (_: string) => true;
  }
  const re = new RegExp(query, 'i');
  return (searchTerm: string) => re.test(searchTerm);
};

export const getSelectionTree =
  createSelector<SelectionTreeQueried, SelectionTreeResult, SelectionTreeEntities, string | undefined, SelectionTree>(
    (state) => state.result,
    (state) => state.entities,
    (state) => state.query,
    ({cities: cityIds}: SelectionTreeResult, {cities, addresses, meters}: SelectionTreeEntities, query) => {

      const shouldBeInTree = matches(query);

      const idsMatchingSearch = new Set<uuid>();

      if (query) {
        cityIds.forEach((cityId) => {
          const city = cities[cityId];
          const cityIncluded = shouldBeInTree(city.name);
          if (cityIncluded) {
            idsMatchingSearch.add(cityId);
          }

          city.addresses.forEach((addressId) => {
            const address = addresses[addressId];
            const addressIncluded = shouldBeInTree(address.name);
            if (cityIncluded || addressIncluded) {
              idsMatchingSearch.add(cityId);
              idsMatchingSearch.add(addressId);
              address.meters.forEach((meterId) => idsMatchingSearch.add(meterId));
            } else {
              address.meters.forEach((meterId) => {
                const meter = meters[meterId];
                if (shouldBeInTree(meter.name)) {
                  idsMatchingSearch.add(cityId);
                  idsMatchingSearch.add(addressId);
                  idsMatchingSearch.add(meterId);
                }
              });
            }
          });
        });
      }

      const citiesWithClusters: ObjectsById<CityWithClusters> = {};

      const onlyMatches = (id) => !query || idsMatchingSearch.has(id);

      const createClustersByCity = (cityId: uuid): ObjectsById<ClusterWithAddresses> => {
        const city = cities[cityId];
        const cityClusters: ObjectsById<ClusterWithAddresses> = city.addresses
          .filter(onlyMatches)
          .map((id) => addresses[id])
          .reduce((clusters, address) => addOrInitialCluster(address, city.id, clusters), {});

        citiesWithClusters[cityId] = {
          id: city.id,
          name: city.name,
          clusters: Object.keys(cityClusters),
        };

        return cityClusters;
      };

      const clusters: ObjectsById<ClusterWithAddresses> = cityIds
        .filter(onlyMatches)
        .map(createClustersByCity)
        .reduce((prev, curr) => ({...prev, ...curr}), {});

      return {
        entities: {
          meters,
          addresses,
          cities: citiesWithClusters,
          clusters,
        },
        result: {
          cities: cityIds.filter(onlyMatches),
        },
      };
    },
  );
