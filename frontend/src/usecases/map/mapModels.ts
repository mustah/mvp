import {Location} from '../../state/domain-models/domainModels';

export interface MapMarker extends Location {
  status: string;
}
