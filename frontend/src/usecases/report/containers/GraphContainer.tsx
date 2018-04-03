import {Paper} from 'material-ui';
import MenuItem from 'material-ui/MenuItem';
import SelectField from 'material-ui/SelectField';
import * as React from 'react';
import {connect} from 'react-redux';
import {CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {HasContent} from '../../../components/content/HasContent';
import {Period} from '../../../components/dates/dateModels';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabSettings} from '../../../components/tabs/components/TabSettings';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MissingDataTitle} from '../../../components/texts/Titles';
import {unixTimestampMillisecondsToDate} from '../../../helpers/formatters';
import {RootState} from '../../../reducers/rootReducer';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {
  fetchMeasurements,
  mapApiResponseToGraphData,
  selectQuantities,
} from '../../../state/ui/graph/measurement/measurementActions';
import {Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {Children, uuid} from '../../../types/Types';
import {allQuantities, emptyGraphContents} from '../reportHelpers';
import {GraphContents, LineProps} from '../reportModels';
import './GraphContainer.scss';

interface StateToProps {
  period: Period;
  selectedListItems: uuid[];
  selectedQuantities: Quantity[];
}

interface State {
  graphContents: GraphContents;
}

interface DispatchToProps {
  selectQuantities: (quantities: Quantity[]) => void;
}

type Props = StateToProps & DispatchToProps;

const style: React.CSSProperties = {width: '100%', height: '100%'};
const margin: React.CSSProperties = {top: 40, right: 0, bottom: 0, left: 0};

const renderGraphContents = ({lines, axes}: GraphContents): Children[] => {
  const components: Children[] = lines.map((props: LineProps, index: number) => (
    <Line
      key={index}
      yAxisId="left"
      type="monotone"
      dot={false}
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

const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 24};

const formatTimestamp = (when: number): string => unixTimestampMillisecondsToDate(when);

class GraphComponent extends React.Component<Props> {

  state: State = {graphContents: emptyGraphContents};

  async componentDidMount() {
    const {selectedListItems, period, selectedQuantities} = this.props;
    const graphData = await fetchMeasurements(selectedQuantities, selectedListItems, period);

    this.setState({
      ...this.state,
      graphContents: mapApiResponseToGraphData(graphData),
      selectedQuantities,
    });
  }

  async componentWillReceiveProps({selectedListItems, period, selectedQuantities}: Props) {
    const somethingChanged = true || period !== this.props.period;
    if (somethingChanged) {
      const graphData = await fetchMeasurements(selectedQuantities, selectedListItems, period);
      this.setState({
        ...this.state,
        graphContents: mapApiResponseToGraphData(graphData),
        selectedQuantities,
      });
    }
  }

  render() {
    const {selectedQuantities} = this.props;
    const {graphContents} = this.state;
    const lines = renderGraphContents(graphContents);
    const {data} = graphContents;

    const selectedTab: TabName = TabName.graph;

    const quantityMenuItem = (quantity: string) => (
      <MenuItem
        key={quantity}
        checked={selectedQuantities.includes(quantity)}
        value={quantity}
        primaryText={quantity}
      />
    );

    // TODO: [!Carl]
    // ResponsiveContainer is a bit weird, if we leave out the dimensions of the containing <div>,
    // it breaks. Setting width of ResponsiveContainer to 100% will cause the menu to overlap when
    // toggled

    const missingData = (
      <MissingDataTitle
        title={firstUpperTranslated('select meters to include in graph')}
      />
    );

    return (
      <Paper style={contentStyle}>
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
              <div style={{padding: '20px 20px 0px'}}>
                <SelectField
                  multiple={true}
                  hintText={firstUpperTranslated('select quantities')}
                  value={selectedQuantities}
                  onChange={this.changeQuantities}
                >
                  {allQuantities.heat.map(quantityMenuItem)}
                </SelectField>
              </div>
              <HasContent
                hasContent={data.length > 0}
                fallbackContent={missingData}
              >
                <div>
                  <ResponsiveContainer width="80%" aspect={4.0}>
                    <LineChart
                      width={10}
                      height={50}
                      data={data}
                      margin={margin}
                    >
                      <XAxis
                        dataKey="name"
                        domain={['dataMin', 'dataMax']}
                        scale="time"
                        tickFormatter={formatTimestamp}
                        type="number"
                      />
                      <CartesianGrid strokeDasharray="3 3"/>
                      <Tooltip/>
                      <Legend/>
                      {lines}
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </HasContent>
            </TabContent>
            <TabContent tab={TabName.table} selectedTab={selectedTab}>
              <HasContent
                hasContent={false}
                fallbackContent={missingData}
              >
                <p>TBD</p>
              </HasContent>
            </TabContent>
          </Tabs>
        </div>
      </Paper>
    );
  }

  onChangeTab = () => void(0);

  changeQuantities = (event, index, values) => {
    this.props.selectQuantities(values);
  }

}

const mapStateToProps = ({
  report: {selectedListItems},
  searchParameters: {selection: {selected: {period}}},
  ui: {measurements: {selectedQuantities}},
}: RootState): StateToProps =>
  ({
    selectedListItems,
    period,
    selectedQuantities,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectQuantities,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(GraphComponent);
