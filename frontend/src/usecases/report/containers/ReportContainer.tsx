import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {shallowEqual} from 'recompose';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {withEmptyContent, WithEmptyContentProps} from '../../../components/hoc/withEmptyContent';
import {OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {SelectedTreeEntities, SelectionTreeEntities} from '../../../state/selection-tree/selectionTreeModels';
import {getMedia} from '../../../state/selection-tree/selectionTreeSelectors';
import {mapApiResponseToGraphData} from '../../../state/ui/graph/measurement/helpers/apiResponseToGraphContents';
import {fetchMeasurements, MeasurementOptions} from '../../../state/ui/graph/measurement/measurementActions';
import {
  initialState,
  Measurements,
  MeasurementState,
  Medium,
  Quantity,
} from '../../../state/ui/graph/measurement/measurementModels';
import {toggleReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {changeTabReport} from '../../../state/ui/tabs/tabsActions';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {SelectedParameters} from '../../../state/user-selection/userSelectionModels';
import {CallbackWith, CallbackWithIds} from '../../../types/Types';
import {logout} from '../../auth/authActions';
import {OnLogout} from '../../auth/authModels';
import {ReportIndicatorProps} from '../components/indicators/ReportIndicatorWidget';
import {ReportIndicatorWidgets, SelectedIndicatorWidgetProps} from '../components/indicators/ReportIndicatorWidgets';
import {MeasurementList} from '../components/MeasurementList';
import {limit, showMetersInGraph} from '../reportActions';
import {GraphContents, hardcodedIndicators, ReportState} from '../reportModels';
import {GraphContainer} from './GraphContainer';
import {LegendContainer} from './LegendContainer';

type SelectedIds = ReportState;

interface StateToProps extends SelectedIds {
  enabledIndicatorTypes: Set<Medium>;
  selectedIndicators: Medium[];
  selectedQuantities: Quantity[];
  selectionTreeEntities: SelectionTreeEntities;
  selectedTab: TabName;
  selectionParameters: SelectedParameters;
}

interface DispatchToProps {
  logout: OnLogout;
  changeTab: CallbackWith<TabName>;
  showMetersInGraph: CallbackWithIds;
  toggleReportIndicatorWidget: OnSelectIndicator;
}

type Props = StateToProps & SelectedIndicatorWidgetProps & DispatchToProps & InjectedAuthRouterProps;

const Measurements = withEmptyContent<Measurements & WithEmptyContentProps>(MeasurementList);

const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 16};

class ReportComponent extends React.Component<Props, MeasurementState> {

  private readonly indicators: ReportIndicatorProps[];

  constructor(props) {
    super(props);
    this.state = {...initialState};
    this.indicators = hardcodedIndicators();
  }

  async componentDidMount() {
    this.setState({isFetching: true});
    await fetchMeasurements(this.makeRequestParameters(this.props));
  }

  async componentWillReceiveProps(nextProps: Props) {
    const requestParameters = this.makeRequestParameters(nextProps);
    const {showMetersInGraph, selectionTreeEntities: {meters}} = nextProps;
    if (!shallowEqual(requestParameters, this.makeRequestParameters(this.props))) {
      if (nextProps.selectionTreeEntities !== this.props.selectionTreeEntities
          && meters
          && Object.keys(meters).length) {
        showMetersInGraph(Object.keys(meters).splice(0, limit));
      }
      this.setState({isFetching: true});
      await fetchMeasurements(requestParameters);
    }
  }

  render() {
    const {
      enabledIndicatorTypes,
      hiddenLines,
      selectedTab,
      selectedIndicatorTypes,
      toggleReportIndicatorWidget,
    } = this.props;
    const {isFetching, error, measurementResponse} = this.state;

    const graphContents: GraphContents = mapApiResponseToGraphData(measurementResponse);
    return (
      <MvpPageContainer>
        <Row className="space-between">
          <MainTitle>{translate('report')}</MainTitle>
          <Row>
            <SummaryContainer/>
            <PeriodContainer/>
          </Row>
        </Row>

        <ReportIndicatorWidgets
          indicators={this.indicators}
          selectedIndicatorTypes={selectedIndicatorTypes}
          onClick={toggleReportIndicatorWidget}
          enabledIndicatorTypes={enabledIndicatorTypes}
        />

        <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
          <Paper style={contentStyle}>
            <Tabs>
              <TabTopBar>
                <TabHeaders selectedTab={selectedTab} onChangeTab={this.onChangeTab}>
                  <Tab tab={TabName.graph} title={translate('graph')}/>
                  <Tab tab={TabName.list} title={translate('list')}/>
                </TabHeaders>
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
      </MvpPageContainer>
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
    selectionTreeEntities,
    selectedIndicators,
    selectedListItems,
    selectedQuantities,
    logout,
    selectionParameters,
  }: Props): MeasurementOptions =>
    ({
      quantities: selectedQuantities,
      logout,
      selectedListItems,
      selectedIndicators,
      selectionTreeEntities,
      updateState: this.updateState,
      selectionParameters,
    })
}

const mapStateToProps =
  ({
    report: {hiddenLines, selectedListItems},
    selectionTree: {entities},
    userSelection: {userSelection: {selectionParameters}},
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
      hiddenLines,
      selectedListItems,
      selectedQuantities,
      selectedIndicators: report,
      selectedIndicatorTypes: report,
      selectionTreeEntities: entities,
      selectedTab: getSelectedTab(tabs.report),
      selectionParameters,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabReport,
  showMetersInGraph,
  logout,
  toggleReportIndicatorWidget,
}, dispatch);

export const ReportContainer =
  connect<SelectedIndicatorWidgetProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportComponent);
