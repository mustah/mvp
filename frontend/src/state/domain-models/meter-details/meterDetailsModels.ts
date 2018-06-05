import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {GatewayMandatory} from '../../domain-models-paginated/gateway/gatewayModels';
import {MeterStatusChangelog} from '../../domain-models-paginated/meter/meterModels';
import {Measurement} from '../../ui/graph/measurement/measurementModels';
import {LocationHolder} from '../location/locationModels';

export interface MeterDetails extends Identifiable, LocationHolder {
  address?: string;
  collectionPercentage?: number;
  readIntervalMinutes?: number;
  facility: uuid;
  medium: string;
  manufacturer: string;
  measurements: Measurement[];
  statusChanged?: string;
  statusChangelog: MeterStatusChangelog[];
  date?: string;
  status: IdNamed;
  gateway: GatewayMandatory;
  gatewaySerial: string;
  organisationId: uuid;
}
