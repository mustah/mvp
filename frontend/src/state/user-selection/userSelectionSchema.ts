import {makeDataFormatter} from '../domain-models/domainModelSchema';
import {UserSelection} from './userSelectionModels';

export const userSelectionsDataFormatter = makeDataFormatter<UserSelection>('userSelections');
