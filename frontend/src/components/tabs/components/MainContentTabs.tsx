import * as React from 'react';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {SelectedTab, TabName} from '../../../state/ui/tabs/tabsModels';
import {
  CallbackWith,
  ClassNamed,
  ClearError,
  EncodedUriParameters,
  ErrorResponse,
  Fetch,
  Fetching,
  WithChildren
} from '../../../types/Types';
import {CollectionStatContentContainer} from '../../../usecases/collection/containers/CollectionStatContentContainer';
import {ResponsiveMapMarkerClusters} from '../../../usecases/map/components/Map';
import {useFetchMapMarkers} from '../../../usecases/map/helper/fetchMapMarkersHook';
import {MapComponentProps, MapMarker, MapProps, OnCenterMapEvent} from '../../../usecases/map/mapModels';
import {SelectionReportContentContainer} from '../../../usecases/selectionReport/containers/SelectionReportContentContainer';
import {EmptyContentProps} from '../../error-message/EmptyContent';
import {withEmptyContent, WithEmptyContentProps} from '../../hoc/withEmptyContent';
import {RetryLoader} from '../../loading/Loader';
import {RetryProps} from '../../retry/Retry';
import {Tab} from './Tab';
import {TabContent} from './TabContent';
import {TabHeaders} from './TabHeaders';
import {Tabs} from './Tabs';
import {TabTopBar} from './TabTopBar';

export interface StateToProps extends MapComponentProps, SelectedTab, EmptyContentProps {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  mapMarkers: DomainModel<MapMarker>;
}

export interface DispatchToProps extends OnCenterMapEvent {
  changeTab: CallbackWith<TabName>;
  clearError: ClearError;
  fetchMapMarkers: Fetch;
}

export type MainContentTabsProps = StateToProps & DispatchToProps & ClassNamed & WithChildren;

const MapClustersWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(ResponsiveMapMarkerClusters);

export const MainContentTabs = ({
  bounds,
  center,
  className,
  children,
  changeTab,
  clearError,
  id,
  isFetching,
  error,
  fetchMapMarkers,
  key,
  lowConfidenceText,
  mapMarkers,
  noContentText,
  onCenterMap,
  parameters,
  selectedTab,
  zoom,
}: MainContentTabsProps) => {
  useFetchMapMarkers({fetchMapMarkers, parameters, selectedTab});

  const wrapperProps: MapProps & WithEmptyContentProps = {
    bounds,
    center,
    hasContent: mapMarkers.result.length > 0,
    id,
    key,
    lowConfidenceText,
    mapMarkers: mapMarkers.entities,
    noContentText,
    onCenterMap,
    zoom,
  };

  const retryLoaderProps: RetryProps & Fetching = {
    clearError,
    error,
    isFetching: isFetching || selectedTab !== TabName.map,
  };

  return (
    <Tabs className={className}>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TabName.list} title={translate('list')}/>
          <Tab tab={TabName.map} title={translate('map')}/>
          <Tab tab={TabName.collection} title={translate('collection')}/>
          <Tab tab={TabName.selectionReport} title={translate('measurements')}/>
        </TabHeaders>
      </TabTopBar>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        {children}
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <RetryLoader {...retryLoaderProps}>
          <MapClustersWrapper {...wrapperProps}/>
        </RetryLoader>
      </TabContent>
      <TabContent tab={TabName.collection} selectedTab={selectedTab}>
        {selectedTab === TabName.collection && <CollectionStatContentContainer/>}
      </TabContent>
      <TabContent tab={TabName.selectionReport} selectedTab={selectedTab}>
        {selectedTab === TabName.selectionReport && <SelectionReportContentContainer/>}
      </TabContent>
    </Tabs>
  );
};
