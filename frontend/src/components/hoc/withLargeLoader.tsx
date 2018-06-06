import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {LoadingLarge} from '../loading/Loading';

interface Fetching {
  isFetching: boolean;
}

export const withLargeLoader =
  <P extends Fetching>(Component: React.ComponentType<P>): React.SFC<P> =>
    (props: P) => props.isFetching
      ? (<RowCenter><LoadingLarge/></RowCenter>)
      : (<Component {...props}/>);
