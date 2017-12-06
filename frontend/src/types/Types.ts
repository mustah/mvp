import * as React from 'react';
import {PayloadAction} from 'react-redux-typescript';

export type uuid = string | number;

export type OnClick = (...args) => void;
export type OnClickWithId = (id: uuid) => void;
export type Callback = () => void;

// React helper types
export type Children = React.ReactNode | React.ReactNode[];
/**
 * Your event handlers will be passed instances of SyntheticEvent,
 * a cross-browser wrapper around the browser’s native event.
 */
export type NativeEvent = React.SyntheticEvent<any>;

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
