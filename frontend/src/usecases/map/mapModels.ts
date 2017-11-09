import {Location} from '../../state/domain-models/domainModels';
import {IdNamed} from '../../types/Types';

export interface MapMarker extends Location {
  status: IdNamed;
}
