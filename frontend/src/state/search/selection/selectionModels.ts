import {ItemOrArray, IdNamed, uuid} from '../../../types/Types';
import {DomainModelsState, SelectionEntity} from '../../domain-models/domainModels';
import {Period} from '../../../components/dates/dateModels';

export const enum ParameterName {
  addresses = 'addresses',
  alarms = 'alarms',
  cities = 'cities',
  gatewayStatuses = 'gatewayStatuses',
  manufacturers = 'manufacturers',
  meterStatuses = 'meterStatuses',
  period = 'period',
  productModels = 'productModels',
}

export type FilterParam = uuid | boolean;

export interface SelectionParameter {
  id: ItemOrArray<FilterParam>;
  name?: string;
  parameter: ParameterName;
}

export type OnSelectParameter = (searchParameters: SelectionParameter) => void;

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
