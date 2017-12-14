import * as React from 'react';
import {PayloadAction} from 'react-redux-typescript';

export type uuid = string | number;

export type OnClick = (...args) => void;
export type OnClickWithId = (id: uuid) => void;
export type Callback = () => void;

export type Predicate<T> = (value: T) => boolean;

export type ItemOrArray<T> = T | T[];

export type Children = ItemOrArray<React.ReactNode>;

/**
 * Is a payload action with action type of <code>string</code> and payload of type <code><P></code>.
 */
export type Action<P> = PayloadAction<string, P>;

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

export const enum Period {
  latest = 'latest',
  currentMonth = 'current_month',
  previousMonth = 'previous_month',
  currentWeek = 'current_week',
  previous7Days = 'previous_7_days',
  custom = 'custom',
}

export const enum UseCases {
  dashboard = 'dashboard',
  collection = 'collection',
  validation = 'validation',
  selection = 'selection',
  report = 'report',
}

const status = {
  0: Status.ok,
  1: Status.info,
  2: Status.warning,
  3: Status.critical,
  4: Status.unknown,
  10: Status.ok,
  11: Status.warning,
};

export const statusFor = (statusCode: uuid): Status | null => status[statusCode];
