import * as React from 'react';
import {HasContent} from '../../types/Types';

export const withContent =
  <P extends HasContent>(Component: React.ComponentType<P>): React.SFC<P> =>
    (props: P) => props.hasContent
      ? <Component {...props}/>
      : null;
