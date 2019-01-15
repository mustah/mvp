import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {IconSelection} from '../../../../components/icons/IconSelection';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Bold, Normal} from '../../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../../services/translationService';
import {UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {OnClick, OnClickWithId} from '../../../../types/Types';
import {DiscardChangesButton} from './SelectionMenuButtons';
import {withResetButtons} from './selectionMenuEnhancers';

export interface SelectionMenuProps {
  selection: UserSelection;
  resetSelection: OnClick;
  selectSavedSelection: OnClickWithId;
}

const ResetButtons = withResetButtons(DiscardChangesButton);

export const SelectionMenuSummary = (props: SelectionMenuProps) => {
  const {selection: {id, name}, selectSavedSelection} = props;
  const selectionName = id === -1 ? firstUpperTranslated('all') : name;
  const resetToSelection = () => selectSavedSelection(id);

  return (
    <RowCenter className="SelectionSearch SelectionMenuSummary">
      <Link to={routes.selection} className="link SelectionIcon-margin">
        <IconSelection/>
      </Link>
      <Normal>{firstUpperTranslated('selection')}: </Normal>
      <Row>
        <Bold className="Italic">{selectionName}</Bold>
        <ResetButtons {...props} selectSavedSelection={resetToSelection}/>
      </Row>
    </RowCenter>
  );
};
