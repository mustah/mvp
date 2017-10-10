import * as React from 'react';
import {PeriodSelection} from '../../common/components/dates/PeriodSelection';
import {Xlarge} from '../../common/components/texts/Texts';
import {Row} from '../../common/components/layouts/row/Row';

export const ValidationOverview = props => (
  <div>
    <Row>
      <Xlarge className="Bold">Vanligaste alarm</Xlarge>
    </Row>
    <Row className="Row-right">
      <PeriodSelection/>
    </Row>
  </div>
);
