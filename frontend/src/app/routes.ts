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
  userProfile: '/user-profile',
  admin: '/admin',
  adminUsers: '/admin/users',
  adminUsersAdd: '/admin/users/add',
  adminUsersModify: '/admin/users/modify',
  adminOrganisations: '/admin/organisations',
};

const organisationLogo = {
  'wayne-industries': 'wayne-industries.png',
};

export const getLogoPath = (organisationId: uuid): string =>
  organisationLogo[organisationId] || 'elvaco_logo.png';
