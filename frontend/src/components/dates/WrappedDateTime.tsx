import * as React from 'react';
import {displayDate} from '../../helpers/dateHelpers';
import {HasContent, Styled} from '../../types/Types';
import {withEmptyContentComponent} from '../hoc/withEmptyContent';
import {Separator} from '../separators/Separator';
import {Normal} from '../texts/Texts';
import './CustomPeriodSelector.scss';

interface Props extends HasContent, Styled {
  date?: string;
}

const DateTime = ({date}: Props) => <Normal>{displayDate(date)}</Normal>;

const WrappedComponent = withEmptyContentComponent<Props>(DateTime, Separator);

export const WrappedDateTime = (props: Props) => <WrappedComponent {...props}/>;
