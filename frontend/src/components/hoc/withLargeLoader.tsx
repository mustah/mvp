import * as React from 'react';
import {RowCenter} from '../layouts/row/Row';
import {LoadingLarge, LoadingSmall} from '../loading/Loading';

interface Fetching {
  isFetching: boolean;
}

const LargeLoader = () => <RowCenter><LoadingLarge/></RowCenter>;
const SmallLoader = () => <RowCenter><LoadingSmall/></RowCenter>;

const withLoader =
  <P extends {}>(
    LoadingComponent: React.ComponentType<{}>,
    Component: React.ComponentType<P>,
  ): React.SFC<P & Fetching> =>
    ({isFetching, ...props}: Fetching) =>
      isFetching ? <LoadingComponent/> : <Component {...props}/>;

export const withLargeLoader =
  <P extends {}>(Component: React.ComponentType<P>): React.SFC<P & Fetching> =>
    withLoader(LargeLoader, Component);

export const withSmallLoader =
  <P extends {}>(Component: React.ComponentType<P>): React.SFC<P & Fetching> =>
    withLoader(SmallLoader, Component);
