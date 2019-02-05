import evoBetaLogo from '../assets/images/evo_beta_logo.svg';
import evoBetaLogoBlue from '../assets/images/evo_beta_logo_blue.svg';
import wayneIndustries from '../assets/images/wayne-industries.png';
import {uuid} from '../types/Types';

export const routes = {
  collection: '/collection',
  dashboard: '/dashboard',
  gateway: '/gateway',
  home: '/',
  login: '/login',
  meter: '/meter',
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

const organisationLogo = {
  'wayne-industries': wayneIndustries,
};

export const getLogoPath = (organisationId: uuid): string =>
  organisationLogo[organisationId] || evoBetaLogo;

export const getLoginLogoPath = (organisationId: uuid): string =>
  organisationLogo[organisationId] || evoBetaLogoBlue;
