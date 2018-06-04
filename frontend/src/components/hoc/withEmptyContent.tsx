import * as React from 'react';
import {HasContent} from '../../types/Types';
import {EmptyContent, EmptyContentProps} from '../error-message/EmptyContent';

export type WithEmptyContentProps = HasContent & EmptyContentProps;

export const withEmptyContent =
  <P extends WithEmptyContentProps>(Component: React.ComponentType<P>): React.SFC<P> =>
    (props: P) => props.hasContent
      ? (<Component {...props}/>)
      : (<EmptyContent {...props}/>);
