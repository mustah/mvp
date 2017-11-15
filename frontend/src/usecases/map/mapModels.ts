import {Location} from '../../state/domain-models/domainModelsModels';
import {IdNamed} from '../../types/Types';

export interface MapMarker extends Location {
  status: IdNamed;
}
