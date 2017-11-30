import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {Loading} from './Loading';

interface Props {
  children: React.ReactElement<any>;
  isFetching: boolean;
}

export const Loader = (props: Props) => {
  const {isFetching, children} = props;
  if (isFetching) {
    return (<RowCenter><Loading/></RowCenter>);
  } else {
    return children;
  }
};
