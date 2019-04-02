import {createHashHistory, History, Location} from 'history';
import evoLogoBlue from '../assets/images/elvaco.evo_logo_blue_cmyk.svg';
import evoLogo from '../assets/images/elvaco.evo_logo_wt.svg';
import wayneIndustries from '../assets/images/wayne-industries.png';
import {uuid} from '../types/Types';

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

export const history: History = createHashHistory();

const searchResultRegExp = new RegExp(`${routes.searchResult}.*`);
export const isOnSearchPage = ({pathname}: Location): boolean => pathname.match(searchResultRegExp) !== null;

const meterDetailsRegExp = new RegExp(`${routes.meter}/`);
export const isOnMeterDetailsPage = (pathName: string): boolean => pathName.match(meterDetailsRegExp) !== null;

const organisationLogo = {
  'wayne-industries': wayneIndustries,
};

export const getLogoPath = (organisationId: uuid): string =>
  organisationLogo[organisationId] || evoLogo;

export const getLoginLogoPath = (organisationId: uuid): string =>
  organisationLogo[organisationId] || evoLogoBlue;
