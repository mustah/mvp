import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {Medium} from '../ui/graph/measurement/measurementModels';

export interface SelectionTreeState extends NormalizedSelectionTree {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  error?: ErrorResponse;
}

export interface SelectionTreeEntities {
  cities: ObjectsById<SelectionTreeCity>;
  addresses: ObjectsById<SelectionTreeAddress>;
  meters: ObjectsById<SelectionTreeMeter>;
}

export interface SelectionTreeResult {
  cities: uuid[];
}

export interface NormalizedSelectionTree {
  entities: SelectionTreeEntities;
  result: SelectionTreeResult;
}

export interface SelectionTreeMeter extends IdNamed {
  address: string;
  city: string;
  medium: Medium;
}

export interface SelectionTreeCity extends IdNamed {
  addresses: uuid[];
  city: string;
  medium: Medium[];
}

export interface SelectionTreeAddress extends IdNamed {
  address: string;
  city: string;
  meters: uuid[];
}
