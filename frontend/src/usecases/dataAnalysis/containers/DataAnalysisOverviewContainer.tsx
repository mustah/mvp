import * as React from 'react';
import {Bold, Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';

export const DataAnalysisOverviewContainer = props => (
  <div>
    <Row>
      <Xlarge><Bold>Förbrukning</Bold></Xlarge>
    </Row>
    <Row className="Row-right">
      <PeriodSelectionContainer/>
    </Row>
  </div>
);
