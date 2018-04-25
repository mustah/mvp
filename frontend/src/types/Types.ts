import * as React from 'react';
import {createEmptyAction, createPayloadAction, EmptyAction, PayloadAction} from 'react-redux-typescript';
import {Maybe} from '../helpers/Maybe';
import {HasPageNumber} from '../state/domain-models-paginated/paginatedDomainModels';

export type uuid = string | number;

export type EncodedUriParameters = string;

export type OnClick = (...args) => void;
export type OnClickWithId = (id: uuid) => void;
export type Callback = () => void;
export type RenderFunction<T> = (props: T) => Children;

export type CallbackWithId = (id: uuid) => void;

export type Fetch = (parameters?: EncodedUriParameters) => void;
export type ClearError = () => void;

export type FetchPaginated = (page: number, requestModel?: string) => void;
export type ClearErrorPaginated = (payload: HasPageNumber) => void;

export type Predicate<T> = (value: T) => boolean;

export type ItemOrArray<T> = T | T[];

export type Children = ItemOrArray<React.ReactNode>;

export type PickValue<T, P extends keyof T> = T[P];

/**
 * Is a payload action with action type of <code>string</code> and payload of type <code><P></code>.
 */
export type Action<P> = PayloadAction<string, P>;
export type OnPayloadAction<P> = (payload: P) => Action<P>;
export type OnEmptyAction = () => EmptyAction<string>;

export const payloadActionOf = <P>(type: string): OnPayloadAction<P> => createPayloadAction(type);
export const emptyActionOf = (type: string): OnEmptyAction => createEmptyAction(type);

export interface Dictionary<T> {
  [key: string]: T;
}

export interface ClassNamed {
  className?: string;
}

export interface Selectable {
  isSelected?: boolean;
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
  alarm = 'alarm',
  active = 'active',
  info = 'info',
  error = 'error',
  warning = 'warning',
  critical = 'critical',
  unknown = 'unknown',
  maintenance_scheduled = 'maintenance_scheduled',
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
  active: Status.active,
  error: Status.error,
  info: Status.info,
  warning: Status.warning,
  critical: Status.critical,
  unknown: Status.unknown,
  maintenance_scheduled: Status.info,
};

export const statusFor = (statusCode: uuid): Status => {
  return Maybe.maybe(status[statusCode]).orElse(Status.unknown);
};
