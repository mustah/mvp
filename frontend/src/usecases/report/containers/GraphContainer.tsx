import * as React from 'react';
import {connect} from 'react-redux';
import {CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';
import {bindActionCreators} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {Bold} from '../../../components/texts/Texts';
import {currentDateRange, toApiParameters} from '../../../helpers/dateHelpers';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {fetchMeasurements} from '../../../state/domain-models/domainModelsActions';
import {MeasurementState} from '../../../state/domain-models/measurement/measurementModels';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {Children, uuid} from '../../../types/Types';
import {mapNormalizedPaginatedResultToGraphData} from '../reportHelpers';
import {GraphContents, LineProps} from '../reportModels';
import './GraphContainer.scss';

interface StateToProps {
  measurements: MeasurementState;
  period: Period;
  selectedListItems: uuid[];
}

interface OwnProps {
  selectedTabOption: string;
}

interface DispatchToProps {
  fetchMeasurements: (encodedUriParameters: string) => void;
}

type Props = StateToProps & DispatchToProps;

const style: React.CSSProperties = {width: '100%', height: '100%'};
const margin: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};

const renderGraphContents = ({lines, axes}: GraphContents): Children[] => {
  const components: Children[] = lines.map((props: LineProps) => (
    <Line
      yAxisId="left"
      type="monotone"
      {...props}
    />
  ));

  if (axes.left) {
    components.push((
      <YAxis key="leftYAxis" label={axes.left} yAxisId="left"/>
    ));
  }

  if (axes.right) {
    components.push((
      <YAxis key="rightYAxis" label={axes.right} yAxisId="right" orientation="right"/>
    ));
  }

  return components;
};

class GraphComponent extends React.Component<Props> {

  state: OwnProps = {selectedTabOption: 'power'};

  onChangeTab = () => void(0);

  fetchMeasurementDataByMeters = (meterIds: uuid[]): void => {
    // TODO when proper ids are used in the left hand side selection tree, you can use:
    // this.props.selectedListItems.map((id: uuid) => `meterId=${id.toString()}`).join('&');
    const parameters: string[] = meterIds.map((id: uuid) => `meterId=${id.toString()}`);
    toApiParameters(currentDateRange(this.props.period)).forEach((parameter: string) =>
      parameters.push(parameter));
    parameters.push('size=500');
    // TODO Spring uses 20 as a default size, we
    this.props.fetchMeasurements(parameters.join('&'));
  }

  componentDidMount() {
    this.fetchMeasurementDataByMeters([1, 2, 3]);
  }

  componentWillReceiveProps(nextProps: Props) {
    if (JSON.stringify(this.props.selectedListItems) !== JSON.stringify(nextProps.selectedListItems)) {
      this.fetchMeasurementDataByMeters([1, 2, 3]);
    }
  }

  render() {
    const {measurements} = this.props;
    const graphContents = mapNormalizedPaginatedResultToGraphData(measurements.entities);
    const {data} = graphContents;
    const lines = renderGraphContents(graphContents);

    const selectedTab: TabName = TabName.graph;

    // ResponsiveContainer is a bit weird, if we leave out the dimensions of the containing <div>, it breaks
    // Setting width of ResponsiveContainer to 100% will case the menu to overlap when toggled
    return (
      <div style={style}>
        <Tabs>
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={this.onChangeTab}>
              <Tab tab={TabName.graph} title={translate('graph')}/>
              <Tab tab={TabName.table} title={translate('table')}/>
            </TabHeaders>
            <TabSettings/>
          </TabTopBar>
          <TabContent tab={TabName.graph} selectedTab={selectedTab}>
            <ResponsiveContainer width="80%" aspect={4.0}>
              <LineChart
                width={10}
                height={30}
                data={data}
                margin={margin}
              >
                <XAxis dataKey="name"/>
                <CartesianGrid strokeDasharray="3 3"/>
                <Tooltip/>
                <Legend/>
                {lines}
              </LineChart>
            </ResponsiveContainer>
          </TabContent>
          <TabContent tab={TabName.table} selectedTab={selectedTab}>
            <Bold>TBD</Bold>
          </TabContent>
        </Tabs>
      </div>
    );
  }
}

const mapStateToProps =
  ({
     domainModels: {measurements},
     report: {selectedListItems},
     searchParameters: {selection: {selected: {period}}},
   }: RootState): StateToProps =>
    ({
      measurements,
      selectedListItems,
      period,
    });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  fetchMeasurements,
}, dispatch);

export const GraphContainer = connect(mapStateToProps, mapDispatchToProps)(GraphComponent);
