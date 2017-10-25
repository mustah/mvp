import {Location} from 'history';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {Logo} from '../../../branding/components/Logo';
import {SelectionIconButton} from '../icons/IconSelection';
import {Row} from '../layouts/row/Row';
import {Summary} from '../summary/Summary';
import {Normal} from '../texts/Texts';
import './SelectionSearch.scss';

interface SelectionSearchSummaryProps {
  location: Location;
}

export const SelectionSearchSummary = (props: SelectionSearchSummaryProps) => (
  <Row className="SelectionSearch-Container Row-center">
    <Row className="SelectionSearch">
      <Link to={`${props.location.pathname}/search`}>
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
