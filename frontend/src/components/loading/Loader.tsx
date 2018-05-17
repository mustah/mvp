import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {Retry, RetryProps} from '../retry/Retry';
import {LoadingLarge} from './Loading';

interface Props extends RetryProps {
  children: React.ReactElement<any>;
  isFetching: boolean;
}

export const Loader = ({isFetching, children, error, clearError}: Props) => {
  if (isFetching) {
    return (<RowCenter><LoadingLarge/></RowCenter>);
  } else if (error.isJust()) {
    return <Retry clearError={clearError} error={error}/>;
  } else {
    return children;
  }
};
