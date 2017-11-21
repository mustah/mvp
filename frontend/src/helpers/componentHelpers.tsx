import * as React from 'react';

/**
 * This is a Higher Order Component composition pattern (HOC)
 */
export function wrapComponent<P>(Component: React.StatelessComponent<P>): React.StatelessComponent<P> {
  return (props: P) => <Component {...props}/>;
}
