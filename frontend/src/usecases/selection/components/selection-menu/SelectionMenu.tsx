import * as React from 'react';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {initialSelectionId} from '../../../../state/user-selection/userSelectionModels';
import {SelectionMenuProps} from '../../containers/SelectionMenuContainer';
import {InlineEditInput} from './InlineEditInput';

export const SelectionMenu = ({
  selection,
  saveSelection,
  updateSelection,
  resetSelection,
  selectSavedSelection,
}: SelectionMenuProps) => (
  <RowMiddle>
    <InlineEditInput
      key={`${selection.id}-${selection.id === initialSelectionId ? '' : selection.isChanged}`}
      isChanged={selection.isChanged}
      selection={selection}
      saveSelection={saveSelection}
      updateSelection={updateSelection}
      resetSelection={resetSelection}
      selectSavedSelection={selectSavedSelection}
    />
  </RowMiddle>
);
