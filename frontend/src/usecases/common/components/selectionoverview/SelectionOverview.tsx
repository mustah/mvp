import * as React from 'react';
import {Row} from '../../../layouts/components/row/Row';
import {SummaryContainer} from '../../containers/SummaryContainer';
import './SelectionOverview.scss';

export const SelectionOverview = props => (
  <Row className="selection-container Row-center">
    <div className="SelectionOverview">Urval: {props.title}</div>
    <SummaryContainer/>
  </Row>
);
