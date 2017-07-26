import * as React from 'react';
import {Row} from '../../../usecases/layouts/components/row/Row';
import {SummaryContainer} from '../containers/SummaryContainer';
import './SelectionsOverview.scss';

export const SelectionsOverview = props => (
  <Row className="selection-container Row-center">
    <div className="SelectionsOverview">Urval: {props.title}</div>
    <SummaryContainer/>
  </Row>
);
