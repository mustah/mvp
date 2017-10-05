export interface ClassNamed {
  className?: string;
}

export interface Expandable {
  isExpanded?: boolean;
}

export interface Selectable {
  isSelected?: boolean;
}

export type State = 'ok' | 'warning' | 'critical' | 'info';

// TODO this is up for refactoring:
// - we want a more solid number -> string, and also string -> number connection
// - this implementation uses an ad-hoc Maybe structure, which is not used
//   in other places throughout the code
export const States = (numeric: number): { valid: boolean, state?: State } => {
  let maybeState: State;
  switch (numeric) {
    case 0:
      maybeState = 'ok';
      break;
    case 1:
      maybeState = 'info';
      break;
    case 2:
      maybeState = 'warning';
      break;
    case 3:
      maybeState = 'critical';
      break;
    default:
      return {valid: false};
  }
  return {valid: true, state: maybeState};
};
