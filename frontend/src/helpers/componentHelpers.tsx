import * as React from 'react';

/**
 * This is a Higher Order Component composition pattern (HOC)
 */
export function wrapComponent<T>(Component: React.StatelessComponent<T>): React.StatelessComponent<T> {
  return (props) => <Component {...props}/>;
}
