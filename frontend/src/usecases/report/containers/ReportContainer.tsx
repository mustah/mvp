import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {shallowEqual} from 'recompose';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {ResolutionProps, ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {withContent} from '../../../components/hoc/withContent';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {Row, RowRight} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MainTitle} from '../../../components/texts/Titles';
import {PageComponent} from '../../../containers/PageComponent';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {
  SelectedTreeEntities,
  SelectionTreeEntities,
  SelectionTreeMeter
} from '../../../state/selection-tree/selectionTreeModels';
import {getMedia, getMeterIdsWithLimit} from '../../../state/selection-tree/selectionTreeSelectors';
import {mapApiResponseToGraphData} from '../../../state/ui/graph/measurement/helpers/apiResponseToGraphContents';
import {fetchMeasurements, MeasurementParameters} from '../../../state/ui/graph/measurement/measurementActions';
import {
  initialState,
  Measurements,
  MeasurementState,
  Medium,
  Quantity,
} from '../../../state/ui/graph/measurement/measurementModels';
import {selectQuantities, toggleReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {changeTabReport} from '../../../state/ui/tabs/tabsActions';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {OnSelectResolution, SelectedParameters} from '../../../state/user-selection/userSelectionModels';
import {CallbackWith, CallbackWithIds, HasContent, uuid} from '../../../types/Types';
import {logout} from '../../auth/authActions';
import {OnLogout} from '../../auth/authModels';
import {ReportIndicatorProps} from '../components/indicators/ReportIndicatorWidget';
import {ReportIndicatorWidgets, SelectedIndicatorWidgetProps} from '../components/indicators/ReportIndicatorWidgets';
import {MeasurementList} from '../components/MeasurementList';
import {QuantityDropdown} from '../components/QuantityDropdown';
import {selectResolution, showMetersInGraph} from '../reportActions';
import {GraphContents, reportIndicators, ReportState} from '../reportModels';
import {GraphContainer} from './GraphContainer';
import {LegendContainer} from './LegendContainer';
import './ReportContainer.scss';

type SelectedIds = ReportState;

interface StateToProps extends SelectedIds {
  enabledIndicatorTypes: Set<Medium>;
  isFetchingSelectionTree: boolean;
  resolution: TemporalResolution;
  selectedIndicators: Medium[];
  selectedQuantities: Quantity[];
  selectionTreeEntities: SelectionTreeEntities;
  selectedTab: TabName;
  selectionParameters: SelectedParameters;
  userSelectionId: uuid;
}

interface DispatchToProps {
  logout: OnLogout;
  changeTab: CallbackWith<TabName>;
  selectQuantities: (quantities: Quantity[]) => void;
  showMetersInGraph: CallbackWithIds;
  toggleReportIndicatorWidget: OnSelectIndicator;
  selectResolution: OnSelectResolution;
}

type Props = StateToProps & SelectedIndicatorWidgetProps & DispatchToProps & InjectedAuthRouterProps;

const Measurements = withEmptyContent<Measurements & WithEmptyContentProps>(MeasurementList);

const ResolutionDropdown = withContent<ResolutionProps & HasContent>(ResolutionSelection);

const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 16, paddingTop: 0};

const hasMeters = (meters: ObjectsById<SelectionTreeMeter>): boolean =>
  meters && Object.keys(meters).length > 0;

class ReportComponent extends React.Component<Props, MeasurementState> {

  private readonly indicators: ReportIndicatorProps[];

  constructor(props) {
    super(props);
    this.state = {...initialState};
    this.indicators = reportIndicators();
  }

  async componentDidMount() {
    this.setState({isFetching: true});
    await fetchMeasurements(this.makeRequestParameters(this.props));
  }

  async componentWillReceiveProps(nextProps: Props) {
    const requestParameters = this.makeRequestParameters(nextProps);
    if (!shallowEqual(requestParameters, this.makeRequestParameters(this.props))) {
      this.showMetersInGraph(nextProps);
      this.setState({isFetching: true});
      await fetchMeasurements(requestParameters);
    }
  }

