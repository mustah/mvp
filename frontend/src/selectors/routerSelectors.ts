import {RouterState} from 'connected-react-router';
import {Location, Pathname} from 'history';
import {createSelector} from 'reselect';
import {routes} from '../app/routes';

const selectLocation = (state: RouterState): Location => state.location!;

export const getPathname = createSelector<RouterState, Location, Pathname>(
  selectLocation,
  (items) => items.pathname,
);

export const isSelectionPage = createSelector<RouterState, string, boolean>(
  getPathname,
  (pathname) => pathname.match(/\/selection$/) !== null,
);

export const isMeterPage = createSelector<RouterState, string, boolean>(
  getPathname,
  (pathname) => pathname === routes.meters
);

export const isReportPage = createSelector<RouterState, string, boolean>(
  getPathname,
  (pathname) => pathname.match(/\/report$/) !== null || pathname.match(/\/report\/(.*)$/) !== null,
);
