import {IdNamed, Period, uuid} from '../../../types/Types';
import {DomainModelsState, SelectionEntity} from '../../domain-models/domainModels';

export enum ParameterName {
  cities = 'cities',
  addresses = 'addresses',
  meterStatuses = 'meterStatuses',
  gatewayStatuses = 'gatewayStatuses',
  alarms = 'alarms',
  manufacturers = 'manufacturers',
  productModels = 'productModels',
  period = 'period',
}

export interface SelectionParameter extends IdNamed {
  parameter: ParameterName;
}

/**
 * After https://github.com/Microsoft/TypeScript/issues/13042 is resolved, we can replace the repetitive definitions
 * below with something prettier, like:
 * interface AllSelectionParameters {
 *   period: Period;
 *   [key: ParameterName]: uuid[]
 * }
 */
interface AllSelectionParameters {
  cities: uuid[];
  addresses: uuid[];
  meterStatuses: uuid[];
  gatewayStatuses: uuid[];
  alarms: uuid[];
  manufacturers: uuid[];
  productModels: uuid[];
  period: Period;
}

export type SelectedParameters = Partial<AllSelectionParameters>;

export interface SelectionState extends IdNamed {
  selected: SelectedParameters;
  isChanged: boolean;
}

export interface LookupState {
  selection: SelectionState;
  domainModels: DomainModelsState;
}

export type OnSelectPeriod = (period: Period) => void;

export type OnSelectSelection = (selection: SelectionState) => void;

export type SelectionListItem = SelectionEntity & {selected: boolean};

export interface SelectionSummary {
  cities: number;
  addresses: number;
  meters: number;
}
