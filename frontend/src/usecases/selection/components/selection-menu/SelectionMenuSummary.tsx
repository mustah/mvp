import {Pathname} from 'history';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {IdNamed} from '../../../../types/Types';
import {routes} from '../../../app/routes';
import {SelectionIconButton} from '../../../common/components/icons/IconSelection';
import {RowCenter} from '../../../common/components/layouts/row/Row';
import {Normal} from '../../../common/components/texts/Texts';

interface Props {
  pathname: Pathname;
  currentSelection: IdNamed;
}

const resolveSearchPath = (pathname: Pathname): Pathname =>
  pathname === routes.home ? `search` : `${pathname}/search`;

export const SelectionMenuSummary = (props: Props) => {
  const {currentSelection: {name}} = props;
  const selectedName = name === 'all' ? translate('all') : name;

  return (
    <RowCenter>
      <Link to={resolveSearchPath(props.pathname)}>
        <SelectionIconButton/>
      </Link>
      <Normal>{translate('selection')}: </Normal>
      <Normal className="Italic">{selectedName}</Normal>
    </RowCenter>
  );
};
