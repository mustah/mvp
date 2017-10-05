import * as React from 'react';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Bold, Xlarge} from '../../common/components/texts/Texts';
import {Row} from '../../layouts/components/row/Row';

export const CollectionOverview = props => (
  <div>
    <Row>
      <Xlarge><Bold>Vanligaste fel & varningar</Bold></Xlarge>
    </Row>
    <Row className="Row-right">
      <PeriodSelection/>
    </Row>
  </div>
);
