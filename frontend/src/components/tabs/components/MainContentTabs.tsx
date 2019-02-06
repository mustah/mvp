import {default as classNames} from 'classnames';
import * as React from 'react';
import {Maybe} from '../../../helpers/Maybe';
import {translate} from '../../../services/translationService';
import {SelectedTab, TabName} from '../../../state/ui/tabs/tabsModels';
import {OnSelectResolution} from '../../../state/user-selection/userSelectionModels';
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
import {GraphContainer, MeasurementsContainer} from '../../../usecases/report/containers/MeasurementsContainer';
import {TemporalResolution} from '../../dates/dateModels';
import {ResolutionSelection} from '../../dates/ResolutionSelection';
import {DetailsDialogProps} from '../../dialog/DetailsDialog';
import {EmptyContentProps} from '../../error-message/EmptyContent';
import {withEmptyContent, WithEmptyContentProps} from '../../hoc/withEmptyContent';
import {RowRight} from '../../layouts/row/Row';
import {Loader} from '../../loading/Loader';
import {Tab} from './Tab';
import {TabContent} from './TabContent';
import {TabHeaders} from './TabHeaders';
import {Tabs} from './Tabs';
import {TabTopBar} from './TabTopBar';

export interface StateToProps extends MapProps, SelectedTab, EmptyContentProps, SelectedId {
  error: Maybe<ErrorResponse>;
  isFetching: boolean;
  parameters: EncodedUriParameters;
  resolution: TemporalResolution;
}

export interface DispatchToProps {
  changeTab: CallbackWith<TabName>;
  close: OnClick;
  clearError: ClearError;
  fetchMapMarkers: Fetch;
  selectResolution: OnSelectResolution;
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
    resolution,
    selectedId,
    selectResolution,
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

  const show = selectedTab === TabName.graph || selectedTab === TabName.values;
  return (
    <Tabs className={className}>
      <TabTopBar>
        <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
          <Tab tab={TabName.list} title={translate('list')}/>
          <Tab tab={TabName.map} title={translate('map')}/>
          <Tab tab={TabName.graph} title={translate('graph')}/>
          <Tab tab={TabName.values} title={translate('measurements')}/>
        </TabHeaders>
        <RowRight className={classNames('Tabs-DropdownMenus', {show})}>
          <ResolutionSelection
            resolution={resolution}
            selectResolution={selectResolution}
          />
        </RowRight>
      </TabTopBar>
      <TabContent tab={TabName.list} selectedTab={selectedTab}>
        {children}
      </TabContent>
      <TabContent tab={TabName.map} selectedTab={selectedTab}>
        <Loader isFetching={isFetching} clearError={clearError} error={error}>
          <>
            <MapClustersWrapper {...wrapperProps} />
            <DetailsDialog {...dialogProps}/>
          </>
        </Loader>
      </TabContent>
      <TabContent tab={TabName.graph} selectedTab={selectedTab}>
        {selectedTab === TabName.graph && <GraphContainer/>}
      </TabContent>
      <TabContent tab={TabName.values} selectedTab={selectedTab}>
        {selectedTab === TabName.values && <MeasurementsContainer/>}
      </TabContent>
    </Tabs>
  );
};
