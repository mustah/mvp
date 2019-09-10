import * as React from 'react';
import {InfiniteLoader, InfiniteLoaderProps} from 'react-virtualized';
import {withListItemLoader} from '../hoc/withLoaders';
import {Row} from '../layouts/row/Row';

type Props = InfiniteLoaderProps;

export const DropdownList = withListItemLoader(({children, ...infiniteLoaderProps}: Props) => (
  <Row>
    <InfiniteLoader {...infiniteLoaderProps}>
      {children}
    </InfiniteLoader>
  </Row>
));
