import {routerActions} from 'connected-react-router';
import {normalize, schema, Schema} from 'normalizr';
import {routes} from '../../../app/routes';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {CallbackWith, Dispatch, ErrorResponse, Sectors} from '../../../types/Types';
import {DataFormatter, postRequest} from '../../domain-models/domainModelsActions';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded} from '../paginatedDomainModelsActions';
import {BatchReference, BatchRequestState} from './batchReferenceModels';

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

export const createBatchReference: CallbackWith<BatchRequestState> =
  postRequest<BatchRequestState>(EndPoints.batches, {
    afterSuccess: (_: BatchRequestState, dispatch: Dispatch) => {
      dispatch(showSuccessMessage(firstUpperTranslated('successfully created batch reference')));
      dispatch(routerActions.push(`${routes.otcBatchReferences}`));
    },
    afterFailure: ({message}: ErrorResponse, dispatch: Dispatch) => {
      dispatch(showFailMessage(firstUpperTranslated(
        'failed to create batch reference: {{error}}',
        {error: firstUpperTranslated(message.toLowerCase())},
      )));
    },
  });
