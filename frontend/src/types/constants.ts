import {IdNamed, Period} from './Types';

export const DASHBOARD = 'dashboard';
export const COLLECTION = 'collection';
export const VALIDATION = 'validation';
export const SELECTION = 'selection';

export const periods: IdNamed[] = [
  {id: Period.now, name: 'Now'},
  {id: Period.week, name: 'Week'},
  {id: Period.month, name: 'Month'},
  {id: Period.quarter, name: 'Quarter'},
];
