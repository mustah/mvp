import * as React from 'react';
import {Xlarge} from '../../common/components/texts/Texts';
import {PeriodSelectionContainer} from '../../common/containers/PeriodSelectionContainer';
import {Row} from '../../layouts/components/row/Row';

export const DataAnalysisOverviewContainer = props => (
  <div>
    <Row>
      <Xlarge className="Bold">Förbrukning</Xlarge>
    </Row>
    <Row className="Row-right">
      <PeriodSelectionContainer/>
    </Row>
  </div>
);
