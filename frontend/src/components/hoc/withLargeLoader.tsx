import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {LoadingLarge} from '../loading/Loading';

interface Fetching {
  isFetching: boolean;
}

export const withLargeLoader =
  <P extends {}>(Component: React.ComponentType<P>): React.SFC<P & Fetching> =>
    ({isFetching, ...props}: Fetching) =>
      isFetching
        ? <RowCenter><LoadingLarge/></RowCenter>
        : <Component {...props}/>;
