import * as React from 'react';
import {IconNavigationBack} from '../../../../components/icons/IconNavigationBack';
import {RowCenter, RowMiddle} from '../../../../components/layouts/row/Row';
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
  <RowCenter className="SelectionSearch">
    <IconNavigationBack onClick={closeSelectionPage}/>
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
  </RowCenter>
);
