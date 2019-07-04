import {makeDataFormatter} from '../domainModelSchema';
import {MeterDefinition} from './meterDefinitionModels';

export const meterDefinitionsDataFormatter = makeDataFormatter<MeterDefinition>('meterDefinitions');
