import * as React from 'react';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {SelectedTab, TabName} from '../../../state/ui/tabs/tabsModels';
import {
  CallbackWith,
  Children,
  ClassNamed,
  ClearError,
  EncodedUriParameters,
  ErrorResponse,
  Fetch,
  OnClick
} from '../../../types/Types';
import {MapClusters} from '../../../usecases/map/components/MapClusters';
import {MapProps, SelectedId} from '../../../usecases/map/mapModels';
import {CollectionStatContentContainer} from '../../../usecases/collection/containers/CollectionStatContentContainer';
import {DetailsDialogProps} from '../../dialog/DetailsDialog';
import {EmptyContentProps} from '../../error-message/EmptyContent';
import {withEmptyContent, WithEmptyContentProps} from '../../hoc/withEmptyContent';
import {RetryLoader} from '../../loading/Loader';
import {Tab} from './Tab';
import {TabContent} from './TabContent';
import {TabHeaders} from './TabHeaders';
import {Tabs} from './Tabs';
import {TabTopBar} from './TabTopBar';

export interface StateToProps extends MapProps, SelectedTab, EmptyContentProps, SelectedId {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
}

export interface DispatchToProps {
  changeTab: CallbackWith<TabName>;
  close: OnClick;
  clearError: ClearError;
  fetchMapMarkers: Fetch;
}

export interface MainContentTabsProps extends StateToProps, DispatchToProps, ClassNamed {
  children?: Children;
  DetailsDialog: React.ComponentType<DetailsDialogProps>;
}

const MapClustersWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapClusters);

export const MainContentTabs = (props: MainContentTabsProps) => {
  const {
    className,
    DetailsDialog,
    bounds,
    children,
    selectedTab,
    changeTab,
    clearError,
    error,
    isFetching,
    lowConfidenceText,
    mapMarkers,
    noContentText,
    selectedId,
    close,
  } = props;
  const wrapperProps: MapProps & WithEmptyContentProps = {
    bounds,
    lowConfidenceText,
    mapMarkers,
    noContentText,
    hasContent: mapMarkers.result.length > 0,
  };

  const dialogProps: DetailsDialogProps = {
    autoScrollBodyContent: true,
    close,
    isOpen: true,
    selectedId,
  };

  return (
    <Tabs className={className}>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TabName.list} title={translate('list')}/>
          <Tab tab={TabName.map} title={translate('map')}/>
          <Tab tab={TabName.collection} title={translate('collection')}/>
        </TabHeaders>
      </TabTopBar>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        {children}
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <RetryLoader isFetching={isFetching} clearError={clearError} error={error}>
          <>
            <MapClustersWrapper {...wrapperProps} />
            <DetailsDialog {...dialogProps}/>
          </>
        </RetryLoader>
      </TabContent>
      <TabContent tab={TabName.collection} selectedTab={selectedTab}>
        {selectedTab === TabName.collection && <CollectionStatContentContainer/>}
      </TabContent>
    </Tabs>
  );
};
