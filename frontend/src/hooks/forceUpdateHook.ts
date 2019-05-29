import * as React from 'react';

export const useForceUpdate = () => {
  const [ignore, forceUpdate] = React.useReducer(x => x + 1, 0);
  return () => forceUpdate(ignore);
};
