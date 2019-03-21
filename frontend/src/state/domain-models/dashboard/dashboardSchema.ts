import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {Dashboard} from './dashboardModels';

const dashboardSchema = [new schema.Entity('dashboards')];

export const dashboardDataFormatter: DataFormatter<Normalized<Dashboard>> =
  (response) => normalize(response, dashboardSchema);
