import {Location, Pathname} from 'history';
import {RouterState} from 'react-router-redux';
import {createSelector} from 'reselect';

const selectLocation = (state: RouterState): Location => state.location!;

export const getPathname = createSelector<RouterState, Location, Pathname>(
  selectLocation,
  items => items.pathname,
);

export const isSelectionPage = createSelector<RouterState, string, boolean>(
  getPathname,
  pathname => pathname.match(/\/selection$/) !== null,
);
