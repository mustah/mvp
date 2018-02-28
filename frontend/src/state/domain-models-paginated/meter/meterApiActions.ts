import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse} from '../../../types/Types';
import {EndPoints} from '../../../services/endPoints';
import {showFailMessage} from '../../ui/message/messageActions';
import {paginationUpdateMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded} from '../paginatedDomainModelsActions';
import {Meter} from './meterModels';
import {meterSchema} from './meterSchema';

export const fetchMeters = fetchIfNeeded<Meter>(EndPoints.meters, meterSchema, 'meters', {
  afterSuccess: (
    {result}: NormalizedPaginated<Meter>,
    dispatch,
  ) => dispatch(paginationUpdateMetaData({entityType: 'meters', ...result})),
  afterFailure: (
    {message}: ErrorResponse,
    dispatch,
  ) => dispatch(showFailMessage(firstUpperTranslated('error: {{message}}', {message}))),
});