  render() {
    const {
      enabledIndicatorTypes,
      hiddenLines,
      resolution,
      selectedTab,
      selectedIndicatorTypes,
      selectedIndicators,
      selectedQuantities,
      selectQuantities,
      selectResolution,
      toggleReportIndicatorWidget,
    } = this.props;
    const {isFetching, error, measurementResponse} = this.state;

    const graphContents: GraphContents = mapApiResponseToGraphData(measurementResponse);

    const canShowResolutionDropdown = selectedIndicators.length > 0;

    return (
      <PageComponent>
        <Row className="space-between">
          <MainTitle>{translate('report')}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        <Row className="ReportContent-Container">
          <ReportIndicatorWidgets
            indicators={this.indicators}
            selectedIndicatorTypes={selectedIndicatorTypes}
            onClick={toggleReportIndicatorWidget}
            enabledIndicatorTypes={enabledIndicatorTypes}
          />
        </Row>

        <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
          <Paper style={contentStyle}>
            <Tabs className="ReportTabs">
              <TabTopBar>
                <TabHeaders selectedTab={selectedTab} onChangeTab={this.onChangeTab}>
                  <Tab tab={TabName.graph} title={translate('graph')}/>
                  <Tab tab={TabName.list} title={translate('table')}/>
                </TabHeaders>
                <RowRight className="ReportTabs-DropdownMenus">
                  <ResolutionDropdown
                    resolution={resolution}
                    selectResolution={selectResolution}
                    hasContent={canShowResolutionDropdown}
                  />
                  <QuantityDropdown
                    selectedIndicators={selectedIndicators}
                    selectedQuantities={selectedQuantities}
                    onSelectQuantities={selectQuantities}
                  />
                </RowRight>
              </TabTopBar>
              <TabContent tab={TabName.graph} selectedTab={selectedTab}>
                <GraphContainer graphContents={graphContents} outerHiddenKeys={hiddenLines}/>
              </TabContent>
              <TabContent tab={TabName.list} selectedTab={selectedTab}>
                <Measurements
                  hasContent={measurementResponse.measurements.length > 0}
                  measurements={measurementResponse.measurements}
                  noContentText={firstUpperTranslated('select meters')}
                />
              </TabContent>
            </Tabs>
            <LegendContainer/>
          </Paper>
        </Loader>
      </PageComponent>
    );
  }

  updateState = (state: MeasurementState): void => this.setState({...state});

  onChangeTab = (selectedTab: TabName): void => {
    this.setState({selectedTab});
    this.props.changeTab(selectedTab);
  }

  clearError = async () => {
    this.setState({error: Maybe.nothing(), isFetching: true});
    await fetchMeasurements(this.makeRequestParameters(this.props));
  }

  makeRequestParameters = ({
    resolution,
    selectionTreeEntities,
    selectedIndicators,
    selectedListItems,
    selectedQuantities,
    logout,
    selectionParameters,
  }: Props): MeasurementParameters =>
    ({
      quantities: selectedQuantities,
      logout,
      resolution,
      selectedListItems,
      selectedIndicators,
      selectionTreeEntities,
      updateState: this.updateState,
      selectionParameters,
    })

  private showMetersInGraph(nextProps: Props) {
    const {isFetchingSelectionTree, showMetersInGraph, selectionTreeEntities, userSelectionId} = this.props;
    if (userSelectionId !== nextProps.userSelectionId) {
      showMetersInGraph([]);
    } else if (nextProps.isFetchingSelectionTree !== isFetchingSelectionTree
               && !shallowEqual(nextProps.selectionTreeEntities.meters, selectionTreeEntities.meters)
               && hasMeters(nextProps.selectionTreeEntities.meters)) {
      if (nextProps.selectedListItems.length > 0) {
        showMetersInGraph(nextProps.selectedListItems);
      } else {
        showMetersInGraph(getMeterIdsWithLimit(nextProps.selectionTreeEntities.meters));
      }
    }
  }
}

const mapStateToProps =
  ({
    report: {hiddenLines, resolution, selectedListItems},
    selectionTree: {entities, isFetching},
    userSelection: {userSelection: {selectionParameters, id: userSelectionId}},
    ui: {
      indicator: {
        selectedIndicators: {report},
        selectedQuantities,
      },
      tabs,
    },
  }: RootState): StateToProps & SelectedIndicatorWidgetProps => {
    const selectedTreeState: SelectedTreeEntities = {selectedListItems, entities};
    return ({
      enabledIndicatorTypes: getMedia(selectedTreeState),
      isFetchingSelectionTree: isFetching,
      hiddenLines,
      resolution,
      selectedListItems,
      selectedQuantities,
      selectedIndicators: report,
      selectedIndicatorTypes: report,
      selectionTreeEntities: entities,
      selectedTab: getSelectedTab(tabs.report),
      selectionParameters,
      userSelectionId,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabReport,
  logout,
  selectQuantities,
  selectResolution,
  showMetersInGraph,
  toggleReportIndicatorWidget,
}, dispatch);

export const ReportContainer =
  connect<SelectedIndicatorWidgetProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportComponent);
