import * as React from 'react';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Xlarge} from '../../common/components/texts/Texts';
import {Row} from '../../layouts/components/row/Row';

export const ReportOverview = props => (
  <div>
    <Row>
      <Xlarge className="Bold">Förbrukning</Xlarge>
    </Row>
    <Row className="Row-right">
      <PeriodSelection/>
    </Row>
  </div>
);
