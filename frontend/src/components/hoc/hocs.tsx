import * as React from 'react';

/**
 * This is a simple implementation of a Higher Order Component (HOC).
 *
 * Only render this component if the user is a super admin. Otherwise return null and do not render.
 */
export const testOrNull =
  <P extends {}>(test: (props: P) => boolean) =>
    (Component: React.ComponentType<P>): React.SFC<P> =>
      (props: P) => test(props) ? <Component {...props} /> : null;
