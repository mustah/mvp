import * as React from 'react';
import {useResizeWindow} from '../../hooks/resizeWindowHook';
import {ClassNamed, WithChildren} from '../../types/Types';
import {ColumnCenter} from '../layouts/column/Column';

interface Props extends ClassNamed, WithChildren {
  key?: string;
  paddingBottom?: number;
}

export const ResponsiveContentHeight = ({children, className, key, paddingBottom = 276}: Props) => {
  const {height: innerHeight, resized} = useResizeWindow();
  const height = innerHeight - paddingBottom;
  return (
    <ColumnCenter className={className} style={{height}} key={`${key}-${resized}`}>
      {children}
    </ColumnCenter>
  );
};
