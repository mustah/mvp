import {Grid, GridColumn} from '@progress/kendo-react-grid';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DateRange, Period} from '../../components/dates/dateModels';
import {PeriodSelection} from '../../components/dates/PeriodSelection';
import {Column} from '../../components/layouts/column/Column';
import '../../components/table/Table.scss';
import {Error} from '../../components/texts/Texts';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
import {newDateRange, timestamp} from '../../helpers/dateHelpers';
import {roundMeasurement} from '../../helpers/formatters';
import {Maybe} from '../../helpers/Maybe';
import {translate} from '../../services/translationService';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';
import {fetchMeasurementsPaged} from '../../state/ui/graph/measurement/measurementActions';
import {
  getMediumType,
  initialMeterMeasurementsState,
  Measurement,
  MeterMeasurementsState,
  PossibleReading,
  Quantity,
  Readings
} from '../../state/ui/graph/measurement/measurementModels';
import {groupMeasurementsByDate, MeasurementTableData} from '../../state/ui/graph/measurement/measurementSelectors';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Fetching, UnixTimestamp} from '../../types/Types';
import {logout} from '../../usecases/auth/authActions';
import {OnLogout} from '../../usecases/auth/authModels';
import {fillMissingMeasurements} from './dialogHelper';
import './MeterMeasurements.scss';

const renderValue = (measurement?: Measurement): string =>
  measurement !== undefined && measurement.value !== undefined && measurement.unit
    ? `${roundMeasurement(measurement.value)} ${measurement.unit}` : '';

const orderedReadings = (readings: Readings): PossibleReading[] =>
  Object.keys(readings)
    .map((key: string) => Number(key))
    .sort()
    .reverse()
    .map((timestamp: UnixTimestamp) => readings[timestamp]);

const style: React.CSSProperties = {
  marginTop: -46,
  marginRight: 40,
  marginBottom: 0,
  marginLeft: 0,
};

interface ReadingsProps {
  readings: PossibleReading[];
  quantities: Quantity[];
}

interface OwnProps {
  meter: MeterDetails;
}

interface DispatchToProps {
  logout: OnLogout;
}

const measurementCell =
  (quantity: Quantity) =>
    ({dataItem}) =>
      <td key={dataItem.id}>{renderValue(dataItem.measurements && dataItem.measurements[quantity])}</td>;

const smallWidth = 110;

const quantityWidth: { [q in Quantity]: number } = {
  [Quantity.temperature]: smallWidth,
  [Quantity.externalTemperature]: smallWidth,
  [Quantity.differenceTemperature]: smallWidth,
  [Quantity.forwardTemperature]: smallWidth,
  [Quantity.returnTemperature]: smallWidth,
  [Quantity.energyReactive]: smallWidth,
  [Quantity.volume]: smallWidth + 30,
  [Quantity.flow]: smallWidth + 30,
  [Quantity.power]: smallWidth + 30,
  [Quantity.relativeHumidity]: smallWidth + 30,
  [Quantity.energyReturn]: smallWidth + 30,
  [Quantity.energy]: smallWidth + 60,
};

const gridColumnOfQuantity = (q: Quantity) => (
  <GridColumn
    key={`measurements.${q}`}
    cell={measurementCell(q)}
    title={translate(`${q} short`)}
    width={quantityWidth[q]}
  />);

const renderCreated = (created: UnixTimestamp, hasValues: boolean) => {
  const textual = hasValues
    ? timestamp(created * 1000)
    : <Error>{timestamp(created * 1000)}</Error>;
  return <td className="no-wrap left-most" key="created">{textual}</td>;
};

const MeasurementsTable = ({readings, quantities}: ReadingsProps) => {
  const renderTimestamp = (quantities: Quantity[]) =>
    ({dataItem}) => {
      const hasValues = quantities.length
                        && dataItem.measurements
                        && Object.keys(dataItem.measurements).length === quantities.length;
      return renderCreated(dataItem.id, hasValues);
    };

  return (
    <Grid scrollable="none" data={readings}>
      <GridColumn
        cell={renderTimestamp(quantities)}
        title={translate('readout')}
        headerClassName="left-most"
        width={136}
      />
      {quantities.map(gridColumnOfQuantity)}
    </Grid>
  );
};

type WrapperProps = Fetching & ReadingsProps;

type Props = OwnProps & DispatchToProps;

interface State extends MeterMeasurementsState {
  period: Period;
  customDateRange: Maybe<DateRange>;
}

class MeterMeasurements extends React.Component<Props, State> {

  constructor(props) {
    super(props);
    this.state = {
      ...initialMeterMeasurementsState,
      customDateRange: Maybe.nothing(),
      period: Period.latest,
    };
  }

  async componentDidMount() {
    const {meter: {id}, logout} = this.props;
    const {period, customDateRange} = this.state;

    this.setState({isFetching: true});

    await fetchMeasurementsPaged(
      id,
      {period, customDateRange: customDateRange.getOrElseUndefined()},
      this.onUpdateState,
      logout
    );
  }

  async componentWillReceiveProps({meter: {id}, logout}: Props) {
    const {period, customDateRange} = this.state;

    this.setState({isFetching: true});

    await fetchMeasurementsPaged(
      id,
      {period, customDateRange: customDateRange.getOrElseUndefined()},
      this.onUpdateState,
      logout
    );
  }

  render() {
    const {customDateRange, isFetching, measurementPages, period} = this.state;
    const {meter: {medium, readIntervalMinutes}} = this.props;

    const {readings: existingReadings, quantities}: MeasurementTableData = groupMeasurementsByDate(
      measurementPages,
      getMediumType(medium),
    );

    const dateRange = customDateRange
      .map((dateRange) => newDateRange(period, Maybe.just(dateRange)))
      .orElseGet(() => newDateRange(period));

    const readings: PossibleReading[] = orderedReadings(fillMissingMeasurements({
      existingReadings,
      readIntervalMinutes,
      dateRange,
    }));

    const wrapperProps: WrapperProps = {isFetching, readings, quantities};

    return (
      <Column className="MeterMeasurements">
        <PeriodSelection
          customDateRange={customDateRange}
          period={period}
          selectPeriod={this.selectPeriod}
          setCustomDateRange={this.setCustomDateRange}
          style={style}
        />
        <MeasurementsTable {...wrapperProps}/>
        <TimestampInfoMessage className="Measurements"/>
      </Column>
    );
  }

  selectPeriod = async (period: Period) => {
    this.setState({period, isFetching: true});

    await this.doFetch({period, customDateRange: this.state.customDateRange.getOrElseUndefined()});
  }

  setCustomDateRange = async (dateRange: DateRange) => {
    const customDateRange = Maybe.maybe(dateRange);
    const period = Period.custom;

    this.setState({customDateRange, period, isFetching: true});

    await this.doFetch({period, customDateRange: customDateRange.getOrElseUndefined()});
  }

  onUpdateState = (state: MeterMeasurementsState) => this.setState({...state});

  doFetch = async (selectionInterval: SelectionInterval) => {
    const {meter: {id}, logout} = this.props;

    await fetchMeasurementsPaged(id, selectionInterval, this.onUpdateState, logout);
  }
}

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  logout,
}, dispatch);

export const MeterMeasurementsContainer = connect<{}, DispatchToProps, OwnProps>(
  mapDispatchToProps,
)(MeterMeasurements);
