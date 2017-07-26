import * as React from 'react';
import {Row} from '../../layouts/components/row/Row';
import {Summary} from '../components/Summary';
import './Summary.scss';

export const SummaryContainer = props => (
  <Row className="Row-right flex-1">
    <Summary title="Områden" count="52"/>
    <Summary title="Objekt" count="431"/>
    <Summary title="Anläggningar" count="2321"/>
    <Summary title="Mätserier" count="3723"/>
  </Row>
);
