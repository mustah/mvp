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
  adminOrganisationsAdd: '/admin/organisations/add',
};

export const assetsPathFor = (image: string): string => `assets/images/${image}`;

const organisationLogo = {
  'wayne-industries': assetsPathFor('wayne-industries.png'),
};

export const getLogoPath = (organisationId: uuid): string =>
  organisationLogo[organisationId] || assetsPathFor('elvaco_logo.png');
