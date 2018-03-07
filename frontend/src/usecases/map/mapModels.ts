import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {Meter} from '../../state/domain-models-paginated/meter/meterModels';
import {Gateway} from '../../state/domain-models/gateway/gatewayModels';
import {LocationHolder} from '../../state/domain-models/location/locationModels';
import {IdNamed} from '../../types/Types';

export interface MapMarker extends LocationHolder {
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
