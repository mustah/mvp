import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {DateRange, Period} from '../../components/dates/dateModels';
import {PeriodSelection} from '../../components/dates/PeriodSelection';
import {withLargeLoader} from '../../components/hoc/withLoaders';
import {Column} from '../../components/layouts/column/Column';
import {renderCreated} from '../../components/table/cellContentHelper';
import '../../components/table/Table.scss';
import {TimestampInfoMessage} from '../../components/timestamp-info-message/TimestampInfoMessage';
import {newDateRange} from '../../helpers/dateHelpers';
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

const renderValue = ({value, unit}: Measurement): string =>
  value !== undefined && unit ? `${roundMeasurement(value)} ${unit}` : '';

const renderReadingRows =
  (quantities: Quantity[]) =>
    (readings: Readings): Array<React.ReactElement<HTMLTableRowElement>> => {
      const rowSpanOfMissingMeasurements = quantities.length;
      const missingMeasurements = <td key={1} colSpan={rowSpanOfMissingMeasurements}/>;

      return Object.keys(readings)
        .map((key: string) => Number(key))
        .sort()
        .reverse()
        .map((timestamp: UnixTimestamp) => {
          const reading = readings[timestamp];
          const row = reading.measurements
            ? quantities
              .map((quantity: Quantity) => reading.measurements![quantity])
              .map((measurement: Measurement, index: number) => <td key={index}>{renderValue(measurement)}</td>)
            : missingMeasurements;

          return (
            <tr key={timestamp}>
              {renderCreated(timestamp, !!reading.measurements)}
              {row}
            </tr>
          );
        });
    };

const readoutColumnStyle: React.CSSProperties = {
  width: 80,
};

const style: React.CSSProperties = {
  marginTop: -46,
  marginRight: 40,
  marginBottom: 0,
  marginLeft: 0,
};

interface ReadingsProps {
  readings: Readings;
  quantities: Quantity[];
}

interface OwnProps {
  meter: MeterDetails;
}

interface DispatchToProps {
  logout: OnLogout;
}

const renderQuantity =
  (quantity: Quantity) =>
    <th key={quantity}>{translate(quantity + ' short')}</th>;

const MeasurementsTable = ({readings, quantities}: ReadingsProps) => (
  <Column>
    <table key="1" className="Table" cellPadding="0" cellSpacing="0">
      <thead>
      <tr>
        <th style={readoutColumnStyle} className="first" key="readout">{translate('readout')}</th>
        {quantities.map(renderQuantity)}
      </tr>
      </thead>
      <tbody>
      {renderReadingRows(quantities)(readings)}
      </tbody>
    </table>
    <TimestampInfoMessage/>
  </Column>
);

type WrapperProps = Fetching & ReadingsProps;

const MeasurementsTableComponent = withLargeLoader<ReadingsProps>(MeasurementsTable);

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

    const readings: Readings = fillMissingMeasurements({
      existingReadings,
      readIntervalMinutes,
      dateRange,
    });

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
        <MeasurementsTableComponent {...wrapperProps}/>
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
