import * as React from 'react';
import {Fetching} from '../../types/Types';
import {RowCenter} from '../layouts/row/Row';
import {Retry, RetryProps} from '../retry/Retry';
import {LoadingLarge} from './Loading';

interface Props extends Fetching {
  children: React.ReactElement<any>;
}

export type RetryLoaderProps = Props & RetryProps;

export const RetryLoader = ({children, clearError, error, isFetching}: RetryLoaderProps) => {
  if (isFetching) {
    return (<RowCenter><LoadingLarge/></RowCenter>);
  } else if (error.isJust()) {
    return <Retry clearError={clearError} error={error}/>;
  } else {
    return children;
  }
};

export const Loader = ({children, isFetching}: Props) => {
  if (isFetching) {
    return (<RowCenter><LoadingLarge/></RowCenter>);
  } else {
    return children;
  }
};
