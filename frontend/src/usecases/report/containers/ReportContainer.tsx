import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {Medium, OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {
  ReportIndicatorWidgets,
  SelectedIndicatorWidgetProps,
} from '../../../components/indicators/ReportIndicatorWidgets';
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
import {toggle} from '../../../helpers/collections';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {fetchMeasurements, mapApiResponseToGraphData} from '../../../state/ui/graph/measurement/measurementActions';
import {initialState, MeasurementResponses, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {toggleReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {getSelectedPeriod} from '../../../state/user-selection/userSelectionSelectors';
import {ErrorResponse, uuid} from '../../../types/Types';
import {logout} from '../../auth/authActions';
import {OnLogout} from '../../auth/authModels';
import {GraphContents, hardcodedIndicators} from '../reportModels';
import {GraphContainer} from './GraphContainer';
import {LegendContainer} from './LegendContainer';
import {MeasurementListContainer} from './MeasurementListContainer';

interface StateToProps {
  customDateRange: Maybe<DateRange>;
  period: Period;
  selectedIndicators: Medium[];
  selectedListItems: uuid[];
  selectedQuantities: Quantity[];
}

export interface ReportContainerState {
  hiddenKeys: string[];
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  selectedTab: TabName;
  measurementResponse: MeasurementResponses;
}

interface DispatchToProps {
  toggleReportIndicatorWidget: OnSelectIndicator;
  logout: OnLogout;
}

type Props = StateToProps & SelectedIndicatorWidgetProps & DispatchToProps & InjectedAuthRouterProps;

const style: React.CSSProperties = {width: '100%', height: '100%'};
const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 16};

export type OnUpdateGraph = (state: ReportContainerState) => void;

class ReportComponent extends React.Component<Props, ReportContainerState> {
  constructor(props) {
    super(props);
    this.state = {...initialState};
  }

  updateState = (state: ReportContainerState) => this.setState({...state});

  clearError = async () => {
    const {selectedIndicators, selectedListItems, period, customDateRange, selectedQuantities, logout} = this.props;
    this.setState({error: Maybe.nothing(), isFetching: true});
    await fetchMeasurements({
      selectedIndicators,
      quantities: selectedQuantities,
      selectedListItems,
      timePeriod: period,
      customDateRange,
      updateState: this.updateState,
      logout,
    });
  }

  async componentDidMount() {
    const {selectedListItems, period, customDateRange, selectedQuantities, logout, selectedIndicators} = this.props;

    this.setState({isFetching: true});

    await fetchMeasurements({
      selectedIndicators,
      quantities: selectedQuantities,
      selectedListItems,
      timePeriod: period,
      customDateRange,
      updateState: this.updateState,
      logout,
    });
  }

  async componentWillReceiveProps({
    selectedListItems, period, customDateRange, selectedQuantities, logout, selectedIndicators,
  }: Props) {
    this.setState({isFetching: true});
    await fetchMeasurements({
      selectedIndicators,
      quantities: selectedQuantities,
      selectedListItems,
      timePeriod: period,
      customDateRange,
      updateState: this.updateState,
      logout,
    });
  }

  render() {
    const {selectedIndicatorTypes, toggleReportIndicatorWidget} = this.props;
    const {isFetching, error, hiddenKeys, selectedTab, measurementResponse} = this.state;

    const graphContents: GraphContents = mapApiResponseToGraphData(measurementResponse);

    const onChangeTab = (selectedTab: TabName) => this.setState({selectedTab});

    const onToggleLine = (dataKey: string) => {
      this.setState({
        hiddenKeys: toggle(
          dataKey,
          hiddenKeys,
        ),
      });
    };

    const indicators = hardcodedIndicators();

    const renderLegend = () => graphContents.lines.length > 0 ?
      <LegendContainer graphContents={graphContents} onToggleLine={onToggleLine}/> : null;

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
          indicators={indicators}
          selectedIndicatorTypes={selectedIndicatorTypes}
          onClick={toggleReportIndicatorWidget}
        />

        <Loader isFetching={isFetching} error={error} clearError={this.clearError}>
          <Paper style={contentStyle}>
            <div style={style}>
              <Tabs>
                <TabTopBar>
                  <TabHeaders selectedTab={selectedTab} onChangeTab={onChangeTab}>
                    <Tab tab={TabName.graph} title={translate('graph')}/>
                    <Tab tab={TabName.list} title={translate('list')}/>
                  </TabHeaders>
                </TabTopBar>
                <TabContent tab={TabName.graph} selectedTab={selectedTab}>
                  <GraphContainer graphContents={graphContents} outerHiddenKeys={hiddenKeys}/>
                </TabContent>
                <TabContent tab={TabName.list} selectedTab={selectedTab}>
                  <MeasurementListContainer measurement={measurementResponse.measurement}/>
                </TabContent>
              </Tabs>
            </div>
            {renderLegend()}
          </Paper>
        </Loader>
      </MvpPageContainer>
    );
  }
}

const mapStateToProps =
  ({
    report: {selectedListItems},
    userSelection: {userSelection},
    ui: {indicator: {selectedIndicators: {report}, selectedQuantities}},
  }: RootState): StateToProps & SelectedIndicatorWidgetProps =>
    ({
      ...getSelectedPeriod(userSelection),
      selectedListItems,
      selectedQuantities,
      selectedIndicators: report,
      selectedIndicatorTypes: report,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleReportIndicatorWidget,
  logout,
}, dispatch);

export const ReportContainer =
  connect<SelectedIndicatorWidgetProps, DispatchToProps>(
    mapStateToProps,
    mapDispatchToProps,
  )(ReportComponent);
