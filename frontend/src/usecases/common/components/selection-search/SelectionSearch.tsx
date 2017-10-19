import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {CloseIconButton} from '../icons/IconClose';
import {Row, RowCenter} from '../layouts/row/Row';
import {Summary} from '../summary/Summary';
import {Normal} from '../texts/Texts';
import './SelectionSearch.scss';

interface OwnProps {
  close: (...args) => void;
}

export const SelectionSearch = (props: OwnProps) => (
  <RowCenter className="SelectionSearch-Container">
    <Row className="SelectionSearch">
      <CloseIconButton onClick={props.close}/>
      <Normal className="Italic clickable">{translate('new search')}*</Normal>
    </Row>
    <Row className="Row-right flex-1 Summaries">
      <Summary title="Städer" count="10"/>
      <Summary title="Adresser" count="22"/>
      <Summary title="Mätpunkter" count="2321"/>
    </Row>
  </RowCenter>
);
