import {normalize, schema} from 'normalizr';
import {DataFormatter} from '../domain-models/domainModelsActions';

const userSelectionSchema = [new schema.Entity('userSelections')];

export const userSelectionsDataFormatter: DataFormatter =
  (response) => normalize(response, userSelectionSchema);
