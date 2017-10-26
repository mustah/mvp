import * as React from 'react';
import {Separator} from '../../../dashboard/components/separators/Separator';
import {IconCalendar} from '../icons/IconCalendar';
import {Column} from '../layouts/column/Column';
import {Row} from '../layouts/row/Row';
import {Normal} from '../texts/Texts';

export const PeriodSelection = props => (
  <Column>
    <Normal className="uppercase">Period</Normal>
    <Separator/>
    <Row className="Row-center">
      <IconCalendar/>
      <Normal>14 Mar till 13 Apr</Normal>
    </Row>
  </Column>
);
