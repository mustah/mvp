import * as classNames from 'classnames';
import {Pathname} from 'history';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {ClassNamed} from '../../../../types/Types';
import {routes} from '../../../app/routes';
import {SelectionIconButton} from '../icons/IconSelection';
import {Row} from '../layouts/row/Row';
import {Logo} from '../logo/Logo';
import {Summary} from '../summary/Summary';
import {Normal} from '../texts/Texts';
import './SelectionSearch.scss';

interface SelectionSearchSummaryProps extends ClassNamed {
  pathname: Pathname;
}

const resolveSearchPath = (pathname: Pathname): Pathname =>
  pathname === routes.home ? `search` : `${pathname}/search`;

export const SelectionSearchSummary = (props: SelectionSearchSummaryProps) => (
  <Row className={classNames('SelectionSearch-Container Row-center', props.className)}>
    <Row className="SelectionSearch">
      <Link to={resolveSearchPath(props.pathname)}>
        <SelectionIconButton/>
      </Link>
      <Normal>{translate('selection')}: </Normal>
      <Normal className="Italic">{translate('all')}</Normal>
    </Row>
    <Row className="Row-right flex-1 Summaries">
      <Summary title="Städer" count="10"/>
      <Summary title="Adresser" count="22"/>
      <Summary title="Mätpunkter" count="2321"/>
      <Logo small={true}/>
    </Row>
  </Row>
);
