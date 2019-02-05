import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {MeterDefinition} from './meterDefinitionModels';

const meterDefinitionSchema = [new schema.Entity('meterDefinitions')];

export const meterDefinitionsDataFormatter: DataFormatter<Normalized<MeterDefinition>> =
  (response) => normalize(response, meterDefinitionSchema);
