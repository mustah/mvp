import * as React from 'react';
import {EmptyContentProps} from '../../../components/error-message/EmptyContent';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {RetryLoader} from '../../../components/loading/Loader';
import {RetryProps} from '../../../components/retry/Retry';
import {Maybe} from '../../../helpers/Maybe';
import {SelectedTab} from '../../../state/ui/tabs/tabsModels';
import {ClearError, EncodedUriParameters, ErrorResponse, Fetch, Fetching} from '../../../types/Types';
import {useFetchMapMarkers} from '../helper/fetchMapMarkersHook';
import {MapComponentProps, MapMarkersProps, MapProps, OnCenterMapEvent} from '../mapModels';
import {ResponsiveMapMarkerClusters} from './Map';

export interface StateToProps extends MapComponentProps, SelectedTab, EmptyContentProps, MapMarkersProps {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
}

export interface DispatchToProps extends OnCenterMapEvent {
  clearError: ClearError;
  fetchMapMarkers: Fetch;
}

const MapClustersWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(ResponsiveMapMarkerClusters);

export const MapMarkers = ({
  mapMarkerClusters,
  clearError,
  error,
  isFetching,
  ...mapProps
}: StateToProps & DispatchToProps) => {
  const {fetchMapMarkers, parameters, selectedTab} = mapProps;

  useFetchMapMarkers({fetchMapMarkers, parameters, selectedTab});

  const wrapperProps: MapProps & WithEmptyContentProps = {
    hasContent: mapMarkerClusters.markers.length > 0,
    mapMarkerClusters,
    ...mapProps,
  };

  const retryLoaderProps: RetryProps & Fetching = {clearError, error, isFetching};

  return (
    <RetryLoader {...retryLoaderProps}>
      <MapClustersWrapper {...wrapperProps}/>
    </RetryLoader>
  );
};
