import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {Medium} from '../meter-definitions/meterDefinitionModels';

const mediumSchema = [new schema.Entity('mediums')];

export const mediumsDataFormatter: DataFormatter<Normalized<Medium>> =
  (response) => normalize(response, mediumSchema);
