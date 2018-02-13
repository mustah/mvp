import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {Location} from '../../state/domain-models/domainModels';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {IdNamed} from '../../types/Types';

export interface MapMarker extends Location {
  status: IdNamed;
}

export type MapMarkerItem = Gateway | Meter;

export interface Marker {
  position: LatLngTuple;
  options: MarkerOptions & {
    icon: Icon;
    mapMarkerItem: MapMarkerItem;
  };
}
