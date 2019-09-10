import * as React from 'react';
import {Fetching} from '../../types/Types';
import {RowCenter} from '../layouts/row/Row';
import {LoadingLarge, LoadingSmall, LoadingWidget} from '../loading/Loading';

const LargeLoader = () => <RowCenter><LoadingLarge/></RowCenter>;
const WidgetLoader = () => <RowCenter><LoadingWidget/></RowCenter>;
const ListItemLoader = () => <RowCenter><LoadingSmall/></RowCenter>;

const withLoader =
  <P extends {}>(
    LoadingComponent: React.ComponentType<{}>,
    Component: React.ComponentType<P>,
  ): React.ComponentType<P & Fetching> =>
    ({isFetching, ...props}: Fetching) =>
      isFetching ? <LoadingComponent/> : <Component {...props as P}/>;

export const withLargeLoader =
  <P extends {}>(Component: React.ComponentType<P>): React.ComponentType<P & Fetching> =>
    withLoader(LargeLoader, Component);

export const withWidgetLoader =
  <P extends {}>(Component: React.ComponentType<P>): React.ComponentType<P & Fetching> =>
    withLoader(WidgetLoader, Component);

export const withListItemLoader =
  <P extends {}>(Component: React.ComponentType<P>): React.ComponentType<P & Fetching> =>
    withLoader(ListItemLoader, Component);
