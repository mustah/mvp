import * as React from 'react';
import {HasContent} from '../../types/Types';

export const withContent =
  <P extends {}>(Component: React.ComponentType<P>): React.SFC<P & HasContent> =>
    ({hasContent, ...props}: HasContent) =>
      hasContent ? <Component {...props}/> : null;
