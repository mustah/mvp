export interface ClassNamed {
  className?: string;
}

export interface Expandable {
  isExpanded?: boolean;
}

export interface Selectable {
  isSelected?: boolean;
}

export enum State {
  ok = 'ok',
  warning = 'warning',
  crititcal = 'critical',
  info = 'info',
}

// TODO this is up for refactoring:
// - we want a more solid number -> string, and also string -> number connection
// - this implementation uses an ad-hoc Maybe structure, which is not used
//   in other places throughout the code
export const states = (numeric: number): { valid: boolean, state?: State } => {
  switch (numeric) {
    case 0:
      return {valid: true, state: State.ok};
    case 1:
      return {valid: true, state: State.info};
    case 2:
      return {valid: true, state: State.warning};
    case 3:
      return {valid: true, state: State.crititcal};
    case 10:
      return {valid: true, state: State.ok};
    case 11:
      return {valid: true, state: State.warning};
    default:
      return {valid: false};
  }
};
