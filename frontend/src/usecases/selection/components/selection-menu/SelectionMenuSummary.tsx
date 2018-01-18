import * as React from 'react';
import {Row, RowCenter} from '../../../../components/layouts/row/Row';
import {Bold, Normal} from '../../../../components/texts/Texts';
import {firstUpperTranslated, translate} from '../../../../services/translationService';
import {SelectionState} from '../../../../state/search/selection/selectionModels';

interface Props {
  selection: SelectionState;
}

export const SelectionMenuSummary = (props: Props) => {
  const {selection: {name}} = props;
  const selectionName = name === 'all' ? translate('all') : name;

  return (
    <RowCenter className="SelectionMenuSummary">
      <Normal>{firstUpperTranslated('selection')}: </Normal>
      <Row>
        <Bold className="Italic">{selectionName}</Bold>
      </Row>
    </RowCenter>
  );
};
