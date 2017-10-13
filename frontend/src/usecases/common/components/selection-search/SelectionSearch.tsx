import * as React from 'react';
import {translate} from '../../../../services/translationService';
import {CloseIconButton} from '../icons/CloseIconButton';
import {SearchIconButton} from '../icons/SearchIconButton';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';
import './SelectionSearch.scss';

interface OwnProps {
  close: (...args) => void;
}

export const SelectionSearch = (props: OwnProps) => (
  <Row className="SelectionSearch-Container Row-center">
    <Row className="SelectionSearch">
      <SearchIconButton/>
      <Normal className="clickable">{translate('new search')}</Normal>
    </Row>
    <Row className="Row-right flex-1">
      <CloseIconButton onClick={props.close}/>
    </Row>
  </Row>
);
