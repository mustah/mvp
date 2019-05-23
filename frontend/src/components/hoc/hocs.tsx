import * as React from 'react';

/**
 * This is a simple implementation of a Higher Order Component (HOC).
 *
 * Only render this component if the predicate passes. Otherwise return null and do not render.
 */
export const componentOrNothing =
  <P extends {}>(predicate: (props: P) => boolean) =>
    (Component: React.ComponentType<P>): React.FunctionComponent<P> =>
      (props: P) => predicate(props) ? <Component {...props}/> : null;
