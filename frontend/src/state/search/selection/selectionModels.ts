import {IdNamed, Period, uuid} from '../../../types/Types';
import {SelectionEntity} from '../../domain-models/domainModels';
import {DomainModelsState} from '../../domain-models/domainModelsReducer';

export interface SelectionParameter extends IdNamed {
  parameter: parameterNames;
}

export interface SelectedParameters {
  cities?: uuid[];
  addresses?: uuid[];
  statuses?: uuid[];
  alarms?: uuid[];
  period?: Period;
}

export interface SelectionState extends IdNamed {
  selected: SelectedParameters;
  isChanged: boolean;
}

export interface LookupState {
  selection: SelectionState;
  domainModels: DomainModelsState;
}

export enum parameterNames {
  cities = 'cities',
  addresses = 'addresses',
  statuses = 'statuses',
  alarms = 'alarms',
  period = 'period',
}

export type OnSelectPeriod = (period: Period) => void;

export type OnSelectSelection = (selection: SelectionState) => void;

export type SelectionListItem = SelectionEntity & {selected: boolean};

export interface SelectionSummary {
  cities: number;
  addresses: number;
  meters: number;
}
