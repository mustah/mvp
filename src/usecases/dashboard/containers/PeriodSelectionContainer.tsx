import * as React from 'react';
import {Icon} from '../../common/components/icons/Icons';
import {Normal} from '../../common/components/texts/Texts';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {Separator} from '../components/separators/Separator';

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
