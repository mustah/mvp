import * as React from 'react';

/**
 * This is a Higher Order Component composition pattern (HOC)
 */
export function wrapComponent<P>(WrappedComponent: React.ComponentType<P>): React.ComponentType<P> {
  return (props: P) => <WrappedComponent {...props}/>;
}
