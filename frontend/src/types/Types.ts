import * as React from 'react';
import {action} from 'typesafe-actions';
import {EmptyAction, PayloadAction} from 'typesafe-actions/dist/type-helpers';
import {Maybe} from '../helpers/Maybe';
import {EndPoints} from '../services/endPoints';
import {SortOption} from '../state/ui/pagination/paginationModels';

export type uuid = string | number;
export type UnixTimestamp = number;

export type EncodedUriParameters = string;

/**
 * Is a payload action with action type of <code>string</code> and payload of type <code><P></code>.
 */
export type Action<P> = PayloadAction<string, P>;
export type OnPayloadAction<P> = (payload: P) => Action<P>;
export type OnEmptyAction = () => EmptyAction<string>;
export type Dispatch = React.Dispatch<any>;

export type CallbackWith<T> = (arg: T) => void;
export type CallbackAny = (...args: any) => void;
export type Callback = () => void;
export type OnClick = Callback;
export type OnClickEventHandler = (event: any) => void;
export type OnClickWith<T> = CallbackWith<T>;
export type OnChange = CallbackAny;
export type OnKeyPress = CallbackAny;
export type OnClickWithId = CallbackWith<uuid>;
export type CallbackWithId = (id: uuid, parameters?: EncodedUriParameters) => void;

export type RenderFunction<T> = (props: T) => Children;

export type Fetch = (parameters?: EncodedUriParameters) => void;
export type FetchPaginated = (page: number, requestModel?: string, sortingOptions?: SortOption[]) => void;
export type OnFetch = (url: EndPoints | string, parameters?: EncodedUriParameters) => void;
export type OnUpdate<T> = (endPoint: EndPoints | string, body: T) => void;
export type ClearError = Callback;

export type Predicate<T> = (value: T) => boolean;

export type ItemOrArray<T> = T | T[];

export type Children = ItemOrArray<React.ReactNode>;

export type PickValue<T, P extends keyof T> = T[P];
export type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;

export const payloadActionOf =
  <P>(type: string): OnPayloadAction<P> => (payload: P) => action<string, P>(type, payload);

export const emptyActionOf = (type: string): OnEmptyAction => () => action<string>(type);

export interface Dictionary<T> {
  [key: string]: T;
}

export interface Id {
  readonly id: string | number;
}

export interface ClassNamed {
  className?: string;
}

export interface WithChildren {
  children?: Children;
}

export interface Styled {
  style?: React.CSSProperties;
}

export interface Titled {
  title?: string;
}

export interface Selectable {
  isSelected?: boolean;
}

export interface Selected {
  selected: boolean;
}

export interface Opened {
  isOpen: boolean;
}

export interface Visible {
  isVisible?: boolean;
}

export interface HasContent {
  hasContent: boolean;
}

export interface Fetching {
  isFetching: boolean;
}

export interface Clickable {
  onClick: OnClick;
}

export interface ClickableEventHandler {
  onClick: OnClickEventHandler;
}

export interface Identifiable {
  readonly id: uuid;
}

export interface IdNamed extends Identifiable {
  readonly name: string;
}

export interface PathNamed {
  pathName: string;
}

export interface ErrorResponse {
  type?: string | number;
  data?: any;
  message: string;
}

export const enum Status {
  ok = 'ok',
  error = 'error',
  warning = 'warning',
  unknown = 'unknown',
}

export const enum UseCases {
  admin = 'admin',
  collection = 'collection',
  dashboard = 'dashboard',
  report = 'report',
  selection = 'selection',
  validation = 'validation',
  otc = 'otc'
}

const status = {
  ok: Status.ok,
  error: Status.error,
  warning: Status.warning,
  unknown: Status.unknown,
};

export const statusFor = (statusCode: uuid): Status =>
  Maybe.maybe(status[statusCode]).orElse(Status.unknown);

export const toIdNamed = (id: string): IdNamed => ({id, name: id});

export const enum Sectors {
  collection = 'collection',
  meterCollection = 'meterCollection',
  collectionStatFacilities = 'collectionStatFacilities',
  meterCollectionStatFacilities = 'meterCollectionStatFacilities',
  summary = 'summary',
  organisationSummary = 'organisationSummary',
  subOrganisations = 'subOrganisations',
  theme = 'theme',
}

export type ActionKey = EndPoints | Sectors;
