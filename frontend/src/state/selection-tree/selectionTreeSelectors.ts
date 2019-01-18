import {createSelector} from 'reselect';
import {orUnknown} from '../../helpers/translations';
import {uuid} from '../../types/Types';
import {limit} from '../../usecases/report/reportActions';
import {ObjectsById} from '../domain-models/domainModels';
import {isSelectedCity, isSelectedMeter} from '../ui/graph/measurement/measurementActions';
import {allQuantities, Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {ThresholdQuery} from '../user-selection/userSelectionModels';
import {
  CityWithClusters,
  ClusterWithAddresses,
  SelectedTreeEntities,
  SelectionTree,
  SelectionTreeAddress,
  SelectionTreeEntities,
  SelectionTreeMeter,
  SelectionTreeResult,
  SelectionTreeState,
} from './selectionTreeModels';

const addOrInitialCluster = (
  address: SelectionTreeAddress,
  cityId: uuid,
  cityClusters: ObjectsById<ClusterWithAddresses>,
): ObjectsById<ClusterWithAddresses> => {
  const {name, id} = address;

  const translatedName = orUnknown(name);
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
        addresses: [...cityCluster.addresses, id],
      }
      : {
        id: clusterId,
        name: `${cityClusterName}(1)`,
        addresses: [id],
      },
  };
};

export const getSelectionTree =
  createSelector<SelectionTreeState, SelectionTreeResult, SelectionTreeEntities, SelectionTree>(
    ({result}) => result,
    ({entities}) => entities,
    (
      {cities: cityIds}: SelectionTreeResult,
      {cities, addresses, meters}: SelectionTreeEntities,
    ) => {
      const citiesWithClusters: ObjectsById<CityWithClusters> = {};

      const createClustersByCity = (cityId: uuid): ObjectsById<ClusterWithAddresses> => {
        const city = cities[cityId];
        const {name, id} = city;
        const cityClusters: ObjectsById<ClusterWithAddresses> = city.addresses
          .map((id): SelectionTreeAddress => addresses[id])
          .reduce((clusters, address) => addOrInitialCluster(address, id, clusters), {});

        citiesWithClusters[cityId] = {
          id,
          name,
          clusters: Object.keys(cityClusters),
        };

        return cityClusters;
      };

      const clusters: ObjectsById<ClusterWithAddresses> = cityIds
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
          cities: cityIds,
        },
      };
    },
  );

export const getMedia = createSelector<SelectedTreeEntities, uuid[], SelectionTreeEntities, Set<Medium>>(
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

export const getThresholdMedia = createSelector<ThresholdQuery | undefined, Quantity, Set<Medium>>(
  (threshold: ThresholdQuery) => threshold && threshold.quantity,
  (quantity) => {
    if (quantity) {
      return new Set<Medium>(Object.keys(allQuantities)
        .map((medium) => (medium as Medium))
        .filter((medium) => Array.from(allQuantities[medium]).includes(quantity))
      );
    } else {
      return new Set<Medium>();
    }
  },
);

export const getMeterIdsWithLimit = (meters?: ObjectsById<SelectionTreeMeter>): uuid[] =>
  meters ? Object.keys(meters).splice(0, limit) : [];

export const getMeterIds = createSelector<SelectionTreeState, ObjectsById<SelectionTreeMeter>, uuid[]>(
  (state) => state.entities.meters,
  (meters) => getMeterIdsWithLimit(meters)
);
