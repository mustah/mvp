import * as React from 'react';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {SelectedTab, TabName, TabsContainerDispatchToProps} from '../../../state/ui/tabs/tabsModels';
import {Children, ClearError, EncodedUriParameters, ErrorResponse, Fetch, OnClick} from '../../../types/Types';
import {MapClusters} from '../../../usecases/map/components/MapClusters';
import {MapProps, SelectedId} from '../../../usecases/map/mapModels';
import {DetailsDialogProps} from '../../dialog/DetailsDialog';
import {EmptyContentProps} from '../../error-message/EmptyContent';
import {withEmptyContent, WithEmptyContentProps} from '../../hoc/withEmptyContent';
import {Loader} from '../../loading/Loader';
import {Tab} from './Tab';
import {TabContent} from './TabContent';
import {TabHeaders} from './TabHeaders';
import {Tabs} from './Tabs';
import {TabTopBar} from './TabTopBar';

export interface StateToProps extends MapProps, SelectedTab, EmptyContentProps, SelectedId {
  isFetching: boolean;
  parameters: EncodedUriParameters;
  error: Maybe<ErrorResponse>;
}

export interface DispatchToProps extends TabsContainerDispatchToProps {
  close: OnClick;
  clearError: ClearError;
  fetchMapMarkers: Fetch;
}

export interface MainContentTabsProps extends StateToProps, DispatchToProps {
  children?: Children;
  DetailsDialog: React.ComponentType<DetailsDialogProps>;
}

const MapClustersWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapClusters);

export const MainContentTabs = (props: MainContentTabsProps) => {
  const {
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
    <Tabs>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TabName.list} title={translate('list')}/>
          <Tab tab={TabName.map} title={translate('map')}/>
        </TabHeaders>
      </TabTopBar>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        {children}
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <Loader isFetching={isFetching} clearError={clearError} error={error}>
          <div>
            <MapClustersWrapper {...wrapperProps} />
            <DetailsDialog {...dialogProps}/>
          </div>
        </Loader>
      </TabContent>
    </Tabs>
  );
};
