import * as React from 'react';
import {Separator} from '../../dashboard/components/separators/Separator';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {Icon} from '../components/icons/Icons';
import {Normal} from '../components/texts/Texts';

export const PeriodSelectionContainer = props => (
  <Column>
    <Normal>Period</Normal>
    <Separator/>
    <Row>
      <Icon name="calendar-range" size="small" className="Row-left"/>
      <Normal>14 Mar till 13 Apr</Normal>
    </Row>
  </Column>
);
