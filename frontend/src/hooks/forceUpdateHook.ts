import * as React from 'react';

export const useForceUpdate = () => React.useState(null)[1];
