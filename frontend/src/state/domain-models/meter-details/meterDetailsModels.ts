import {Identifiable, IdNamed, uuid} from '../../../types/Types';
import {LocationHolder} from '../location/locationModels';
import {Flag} from '../flag/flagModels';
import {Measurement} from '../../ui/graph/measurement/measurementModels';
import {MeterStatusChangelog} from '../../domain-models-paginated/meter/meterModels';
import {GatewayMandatory} from '../../domain-models-paginated/gateway/gatewayModels';

export interface MeterDetails extends Identifiable, LocationHolder {
  address?: string;
  created: string;
  collectionPercentage?: number;
  readIntervalMinutes?: number;
  facility: uuid;
  flags: Flag[];
  flagged: boolean;
  medium: string;
  manufacturer: string;
  measurements: Measurement[];
  statusChanged?: string;
  statusChangelog: MeterStatusChangelog[];
  date?: string;
  status: IdNamed;
  gateway: GatewayMandatory;
  organisationId: uuid;
}
