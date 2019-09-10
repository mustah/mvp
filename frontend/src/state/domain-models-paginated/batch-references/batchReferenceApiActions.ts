import {routerActions} from 'connected-react-router';
import {normalize, schema, Schema} from 'normalizr';
import {routes} from '../../../app/routes';
import {EndPoints} from '../../../services/endPoints';
import {restClient} from '../../../services/restClient';
import {firstUpperTranslated} from '../../../services/translationService';
import {CallbackWith, Dispatch, ErrorResponse, IdNamed, Sectors, toIdNamed} from '../../../types/Types';
import {DataFormatter, postRequest} from '../../domain-models/domainModelsActions';
import {PagedResponse} from '../../domain-models/selections/selectionsModels';
import {showFailMessage, showSuccessMessage} from '../../ui/message/messageActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded} from '../paginatedDomainModelsActions';
import {BatchReference, BatchRequestState, DeviceResponseDto} from './batchReferenceModels';

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

const deviceEuiToIdNamed = ({deviceEui}: DeviceResponseDto): IdNamed => toIdNamed(deviceEui);

const fetchItems = async <T, R>(
  url: string,
  contentMapper: (value: T) => R
): Promise<PagedResponse> => {
  const {data} = await restClient.get(url);
  return {items: data.map(contentMapper), totalElements: data.length};
};

export const batchDevicesUrlWith = (batchId: string): string => `${EndPoints.batches}/${batchId}${EndPoints.devices}`;

export const fetchBatchDevices = (batchId: string) =>
  async (_: number, __?: string): Promise<PagedResponse> =>
    fetchItems<DeviceResponseDto, IdNamed>(batchDevicesUrlWith(batchId), deviceEuiToIdNamed);
