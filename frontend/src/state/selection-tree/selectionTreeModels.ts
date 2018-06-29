import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';

export interface SelectionTreeState extends NormalizedSelectionTree {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  error?: ErrorResponse;
}

export interface SelectionTreeEntities {
  cities: ObjectsById<CityWithAddresses>;
  addresses: ObjectsById<AddressWithMeters>;
  meters: ObjectsById<IdNamed>;
}

export interface SelectionTreeResult {
  cities: uuid[];
}

export interface NormalizedSelectionTree {
  entities: SelectionTreeEntities;
  result: SelectionTreeResult;
}

interface CityWithAddresses extends IdNamed {
  addresses: uuid[];
}

export interface AddressWithMeters extends IdNamed {
  meters: uuid[];
}

export interface CityWithClusters extends IdNamed {
  clusters: uuid[];
}

export interface ClusterWithAddresses extends IdNamed {
  addresses: uuid[];
}

export interface SelectionTree {
  entities: {
    cities: ObjectsById<CityWithClusters>;
    clusters: ObjectsById<ClusterWithAddresses>;
    addresses: ObjectsById<AddressWithMeters>;
    meters: ObjectsById<IdNamed>;
  };
  result: {
    cities: uuid[];
  };
}
