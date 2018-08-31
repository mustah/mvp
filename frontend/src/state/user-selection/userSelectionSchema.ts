import {normalize, schema} from 'normalizr';
import {Normalized} from '../domain-models/domainModels';
import {DataFormatter} from '../domain-models/domainModelsActions';
import {UserSelection} from './userSelectionModels';

const userSelectionSchema = [new schema.Entity('userSelections')];

export const userSelectionsDataFormatter: DataFormatter<Normalized<UserSelection>> =
  (response) => normalize(response, userSelectionSchema);
