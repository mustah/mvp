import * as React from 'react';
import {Link} from 'react-router-dom';
import {routes} from '../../../../app/routes';
import {ButtonLink} from '../../../../components/buttons/ButtonLink';
import {IconSelection} from '../../../../components/icons/IconSelection';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Bold, Normal} from '../../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {UserSelection} from '../../../../state/user-selection/userSelectionModels';
import {OnClick, OnClickWithId, uuid} from '../../../../types/Types';

interface Props {
  selection: UserSelection;
  resetSelection: OnClick;
  selectSavedSelection: OnClickWithId;
}

export const SelectionMenuSummary = (props: Props) => {
  const {selection: {id, name, isChanged}, resetSelection, selectSavedSelection} = props;
  const selectionName = id === -1 ? firstUpperTranslated('all') : name;

  const isInitialSelection = (id: uuid) => id === -1;

  const renderReset = (): React.ReactNode => {
    const resetToSelection = () => selectSavedSelection(id);
    return isInitialSelection(id) ?
      <ButtonLink onClick={resetSelection}>{translate('reset selection')}</ButtonLink> :
      <ButtonLink onClick={resetToSelection}>{translate('discard changes')}</ButtonLink>;
  };

  return (
    <RowCenter className="SelectionMenuSummary">
      <Link to={routes.selection} className="link SelectionIcon-margin">
        <IconSelection />
      </Link>
      <Normal>{firstUpperTranslated('selection')}: </Normal>
      <Row>
        <Bold className="Italic">{selectionName}</Bold>
        {isChanged && renderReset()}
      </Row>
    </RowCenter>
  );
};
