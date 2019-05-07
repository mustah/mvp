import {shallowEqual} from 'recompose';
import {DateRange, Period} from '../../components/dates/dateModels';
import {CurrentPeriod} from '../../helpers/dateHelpers';
import {IdNamed, Selected, uuid} from '../../types/Types';
import {Address, City} from '../domain-models/location/locationModels';
import {Query} from '../search/searchModels';
import {Quantity} from '../ui/graph/measurement/measurementModels';
import {ApiRequestSortingOptions, Pagination} from '../ui/pagination/paginationModels';

export const enum ParameterName {
  addresses = 'addresses',
  alarms = 'alarms',
  cities = 'cities',
  facilities = 'facilities',
  gatewaySerials = 'gatewaySerials',
  organisations = 'organisations',
  media = 'media',
  reported = 'reported',
  secondaryAddresses = 'secondaryAddresses',
  gatewayIds = 'gatewayIds',
  manufacturers = 'manufacturers',
  productModels = 'productModels',
  threshold = 'threshold',
  sort = 'sort',
  query = 'w',
}

export type SelectionItem = IdNamed | City | Address;

export enum RelationalOperator {
  lt = '<',
  lte = '<=',
  gt = '>',
  gte = '>=',
}

export interface ThresholdQuery {
  value: string;
  relationalOperator: RelationalOperator;
  quantity: Quantity;
  unit: string;
  duration?: string | null;
  dateRange: SelectionInterval;
}

export type ThresholdQueryProps = Partial<ThresholdQuery>;

export const defaultDateRange: SelectionInterval = {period: Period.latest};

export const emptyThreshold: ThresholdQueryProps = {
  value: '',
  dateRange: defaultDateRange
};

export const isValidThreshold = (threshold?: ThresholdQuery) =>
  threshold !== undefined &&
  ['value', 'relationalOperator', 'quantity', 'unit']
    .every(key => threshold[key] && (threshold[key] as string).length > 0);

export const isEmptyThreshold = (threshold?: ThresholdQuery): boolean =>
  threshold !== undefined && shallowEqual(threshold, emptyThreshold);

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
  dateRange?: SelectionInterval;
  collectionDateRange?: SelectionInterval;
  facilities?: IdNamed[];
  gatewaySerials?: IdNamed[];
  media?: IdNamed[];
  organisations?: IdNamed[];
  reported?: IdNamed[];
  reportDateRange?: SelectionInterval;
  secondaryAddresses?: IdNamed[];
  threshold?: ThresholdQuery;
  w?: IdNamed[];
}

export interface OldSelectionParameters {
  addresses: uuid[];
  cities: uuid[];
  facilities: uuid[];
  gatewaySerials: uuid[];
  media: uuid[];
  secondaryAddresses: uuid[];
}

// TODO specify that customDateRange needs to exist if period === Period.custom
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
  start?: Date;
}

export interface UriLookupStatePaginated extends UriLookupState {
  pagination: Pagination;
  sort?: ApiRequestSortingOptions[];
}

export interface UriLookupStatePaginatedWithPeriod extends UriLookupStatePaginated {
  period?: CurrentPeriod;
}

export type SelectionListItem = SelectionItem & Selected;

export const initialSelectionId = -1;
