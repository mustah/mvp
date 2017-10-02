import * as React from 'react';
import {Separator} from '../../../dashboard/components/separators/Separator';
import {Column} from '../../../layouts/components/column/Column';
import {Row} from '../../../layouts/components/row/Row';
import {Icon} from '../icons/Icons';
import {Normal} from '../texts/Texts';

export const PeriodSelection = props => (
  <Column>
    <Normal className="uppercase">Period</Normal>
    <Separator/>
    <Row className="Row-center">
      <Icon name="calendar-range" size="small" className="Row-left"/>
      <Normal>14 Mar till 13 Apr</Normal>
    </Row>
  </Column>
);
