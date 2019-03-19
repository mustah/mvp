import {GatewayMandatory} from '../../domain-models-paginated/gateway/gatewayModels';
import {EventLog, Meter} from '../../domain-models-paginated/meter/meterModels';

export interface MeterDetails extends Meter {
  eventLog: EventLog[];
  date?: string;
  gateway?: GatewayMandatory;
}
