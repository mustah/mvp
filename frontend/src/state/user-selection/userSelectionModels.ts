import {DateRange, Period} from '../../components/dates/dateModels';
import {IdNamed, Selected, uuid} from '../../types/Types';
import {Query} from '../../usecases/search/searchModels';
import {SelectionItem} from '../domain-models/domainModels';
import {Pagination} from '../ui/pagination/paginationModels';

export const enum ParameterName {
  addresses = 'addresses',
  alarms = 'alarms',
  cities = 'cities',
  facilities = 'facilities',
  gatewaySerials = 'gatewaySerials',
  media = 'media',
  period = 'period',
  reported = 'reported',
  secondaryAddresses = 'secondaryAddresses',
}

export interface SelectionParameter {
  item: SelectionItem;
  parameter: ParameterName;
}

/**
 * After https://github.com/Microsoft/TypeScript/issues/13042 is resolved, we can replace the
 * repetitive definitions below with something prettier, like: interface SelectedParameters {
 * period: Period;
 *   [key: ParameterName]: uuid[]
 * }
 */
export interface SelectedParameters {
  addresses?: SelectionItem[];
  cities?: SelectionItem[];
  dateRange: SelectionInterval;
  facilities?: IdNamed[];
  gatewaySerials?: IdNamed[];
  media?: IdNamed[];
  reported?: IdNamed[];
  secondaryAddresses?: IdNamed[];
}

export interface OldSelectionParameters {
  addresses: uuid[];
  cities: uuid[];
  facilities: uuid[];
  gatewaySerials: uuid[];
  media: uuid[];
  secondaryAddresses: uuid[];
}

export interface SelectionInterval {
  period: Period;
  customDateRange?: DateRange;
}

export interface UserSelection extends IdNamed {
  selectionParameters: SelectedParameters;
  isChanged: boolean;
  ownerUserId?: uuid;
  organisationId?: uuid;
}

export interface UserSelectionState {
  userSelection: UserSelection;
}

export interface UriLookupState extends UserSelectionState, Query {
  now: Date;
}

export interface UriLookupStatePaginated extends UriLookupState, Query {
  pagination: Pagination;
}

export type OnSelectPeriod = (period: Period) => void;
export type OnSelectCustomDateRange = (dateRange: DateRange) => void;

export type OnSelectSelection = (selection: UserSelection) => void;

export type SelectionListItem = SelectionItem & Selected;

export type OnSelectParameter = (selectionParameter: SelectionParameter) => void;
