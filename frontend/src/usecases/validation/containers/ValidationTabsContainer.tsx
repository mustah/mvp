import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {HasContent} from '../../../components/content/HasContent';
import {Dialog} from '../../../components/dialog/Dialog';
import {Loader} from '../../../components/loading/Loader';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {MeterDetailsContainer} from '../../../containers/dialogs/MeterDetailsContainer';
import {MeterListContainer} from '../../../containers/meters/MeterListContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {getDomainModel, getError} from '../../../state/domain-models/domainModelsSelectors';
import {getMeterParameters} from '../../../state/search/selection/selectionSelectors';
import {changeTabValidation} from '../../../state/ui/tabs/tabsActions';
import {TabName, TabsContainerDispatchToProps, TabsContainerStateToProps} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {ClearError, EncodedUriParameters, ErrorResponse, Fetch, OnClick, uuid} from '../../../types/Types';
import {ClusterContainer} from '../../map/containers/ClusterContainer';
import {Map} from '../../map/containers/Map';
import {meterLowConfidenceTextInfo} from '../../map/helper/mapHelper';
import {closeClusterDialog} from '../../map/mapActions';
import {MapMarker} from '../../map/mapModels';
import {getSelectedMapMarker} from '../../map/mapSelectors';
import {clearErrorMeterMapMarkers, fetchMeterMapMarkers} from '../../map/meterMapMarkerApiActions';

interface StateToProps extends TabsContainerStateToProps {
  isFetching: boolean;
  meterMapMarkers: DomainModel<MapMarker>;
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

class ValidationTabs extends React.Component<Props> {

  componentDidMount() {
    const {parameters, fetchMeterMapMarkers} = this.props;
    fetchMeterMapMarkers(parameters);
  }

  componentWillReceiveProps({parameters, fetchMeterMapMarkers}: Props) {
    fetchMeterMapMarkers(parameters);
  }

  render() {
    const {
      selectedTab,
      changeTab,
      clearError,
      error,
      isFetching,
      meterMapMarkers,
      selectedMarker,
      closeClusterDialog,
    } = this.props;

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
              <HasContent
                hasContent={meterMapMarkers.result.length > 0}
                fallbackContent={<MissingDataTitle title={firstUpperTranslated('no meters')}/>}
              >
                <Map lowConfidenceText={meterLowConfidenceTextInfo(meterMapMarkers)}>
                  <ClusterContainer markers={meterMapMarkers.entities}/>
                </Map>
              </HasContent>
              {dialog}
            </div>
          </Loader>
        </TabContent>
      </Tabs>
    );
  }
}

const mapStateToProps =
  ({ui, userSelection, map, domainModels: {meterMapMarkers}}: RootState): StateToProps => ({
    selectedTab: getSelectedTab(ui.tabs.validation),
    meterMapMarkers: getDomainModel(meterMapMarkers),
    selectedMarker: getSelectedMapMarker(map),
    parameters: getMeterParameters(userSelection),
    error: getError(meterMapMarkers),
    isFetching: meterMapMarkers.isFetching,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabValidation,
  closeClusterDialog,
  clearError: clearErrorMeterMapMarkers,
  fetchMeterMapMarkers,
}, dispatch);

export const ValidationTabsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationTabs);
