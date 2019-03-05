import {DateRange, Period, TemporalResolution} from '../../components/dates/dateModels';
import {IdNamed, Selected, uuid} from '../../types/Types';
import {Query} from '../../usecases/search/searchModels';
import {Address, City} from '../domain-models/location/locationModels';
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

export const isValidThreshold = (threshold: undefined | ThresholdQuery) =>
  threshold !== undefined &&
  ['value', 'relationalOperator', 'quantity', 'unit']
    .every((key) => threshold[key] && (threshold[key] as string).length > 0);

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
  organisations?: IdNamed[];
  reported?: IdNamed[];
  secondaryAddresses?: IdNamed[];
  threshold?: ThresholdQuery;
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
  start?: Date;
}

export interface UriLookupStatePaginated extends UriLookupState {
  pagination: Pagination;
  sort?: ApiRequestSortingOptions[];
}

export type OnSelectPeriod = (period: Period) => void;
export type OnSelectResolution = (resolution: TemporalResolution) => void;
export type OnSelectCustomDateRange = (dateRange: DateRange) => void;

export type OnSelectSelection = (selection: UserSelection) => void;

export type SelectionListItem = SelectionItem & Selected;

export type OnSelectParameter = (selectionParameter: SelectionParameter) => void;

export type OnChangeThreshold = (threshold: ThresholdQuery) => void;

export const initialSelectionId = -1;
