import * as React from 'react';
import {Bold, Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';

export const CollectionOverviewContainer = props => (
  <div>
    <Row>
      <Xlarge><Bold>Vanligaste fel & varningar</Bold></Xlarge>
    </Row>
    <Row className="Row-right">
      <PeriodSelectionContainer/>
    </Row>
  </div>
);
