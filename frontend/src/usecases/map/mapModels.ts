import {Icon, LatLngTuple, MarkerOptions} from 'leaflet';
import {Maybe} from '../../helpers/Maybe';
import {GeoPosition} from '../../state/domain-models/location/locationModels';
import {SelectedTab} from '../../state/ui/tabs/tabsModels';
import {
  CallbackWith,
  Dictionary,
  EncodedUriParameters,
  Fetch,
  Id,
  Identifiable,
  Status,
  uuid,
  WithChildren
} from '../../types/Types';

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

export interface MapMarkerClusters {
  readonly markers: Marker[];
  readonly faults: number;
}

export interface MapMarkersProps {
  mapMarkerClusters: MapMarkerClusters;
}

export interface MapZoomSettings {
  readonly center: GeoPosition;
  readonly zoom: number;
}

export type MapZoomSettingsPayload = MapZoomSettings & Id;

export interface OnCenterMapEvent {
  onCenterMap: CallbackWith<MapZoomSettingsPayload>;
}

export interface MapComponentProps extends WithChildren, Partial<MapZoomSettings>, Id {
  bounds?: Bounds;
  height?: number;
  lowConfidenceText?: string;
  paddingBottom?: number;
  width?: number;
}

export type MapProps = MapComponentProps & MapMarkersProps & OnCenterMapEvent;
