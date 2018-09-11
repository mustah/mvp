import {Identifiable} from '../../../types/Types';
import {GatewayMandatory} from '../../domain-models-paginated/gateway/gatewayModels';
import {Meter, MeterStatusChangelog} from '../../domain-models-paginated/meter/meterModels';
import {Measurement} from '../../ui/graph/measurement/measurementModels';
import {LocationHolder} from '../location/locationModels';

export interface MeterDetails extends Meter, Identifiable, LocationHolder {
  measurements: Measurement[];
  statusChangelog: MeterStatusChangelog[];
  date?: string;
  gateway: GatewayMandatory;
}
