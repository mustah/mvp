import {createHashHistory, History, Location} from 'history';
import {config} from '../config/config';

export const routes = {
  collection: '/collection',
  dashboard: '/dashboard',
  gateway: '/gateway',
  home: '/',
  login: '/login',
  meter: '/meter',
  meters: '/meters',
  mp: '/mp',
  report: '/report',
  validation: '/validation',
  selection: '/selection',
  searchResult: '/search-result',
  userProfile: '/user-profile',
  admin: '/admin',
  adminUsers: '/admin/users',
  adminUsersAdd: '/admin/users/add',
  adminUsersModify: '/admin/users/modify',
  adminOrganisations: '/admin/organisations',
  adminOrganisationsAdd: '/admin/organisations/add',
  adminOrganisationsModify: '/admin/organisations/modify',
  adminMeterDefinitions: '/admin/meter-definitions',
  adminMeterDefinitionsAdd: '/admin/meter-definitions/add',
  adminMeterDefinitionsModify: '/admin/meter-definitions/modify',
};

export const linkToReleaseNotes = 'https://support.elvaco.com/hc/sv/articles/360001339938-Release-notes-EVO';

export const history: History = createHashHistory();

const searchResultRegExp = new RegExp(`${routes.searchResult}.*`);
export const isOnSearchPage = ({pathname}: Location): boolean => pathname.match(searchResultRegExp) !== null;

const meterDetailsRegExp = new RegExp(`${routes.meter}/`);
export const isOnMeterDetailsPage = (pathName: string): boolean => pathName.match(meterDetailsRegExp) !== null;

export const getLoginLogoPath = (slug: string = 'elvaco'): string =>
  `${config().axios.baseURL}/organisations/${slug}/assets/login_logotype`;

export const getLogoPath = (slug: string = 'elvaco'): string =>
  `${config().axios.baseURL}/organisations/${slug}/assets/logotype`;

export const getBackgroundImagePath = (slug: string = 'elvaco'): string =>
  `${config().axios.baseURL}/organisations/${slug}/assets/login_background`;
