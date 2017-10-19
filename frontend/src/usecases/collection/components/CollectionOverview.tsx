import * as React from 'react';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Row} from '../../common/components/layouts/row/Row';

export const CollectionOverview = props => (
  <Row className="Row-right">
    <PeriodSelection/>
  </Row>
);
