import {normalize, schema, Schema} from 'normalizr';
import {toGatewayIdsApiParameters} from '../../../helpers/urlFactory';
import {EndPoints} from '../../../services/endPoints';
import {meterProcessStrategy} from '../../domain-models-paginated/meter/meterSchema';
import {Normalized} from '../domainModels';
import {DataFormatter, fetchEntitiesIfNeeded, fetchEntityIfNeeded} from '../domainModelsActions';
import {MeterDetails} from './meterDetailsModels';

const meterSchema: Schema = [new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy})];

const meterDetailsDataFormatter: DataFormatter<Normalized<MeterDetails>> =
  (response) => normalize(response, meterSchema);

export const fetchMeter = fetchEntityIfNeeded<MeterDetails>(
  EndPoints.meters,
  'meters'
);

export const fetchGatewayMeterDetails = fetchEntitiesIfNeeded(
  EndPoints.meterDetails,
  'meters',
  meterDetailsDataFormatter,
  toGatewayIdsApiParameters,
);
