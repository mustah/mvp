import {Location} from 'history';
import * as React from 'react';
import {Link} from 'react-router-dom';
import {translate} from '../../../../services/translationService';
import {SearchIconButton} from '../icons/SearchIconButton';
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
        <SearchIconButton/>
      </Link>
      <Normal>{translate('all')}</Normal>
    </Row>
    <Row className="Row-right flex-1">
      <Summary title="Områden" count="52"/>
      <Summary title="Objekt" count="431"/>
      <Summary title="Anläggningar" count="2321"/>
      <Summary title="Mätserier" count="3723"/>
    </Row>
  </Row>
);
