import {Medium} from '../../components/indicators/indicatorWidgetModels';
import {ErrorResponse, IdNamed, uuid} from '../../types/Types';
import {ReportState} from '../../usecases/report/reportModels';
import {ObjectsById} from '../domain-models/domainModels';

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
    addresses: ObjectsById<SelectionTreeAddress>;
    meters: ObjectsById<SelectionTreeMeter>;
  };
  result: {
    cities: uuid[];
  };
}

export type SelectedTreeEntities = Pick<ReportState, 'selectedListItems'> & Pick<SelectionTreeState, 'entities'>;
