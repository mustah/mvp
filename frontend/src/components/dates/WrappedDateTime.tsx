import 'CustomPeriodSelector.scss';
import * as React from 'react';
import {displayDate} from '../../helpers/dateHelpers';
import {HasContent} from '../../types/Types';
import {withEmptyContentComponent} from '../hoc/withEmptyContent';
import {Separator} from '../separators/Separator';
import {Normal} from '../texts/Texts';

interface Props {
  date?: string;
}

const DateTime = ({date}: Props) => <Normal>{displayDate(date)}</Normal>;

const WrappedComponent = withEmptyContentComponent<Props & HasContent>(
  DateTime,
  Separator,
);

export const WrappedDateTime = (props: Props & HasContent) => <WrappedComponent {...props}/>;
