import {makeDataFormatter} from '../domainModelSchema';
import {Quantity} from '../meter-definitions/meterDefinitionModels';

export const quantitiesDataFormatter = makeDataFormatter<Quantity>('quantities');
