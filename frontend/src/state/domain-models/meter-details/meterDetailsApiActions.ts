import {normalize, schema, Schema} from 'normalizr';
import {toGatewayIdsApiParameters, toMeterIdsApiParameters} from '../../../helpers/urlFactory';
import {EndPoints} from '../../../services/endPoints';
import {meterProcessStrategy} from '../../domain-models-paginated/meter/meterSchema';
import {Normalized} from '../domainModels';
import {DataFormatter, fetchEntitiesIfNeeded} from '../domainModelsActions';
import {MeterDetails} from './meterDetailsModels';

const meterSchema: Schema = [new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy})];

const meterDetailsDataFormatter: DataFormatter<Normalized<MeterDetails>> =
  (response) => normalize(response, meterSchema);

export const fetchMeterDetails = fetchEntitiesIfNeeded(
  EndPoints.meterDetails,
  'meters',
  meterDetailsDataFormatter,
  toMeterIdsApiParameters,
);

export const fetchGatewayMeterDetails = fetchEntitiesIfNeeded(
  EndPoints.meterDetails,
  'meters',
  meterDetailsDataFormatter,
  toGatewayIdsApiParameters,
);
