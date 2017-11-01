import {Pathname} from 'history';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {routes} from '../../../app/routes';
import {SelectionIconButton} from '../../../common/components/icons/IconSelection';
import {RowCenter} from '../../../common/components/layouts/row/Row';
import {Normal} from '../../../common/components/texts/Texts';

interface Props {
  pathname: Pathname;
}

const resolveSearchPath = (pathname: Pathname): Pathname =>
  pathname === routes.home ? `search` : `${pathname}/search`;

export const SelectionMenuSummary = (props: Props) => (
  <RowCenter>
    <Link to={resolveSearchPath(props.pathname)}>
      <SelectionIconButton/>
    </Link>
    <Normal>{translate('selection')}: </Normal>
    <Normal className="Italic">{translate('all')}</Normal>
  </RowCenter>
);
