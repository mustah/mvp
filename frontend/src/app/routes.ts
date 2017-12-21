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
};

const companyLogo = {
  'wayne-industries': 'wayne-industries.png',
};

export const getLogoPath = (company: uuid): string => companyLogo[company] || 'elvaco_logo.png';
