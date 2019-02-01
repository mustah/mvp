import {default as classNames} from 'classnames';
import * as React from 'react';
import {translate} from '../../services/translationService';
import {ClassNamed} from '../../types/Types';
import {RowRight} from '../layouts/row/Row';
import {Small} from '../texts/Texts';
import './TimestampInfoMessage.scss';

export const TimestampInfoMessage = ({className}: ClassNamed) => (
  <RowRight className={classNames('TimestampInfoMessage', className)}>
    <Small className="first-uppercase">
      {translate('all timestamps are displayed in UTC+1')}
    </Small>
  </RowRight>
);
