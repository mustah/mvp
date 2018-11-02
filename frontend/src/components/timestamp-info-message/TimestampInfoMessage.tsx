import * as React from 'react';
import './TimestampInfoMessage.scss';
import {translate} from '../../services/translationService';
import {RowRight} from '../layouts/row/Row';
import {Small} from '../texts/Texts';

export const TimestampInfoMessage = () => (
  <RowRight className="TimestampInfoMessage">
    <Small className="first-uppercase">
      {translate('all timestamps are displayed in UTC+1')}
    </Small>
  </RowRight>
);
