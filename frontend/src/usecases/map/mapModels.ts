import {Location} from '../../state/domain-models/domainModels';
import {IdNamed} from '../../types/Types';
import {Marker} from 'react-leaflet';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../state/domain-models/meter/meterModels';

export interface MapMarker extends Location {
  status: IdNamed;
}

// Horrible name, please feel free to improve
export interface ExtendedMarker extends Marker {
  options: {
    mapMarker: Gateway & Meter;
  };
}
