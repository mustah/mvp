import * as React from 'react';
import {Separator} from '../../../dashboard/components/separators/Separator';
import {CalendarIcon} from '../icons/CalendarIcon';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';

export const PeriodSelection = props => (
  <Column>
    <Normal className="uppercase">Period</Normal>
    <Separator/>
    <Row className="Row-center">
      <CalendarIcon/>
      <Normal>14 Mar till 13 Apr</Normal>
    </Row>
  </Column>
);
