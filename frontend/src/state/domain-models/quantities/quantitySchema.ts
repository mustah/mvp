import {normalize, schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {Quantity} from '../meter-definitions/meterDefinitionModels';

const quantitySchema = [new schema.Entity('quantities')];

export const quantitiesDataFormatter: DataFormatter<Normalized<Quantity>> =
  (response) => normalize(response, quantitySchema);
