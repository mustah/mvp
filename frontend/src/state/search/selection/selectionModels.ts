import {IdNamed, Period, uuid} from '../../../types/Types';
import {GeoDataState} from '../../domain-models/geoData/geoDataModels';

export interface SelectionParameter extends IdNamed {
  parameter: parameterNames;
}

export interface SelectedParameters {
  cities?: uuid[];
  addresses?: uuid[];
  statuses?: uuid[];
  period?: Period;
}

export interface SelectionState {
  selected: SelectedParameters;
}

export interface LookupState {
  selection: SelectionState;
  repository: GeoDataState;
}

export enum parameterNames {
  cities = 'cities',
  addresses = 'addresses',
  statuses = 'statuses',
  period = 'period',
}

export type OnSelectPeriod = (period: Period) => void;
