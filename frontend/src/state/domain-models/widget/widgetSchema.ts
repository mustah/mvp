import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {Widget} from './widgetModels';

const widgetSchema = [new schema.Entity('widgets')];

export const widgetDataFormatter: DataFormatter<Normalized<Widget>> =
  (response) => normalize(response, widgetSchema);
