import * as React from 'react';
import {Dispatch} from 'react-redux';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {Maybe} from '../helpers/Maybe';
import {PageNumbered} from '../state/domain-models-paginated/paginatedDomainModels';

export type uuid = string | number;
export type UnixTimestamp = number;

export type EncodedUriParameters = string;

export type OnClick = (...args) => void;
export type OnChange = OnClick;
export type Callback = () => void;
export type CallbackWith<T> = (result: T) => void;
export type OnClickWithId = CallbackWith<uuid>;
export type CallbackWithId = (id: uuid, parameters?: EncodedUriParameters) => void;
export type CallbackWithIds = (ids: uuid[], parameters?: EncodedUriParameters) => void;
export type CallbackWithData = (requestData: any) => Dispatch<any>;
export type CallbackWithDataAndUrlParameters = (requestData: any, urlParameters: any) => any;
export type RenderFunction<T> = (props: T) => Children;

export type Fetch = (parameters?: EncodedUriParameters) => void;
export type ClearError = () => void;

export type FetchPaginated = (page: number, requestModel?: string) => void;
export type ClearErrorPaginated = (payload: PageNumbered) => void;

export type Predicate<T> = (value: T) => boolean;

export type ItemOrArray<T> = T | T[];

export type Children = ItemOrArray<React.ReactNode>;

export type PickValue<T, P extends keyof T> = T[P];
export type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;

/**
 * Is a payload action with action type of <code>string</code> and payload of type <code><P></code>.
 */
export type Action<P> = PayloadAction<string, P>;
export type OnPayloadAction<P> = (payload: P) => Action<P>;
export type OnEmptyAction = () => EmptyAction<string>;
export type Dispatcher = Dispatch<any>;

export const payloadActionOf = <P>(type: string): OnPayloadAction<P> => createPayloadAction<string, P>(type);
export const emptyActionOf = (type: string): OnEmptyAction => createEmptyAction<string>(type);

export interface Dictionary<T> {
  [key: string]: T;
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

export interface Selectable {
  isSelected?: boolean;
}

export interface Selected {
  selected: boolean;
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

export interface IdNamed {
  readonly id: uuid;
  readonly name: string;
}

export interface Identifiable {
  id: uuid;
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
  dashboard = 'dashboard',
  collection = 'collection',
  validation = 'validation',
  selection = 'selection',
  report = 'report',
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
