import {makeDataFormatter} from '../domainModelSchema';
import {Dashboard} from './dashboardModels';

export const dashboardDataFormatter = makeDataFormatter<Dashboard>('dashboards');
