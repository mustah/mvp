import * as React from 'react';
import {Row} from '../layouts/row/Row';
import {Summary} from '../summary/Summary';
import './SelectionOverview.scss';

export const SelectionOverview = props => (
  <Row className="selection-container Row-center">
    <div className="SelectionOverview">Urval: {props.title}</div>
    <Row className="Row-right flex-1">
      <Summary title="Områden" count="52"/>
      <Summary title="Objekt" count="431"/>
      <Summary title="Anläggningar" count="2321"/>
      <Summary title="Mätserier" count="3723"/>
    </Row>
  </Row>
);
