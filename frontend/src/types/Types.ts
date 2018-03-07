import * as React from 'react';
import {PayloadAction} from 'react-redux-typescript';
import {Maybe} from '../helpers/Maybe';

export type uuid = string | number;

export type OnClick = (...args) => void;
export type OnClickWithId = (id: uuid) => void;
export type Callback = () => void;
export type RenderFunction<T> = (props: T) => Children;

export type Predicate<T> = (value: T) => boolean;

export type ItemOrArray<T> = T | T[];

export type Children = ItemOrArray<React.ReactNode>;

/**
 * Is a payload action with action type of <code>string</code> and payload of type <code><P></code>.
 */
export type Action<P> = PayloadAction<string, P>;

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
  warning = 'warning',
  critical = 'critical',
  info = 'info',
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
  info: Status.info,
  warning: Status.warning,
  critical: Status.critical,
  unknown: Status.unknown,
};

export const statusFor = (statusCode: uuid): Status => {
  return Maybe.maybe(status[statusCode]).orElse(Status.unknown);
};
