import {makeDataFormatter} from '../domainModelSchema';
import {Organisation} from './organisationModels';

export const organisationsDataFormatter = makeDataFormatter<Organisation>('organisations');
