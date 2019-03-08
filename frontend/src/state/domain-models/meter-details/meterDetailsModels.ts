import {Identifiable} from '../../../types/Types';
import {GatewayMandatory} from '../../domain-models-paginated/gateway/gatewayModels';
import {EventLog, Meter} from '../../domain-models-paginated/meter/meterModels';
import {LocationHolder} from '../location/locationModels';

export interface MeterDetails extends Meter, Identifiable, LocationHolder {
  eventLog: EventLog[];
  date?: string;
  gateway?: GatewayMandatory;
}
