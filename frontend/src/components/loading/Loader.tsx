import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {Retry, RetryProps} from '../retry/Retry';
import {LoadingLarge} from './Loading';

interface Props {
  children: React.ReactElement<any>;
  isFetching: boolean;
}

export const RetryLoader = ({isFetching, children, error, clearError}: Props & RetryProps) => {
  if (isFetching) {
    return (<RowCenter><LoadingLarge/></RowCenter>);
  } else if (error.isJust()) {
    return <Retry clearError={clearError} error={error}/>;
  } else {
    return children;
  }
};

export const Loader = ({isFetching, children}: Props) => {
  if (isFetching) {
    return (<RowCenter><LoadingLarge/></RowCenter>);
  } else {
    return children;
  }
};
