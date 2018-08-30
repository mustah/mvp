import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {Dialog} from '../../../components/dialog/Dialog';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {Loader} from '../../../components/loading/Loader';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import {now} from '../../../helpers/dateHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {ClearError, EncodedUriParameters, ErrorResponse, Fetch, OnClick, uuid} from '../../../types/Types';
import {Map} from '../../map/components/Map';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {closeClusterDialog} from '../../map/mapActions';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../../map/mapMarkerActions';
import {Bounds, MapMarker} from '../../map/mapModels';
import {getBounds, getMeterLowConfidenceTextInfo, getSelectedMapMarker} from '../../map/mapSelectors';

interface MapProps {
  bounds?: Bounds;
  lowConfidenceText?: string;
  meterMapMarkers: DomainModel<MapMarker>;
}

interface StateToProps extends MapProps, TabsContainerStateToProps {
  isFetching: boolean;
  selectedMarker: Maybe<uuid>;
  parameters: EncodedUriParameters;
  error: Maybe<ErrorResponse>;
}

interface DispatchToProps extends TabsContainerDispatchToProps {
  closeClusterDialog: OnClick;
  fetchMeterMapMarkers: Fetch;
  clearError: ClearError;
}

type Props = StateToProps & DispatchToProps;

const MapContent = ({bounds, lowConfidenceText, meterMapMarkers}: MapProps) => (
  <Map bounds={bounds} lowConfidenceText={lowConfidenceText}>
    <ClusterContainer markers={meterMapMarkers.entities}/>
  </Map>
);

const MapContentWrapper = withEmptyContent<MapProps & WithEmptyContentProps>(MapContent);

const fetchMarkersWhenMapTabIsSelected =
  ({fetchMeterMapMarkers, selectedTab, parameters}: Props) => {
    if (TabName.map === selectedTab) {
      fetchMeterMapMarkers(parameters);
    }
  };

class ValidationTabs extends React.Component<Props> {

  componentDidMount() {
    fetchMarkersWhenMapTabIsSelected(this.props);
  }

  componentWillReceiveProps(props: Props) {
    fetchMarkersWhenMapTabIsSelected(props);
  }

  render() {
    const {
      bounds,
      selectedTab,
      changeTab,
      clearError,
      error,
      isFetching,
      lowConfidenceText,
      meterMapMarkers,
      selectedMarker,
      closeClusterDialog,
    } = this.props;

    const wrapperProps: MapProps & WithEmptyContentProps = {
      bounds,
      lowConfidenceText,
      meterMapMarkers,
      noContentText: firstUpperTranslated('no meters'),
      hasContent: meterMapMarkers.result.length > 0,
    };

    const dialog = selectedMarker.isJust() && (
      <Dialog isOpen={true} close={closeClusterDialog} autoScrollBodyContent={true}>
        <MeterDetailsContainer meterId={selectedMarker.get()}/>
      </Dialog>
    );

    return (
      <Tabs>
        <TabTopBar>
          <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
            <Tab tab={TabName.list} title={translate('list')}/>
            <Tab tab={TabName.map} title={translate('map')}/>
          </TabHeaders>
        </TabTopBar>
        <TabContent tab={TabName.list} selectedTab={selectedTab}>
          <MeterListContainer componentId={'validationMeterList'}/>
        </TabContent>
        <TabContent tab={TabName.map} selectedTab={selectedTab}>
          <Loader isFetching={isFetching} clearError={clearError} error={error}>
            <div>
              <MapContentWrapper {...wrapperProps} />
              {dialog}
            </div>
          </Loader>
        </TabContent>
      </Tabs>
    );
  }
}

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      ui,
      userSelection: {userSelection},
      map,
      domainModels: {meterMapMarkers},
    }: RootState = rootState;
    return ({
      bounds: getBounds(meterMapMarkers),
      lowConfidenceText: getMeterLowConfidenceTextInfo(rootState),
      selectedTab: getSelectedTab(ui.tabs.validation),
      meterMapMarkers: getDomainModel(meterMapMarkers),
      selectedMarker: getSelectedMapMarker(map),
      parameters: getMeterParameters({userSelection, now: now()}),
      error: getError(meterMapMarkers),
      isFetching: meterMapMarkers.isFetching,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  closeClusterDialog,
  clearError: clearErrorMeterMapMarkers,
  fetchMeterMapMarkers,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);
