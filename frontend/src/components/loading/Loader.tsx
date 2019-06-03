import * as React from 'react';
import {Fetching, Styled, WithChildren} from '../../types/Types';
import {RowCenter} from '../layouts/row/Row';
import {Retry, RetryProps} from '../retry/Retry';
import {LoadingLarge} from './Loading';

interface Props extends Fetching {
  children: React.ReactElement<any>;
}

export const LargeContentLoader = ({style}: Styled) =>
  <RowCenter style={style}><LoadingLarge/></RowCenter>;

export const RetryLoader = ({children, clearError, error, isFetching}: Props & RetryProps) => {
  if (isFetching) {
    return <LargeContentLoader/>;
  } else if (error.isJust()) {
    return <Retry clearError={clearError} error={error}/>;
  } else {
    return children;
  }
};

export const Loader = ({children, isFetching}: Props & WithChildren) => {
  if (isFetching) {
    return <LargeContentLoader/>;
  } else {
    return children;
  }
};
