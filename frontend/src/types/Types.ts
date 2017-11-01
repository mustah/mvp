export type uuid = string | number;

export type OnClick = (...args) => void;

export interface ClassNamed {
  className?: string;
}

export interface Expandable {
  isExpanded?: boolean;
}

export interface Selectable {
  isSelected?: boolean;
}

export interface Clickable {
  onClick: OnClick;
}

export interface IdNamed {
  id: uuid;
  name: string;
}

export interface ErrorResponse {
  type?: string | number;
  data?: any;
  message: string;
}

export enum Status {
  ok = 'ok',
  warning = 'warning',
  critical = 'critical',
  info = 'info',
}

export enum Period {
  now = 'now',
  week = 'week',
  month = 'month',
  quarter = 'quarter',
}

const status = {
  0: Status.ok,
  1: Status.info,
  2: Status.warning,
  3: Status.critical,
  10: Status.ok,
  11: Status.warning,
};

export const statusFor = (statusCode: number): Status | null => status[statusCode];
