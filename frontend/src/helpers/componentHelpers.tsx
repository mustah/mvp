import * as React from 'react';

/**
 * This is a Higher Order Component composition pattern (HOC)
 */
export function wrapComponent<P>(WrappedComponent: React.StatelessComponent<P>): React.StatelessComponent<P> {
  return (props: P) => <WrappedComponent {...props}/>;
}
