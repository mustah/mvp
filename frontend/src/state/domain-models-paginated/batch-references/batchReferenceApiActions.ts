import {normalize, schema, Schema} from 'normalizr';
import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded} from '../paginatedDomainModelsActions';
import {BatchReference} from './batchReferenceModels';

export const processStrategy = ({batchId: id, ...rest}: any): schema.StrategyFunction<BatchReference> =>
  ({...rest, id});

const content = [new schema.Entity('batchReferences', {}, {idAttribute: 'batchId', processStrategy})];
const batchReferenceSchema: Schema = {content};

const dataFormatter: DataFormatter<NormalizedPaginated<BatchReference>> =
  (response) => normalize(response, batchReferenceSchema) as NormalizedPaginated<BatchReference>;

export const fetchBatchReferences = fetchIfNeeded(
  Sectors.batchReferences,
  EndPoints.batches,
  dataFormatter,
  'batchReferences',
  {
    afterSuccess: (
      {result}: NormalizedPaginated<BatchReference>,
      dispatch,
    ) => dispatch(updatePageMetaData({entityType: 'batchReferences', ...result})),
  },
);
