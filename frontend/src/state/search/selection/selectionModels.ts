import {Period} from '../../../components/dates/dateModels';
import {IdNamed, ItemOrArray, uuid} from '../../../types/Types';
import {DomainModelsState, SelectionEntity} from '../../domain-models/domainModels';

export const enum ParameterName {
  countries = 'countries',
  cities = 'cities',
  addresses = 'addresses',
  alarms = 'alarms',
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
 * interface SelectedParameters {
 *   period: Period;
 *   [key: ParameterName]: uuid[]
 * }
 */
export interface SelectedParameters {
  cities?: uuid[];
  addresses?: uuid[];
  meterStatuses?: uuid[];
  gatewayStatuses?: uuid[];
  alarms?: uuid[];
  manufacturers?: uuid[];
  productModels?: uuid[];
  period: Period;
}

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
