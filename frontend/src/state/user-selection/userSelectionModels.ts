import {DateRange, Period} from '../../components/dates/dateModels';
import {IdNamed, ItemOrArray, uuid} from '../../types/Types';
import {DomainModelsState, SelectionEntity} from '../domain-models/domainModels';

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

/**
 * After https://github.com/Microsoft/TypeScript/issues/13042 is resolved, we can replace the repetitive definitions
 * below with something prettier, like:
 * interface SelectedParameters {
 *   period: Period;
 *   [key: ParameterName]: uuid[]
 * }
 */
export interface SelectedParameters {
  addresses?: uuid[];
  alarms?: uuid[];
  cities?: uuid[];
  gatewayStatuses?: uuid[];
  manufacturers?: uuid[];
  meterIds?: uuid[];
  meterStatuses?: uuid[];
  dateRange: SelectionInterval;
  productModels?: uuid[];
}

export interface SelectionInterval {
  period: Period;
  customDateRange?: DateRange;
}

export interface UserSelection extends IdNamed {
  selectionParameters: SelectedParameters;
  isChanged: boolean;
}

export interface UserSelectionState {
  userSelection: UserSelection;
}

export interface LookupState {
  userSelection: UserSelectionState;
  domainModels: DomainModelsState;
}

export type OnSelectPeriod = (period: Period) => void;
export type OnSelectCustomDateRange = (dateRange: DateRange) => void;

export type OnSelectSelection = (selection: UserSelection) => void;

export type SelectionListItem = SelectionEntity & {selected: boolean};

export type OnSelectParameter = (selectionParameter: SelectionParameter) => void;