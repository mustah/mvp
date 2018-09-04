import * as React from 'react';
import 'TableInfoText.scss';
import {translate} from '../../services/translationService';
import {RowRight} from '../layouts/row/Row';
import {Small} from '../texts/Texts';

export const TableInfoText = () => (
  <RowRight className="TableInfoText">
    <Small className="first-uppercase">
      {translate('all timestamps are displayed in UTC+1')}
    </Small>
  </RowRight>
);
