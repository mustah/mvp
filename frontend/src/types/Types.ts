export type uuid = string | number;

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
  onClick?: (...args) => void;
}

export interface IdNamed {
  id: uuid;
  name: string;
}

export enum Status {
  ok = 'ok',
  warning = 'warning',
  critical = 'critical',
  info = 'info',
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
