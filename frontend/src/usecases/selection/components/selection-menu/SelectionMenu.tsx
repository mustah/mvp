import * as React from 'react';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {SelectionMenuProps} from '../../containers/SelectionMenuContainer';
import {InlineEditInput} from './InlineEditInput';

export const SelectionMenu = ({
  closeSelectionPage,
  selection,
  saveSelection,
  updateSelection,
  resetSelection,
  selectSavedSelection,
}: SelectionMenuProps) => (
  <RowMiddle>
    <InlineEditInput
      key={`${selection.id}-${selection.isChanged}`}
      isChanged={selection.isChanged}
      selection={selection}
      saveSelection={saveSelection}
      updateSelection={updateSelection}
      resetSelection={resetSelection}
      selectSavedSelection={selectSavedSelection}
    />
  </RowMiddle>
);
