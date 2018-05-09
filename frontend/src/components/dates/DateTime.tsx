import 'CustomPeriodSelector.scss';
import * as React from 'react';
import {displayDate} from '../../helpers/dateHelpers';
import {HasContent} from '../content/HasContent';
import {Normal} from '../texts/Texts';

interface Props {
  date?: string;
  fallbackContent: React.ReactElement<any>;
}

export const DateTime = ({date, fallbackContent}: Props) =>
  (
    <HasContent hasContent={!!date} fallbackContent={fallbackContent}>
      <Normal>{displayDate(date)}</Normal>
    </HasContent>
  );
