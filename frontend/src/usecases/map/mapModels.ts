import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {Maybe} from '../../helpers/Maybe';
import {DomainModel} from '../../state/domain-models/domainModels';
import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {SelectedTab} from '../../state/ui/tabs/tabsModels';
import {Dictionary, EncodedUriParameters, Fetch, Identifiable, Status, uuid} from '../../types/Types';

export type IdentifiablePosition = Identifiable & GeoPosition;

export interface MapMarkerApiResponse {
  markers: Dictionary<IdentifiablePosition[]>;
}

export interface MapMarker extends Identifiable {
  status: Status;
  alarm?: number;
  latitude: number;
  longitude: number;
}

export interface Marker {
  position: LatLngTuple;
  options: MarkerOptions & {
    icon: Icon;
    mapMarkerItem: uuid;
    status?: Status;
  };
}

export type Bounds = LatLngTuple[] | undefined;

export interface MapMarkerProps extends SelectedTab {
  parameters: EncodedUriParameters;
  fetchMapMarkers: Fetch;
}

export interface SelectedId {
  selectedId: Maybe<uuid>;
}

export interface MapProps {
  bounds?: Bounds;
  key?: string;
  lowConfidenceText?: string;
  mapMarkers: DomainModel<MapMarker>;
}
