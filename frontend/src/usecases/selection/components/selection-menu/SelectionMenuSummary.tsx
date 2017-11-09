import {Pathname} from 'history';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {SelectionState} from '../../../../state/search/selection/selectionModels';
import {routes} from '../../../app/routes';
import {SelectionIconButton} from '../../../common/components/icons/IconSelection';
import {Row, RowCenter} from '../../../common/components/layouts/row/Row';
import {Normal} from '../../../common/components/texts/Texts';

interface Props {
  pathname: Pathname;
  selection: SelectionState;
}

const resolveSearchPath = (pathname: Pathname): Pathname =>
  pathname === routes.home ? `search` : `${pathname}/search`;

export const SelectionMenuSummary = (props: Props) => {
  const {selection: {name}} = props;
  const selectionName = name === 'all' ? translate('all') : name;

  return (
    <RowCenter className="SelectionMenuSummary">
      <Link to={resolveSearchPath(props.pathname)}>
        <SelectionIconButton/>
      </Link>
      <Normal>{translate('selection')}: </Normal>
      <Row>
        <Normal className="Italic">{selectionName}</Normal>
      </Row>
    </RowCenter>
  );
};
