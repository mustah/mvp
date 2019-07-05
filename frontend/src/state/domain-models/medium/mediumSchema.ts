import {makeDataFormatter} from '../domainModelSchema';
import {Medium} from '../meter-definitions/meterDefinitionModels';

export const mediumsDataFormatter = makeDataFormatter<Medium>('mediums');
