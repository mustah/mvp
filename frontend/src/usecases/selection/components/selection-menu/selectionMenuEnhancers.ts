import {branch, compose, renderComponent, renderNothing} from 'recompose';
import {ResetSelectionButton} from './SelectionMenuButtons';
import {SelectionMenuProps} from './SelectionMenuSummary';

const isInitialSelection = ({selection: {id}}: SelectionMenuProps): boolean => id === -1;
const isNotChanged = ({selection: {isChanged}}: SelectionMenuProps): boolean => !isChanged;

const whenInitialSelection = branch<SelectionMenuProps>(
  isInitialSelection,
  renderComponent(ResetSelectionButton)
);

const whenNotChanged = branch<SelectionMenuProps>(
  isNotChanged,
  renderNothing
);

export const withResetButtons = compose<SelectionMenuProps, SelectionMenuProps>(
  whenNotChanged,
  whenInitialSelection
);
